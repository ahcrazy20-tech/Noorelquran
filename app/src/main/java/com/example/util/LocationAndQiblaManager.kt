package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class UserGeoLocation(
    val latitude: Double,
    val longitude: Double,
    val areaNameArabic: String,
    val qiblaBearingDegrees: Float,
    val distanceToKaabaKm: Int
)

class LocationAndQiblaManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("noor_location_prefs", Context.MODE_PRIVATE)
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(context) }

    fun hasRequestedLocationBefore(): Boolean {
        return prefs.getBoolean(KEY_REQUESTED_ONCE, false)
    }

    fun markLocationRequested() {
        prefs.edit().putBoolean(KEY_REQUESTED_ONCE, true).apply()
    }

    fun getSavedLocation(): UserGeoLocation? {
        if (!prefs.contains(KEY_LAT) || !prefs.contains(KEY_LNG)) return null
        val lat = prefs.getFloat(KEY_LAT, 30.0444f).toDouble()
        val lng = prefs.getFloat(KEY_LNG, 31.2357f).toDouble()
        val name = prefs.getString(KEY_NAME, "القاهرة (موقعي المحفوظ)") ?: "موقعي المحفوظ"
        return buildGeoLocation(lat, lng, name)
    }

    private fun saveLocation(loc: UserGeoLocation) {
        prefs.edit()
            .putBoolean(KEY_REQUESTED_ONCE, true)
            .putFloat(KEY_LAT, loc.latitude.toFloat())
            .putFloat(KEY_LNG, loc.longitude.toFloat())
            .putString(KEY_NAME, loc.areaNameArabic)
            .apply()
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentDeviceLocation(): Result<UserGeoLocation> = withContext(Dispatchers.IO) {
        runCatching {
            markLocationRequested()
            var androidLoc: Location? = withTimeoutOrNull(6000L) {
                try {
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        CancellationTokenSource().token
                    ).await()
                } catch (_: Exception) {
                    null
                }
            }

            if (androidLoc == null) {
                androidLoc = withTimeoutOrNull(3000L) {
                    try {
                        fusedClient.lastLocation.await()
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            if (androidLoc == null) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                androidLoc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            val finalLat = androidLoc?.latitude ?: 30.0444
            val finalLng = androidLoc?.longitude ?: 31.2357
            val resolvedName = resolveCityNameArabic(finalLat, finalLng)
            val userLoc = buildGeoLocation(finalLat, finalLng, resolvedName)
            saveLocation(userLoc)
            userLoc
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveCityNameArabic(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("ar"))
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val addr = addresses?.firstOrNull()
            val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
            val country = addr?.countryName
            when {
                !city.isNullOrBlank() && !country.isNullOrBlank() -> "$city، $country"
                !city.isNullOrBlank() -> city
                !country.isNullOrBlank() -> country
                else -> "إحداثيات (${String.format(Locale.US, "%.2f", lat)}, ${String.format(Locale.US, "%.2f", lng)})"
            }
        } catch (_: Exception) {
            "إحداثيات (${String.format(Locale.US, "%.2f", lat)}, ${String.format(Locale.US, "%.2f", lng)})"
        }
    }

    fun observeCompassHeadingDegrees(): Flow<Float> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (sensorManager == null || (rotationSensor == null && (accelerometer == null || magnetometer == null))) {
            trySend(0f)
            awaitClose {}
            return@callbackFlow
        }

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthDeg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                    trySend(azimuthDeg)
                } else {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasMagnetic = true
                    }
                    if (hasGravity && hasMagnetic) {
                        val r = FloatArray(9)
                        val i = FloatArray(9)
                        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(r, orientation)
                            val azimuthDeg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                            trySend(azimuthDeg)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    companion object {
        private const val KEY_REQUESTED_ONCE = "has_requested_location_once"
        private const val KEY_LAT = "saved_lat"
        private const val KEY_LNG = "saved_lng"
        private const val KEY_NAME = "saved_area_name"

        private const val KAABA_LAT = 21.422487
        private const val KAABA_LNG = 39.826206

        fun buildGeoLocation(lat: Double, lng: Double, areaName: String): UserGeoLocation {
            val bearing = calculateQiblaBearing(lat, lng)
            val distKm = calculateDistanceKm(lat, lng, KAABA_LAT, KAABA_LNG)
            return UserGeoLocation(
                latitude = lat,
                longitude = lng,
                areaNameArabic = areaName,
                qiblaBearingDegrees = bearing,
                distanceToKaabaKm = distKm
            )
        }

        fun calculateQiblaBearing(lat: Double, lng: Double): Float {
            val phiK = Math.toRadians(KAABA_LAT)
            val lambdaK = Math.toRadians(KAABA_LNG)
            val phi = Math.toRadians(lat)
            val lambda = Math.toRadians(lng)

            val deltaLambda = lambdaK - lambda
            val y = sin(deltaLambda)
            val x = cos(phi) * Math.tan(phiK) - sin(phi) * cos(deltaLambda)
            val bearingRad = atan2(y, x)
            return ((Math.toDegrees(bearingRad) + 360.0) % 360.0).toFloat()
        }

        private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
            val r = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return (r * c).roundToInt()
        }
    }
}
