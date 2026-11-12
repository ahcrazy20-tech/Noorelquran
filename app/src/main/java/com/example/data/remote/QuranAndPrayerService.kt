package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.QuranStaticCatalog
import com.example.data.model.AyahItem
import com.example.data.model.DownloadedSurahAudio
import com.example.data.model.PrayerTimings
import com.example.data.model.ReciterInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.concurrent.TimeUnit

class QuranAndPrayerService(private val context: Context) {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val surahCache = mutableMapOf<Int, List<AyahItem>>()

    suspend fun fetchSurahWithTafsir(surahNumber: Int): List<AyahItem> = withContext(Dispatchers.IO) {
        surahCache[surahNumber]?.let { return@withContext it }

        try {
            val url = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,ar.muyassar,ar.jalalayn"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext QuranStaticCatalog.getOfflineFallbackAyahs(surahNumber)
                }
                val bodyStr = response.body?.string().orEmpty()
                if (bodyStr.isBlank()) {
                    return@withContext QuranStaticCatalog.getOfflineFallbackAyahs(surahNumber)
                }
                val root = JSONObject(bodyStr)
                val dataArray = root.optJSONArray("data")
                    ?: return@withContext QuranStaticCatalog.getOfflineFallbackAyahs(surahNumber)
                if (dataArray.length() < 2) {
                    return@withContext QuranStaticCatalog.getOfflineFallbackAyahs(surahNumber)
                }

                val uthmaniObj = dataArray.getJSONObject(0)
                val muyassarObj = dataArray.getJSONObject(1)
                val jalalaynObj = if (dataArray.length() >= 3) dataArray.getJSONObject(2) else null

                val uthmaniAyahs = uthmaniObj.getJSONArray("ayahs")
                val muyassarAyahs = muyassarObj.getJSONArray("ayahs")
                val jalalaynAyahs = jalalaynObj?.optJSONArray("ayahs")

                val result = mutableListOf<AyahItem>()
                for (i in 0 until uthmaniAyahs.length()) {
                    val u = uthmaniAyahs.getJSONObject(i)
                    val m = if (i < muyassarAyahs.length()) muyassarAyahs.getJSONObject(i) else null
                    val j = if (jalalaynAyahs != null && i < jalalaynAyahs.length()) jalalaynAyahs.getJSONObject(i) else null

                    result.add(
                        AyahItem(
                            numberInSurah = u.optInt("numberInSurah", i + 1),
                            globalNumber = u.optInt("number", i + 1),
                            textUthmani = u.optString("text", ""),
                            page = u.optInt("page", 1),
                            juz = u.optInt("juz", 1),
                            tafsirMuyassar = m?.optString("text", "").orEmpty(),
                            tafsirJalalayn = j?.optString("text", "").orEmpty()
                        )
                    )
                }
                if (result.isNotEmpty()) {
                    surahCache[surahNumber] = result
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            Log.w("QuranService", "Using offline Quran fallback for surah $surahNumber: ${e.message}")
        }
        QuranStaticCatalog.getOfflineFallbackAyahs(surahNumber)
    }

    suspend fun fetchPrayerTimings(cityLabelArabic: String, cityQuery: String): PrayerTimings = withContext(Dispatchers.IO) {
        val parts = cityQuery.split(",")
        val city = parts.getOrNull(0)?.trim() ?: "Cairo"
        val country = parts.getOrNull(1)?.trim() ?: "Egypt"

        try {
            val url = "https://api.aladhan.com/v1/timingsByCity?city=$city&country=$country&method=5"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext QuranStaticCatalog.defaultPrayerTimings(cityLabelArabic)
                }
                val bodyStr = response.body?.string().orEmpty()
                val root = JSONObject(bodyStr)
                val data = root.optJSONObject("data")
                    ?: return@withContext QuranStaticCatalog.defaultPrayerTimings(cityLabelArabic)
                val timings = data.optJSONObject("timings")
                    ?: return@withContext QuranStaticCatalog.defaultPrayerTimings(cityLabelArabic)
                val dateObj = data.optJSONObject("date")?.optJSONObject("hijri")
                val hijriDay = dateObj?.optString("day", "١٢") ?: "١٢"
                val hijriMonth = dateObj?.optJSONObject("month")?.optString("ar", "جمادى الأولى") ?: "جمادى الأولى"
                val hijriYear = dateObj?.optString("year", "١٤٤٨") ?: "١٤٤٨"

                val fajr = timings.optString("Fajr", "04:52").substringBefore(" ")
                val sunrise = timings.optString("Sunrise", "06:19").substringBefore(" ")
                val dhuhr = timings.optString("Dhuhr", "11:48").substringBefore(" ")
                val asr = timings.optString("Asr", "14:51").substringBefore(" ")
                val maghrib = timings.optString("Maghrib", "17:16").substringBefore(" ")
                val isha = timings.optString("Isha", "18:36").substringBefore(" ")

                val (nextName, nextTime, remaining) = computeNextPrayer(fajr, dhuhr, asr, maghrib, isha)

                return@withContext PrayerTimings(
                    city = cityLabelArabic,
                    country = country,
                    hijriDate = "$hijriDay $hijriMonth $hijriYear هـ",
                    fajr = fajr,
                    sunrise = sunrise,
                    dhuhr = dhuhr,
                    asr = asr,
                    maghrib = maghrib,
                    isha = isha,
                    nextPrayerName = nextName,
                    nextPrayerTime = nextTime,
                    remainingTimeText = remaining
                )
            }
        } catch (e: Exception) {
            Log.w("QuranService", "Fallback prayer timings for $cityLabelArabic: ${e.message}")
            QuranStaticCatalog.defaultPrayerTimings(cityLabelArabic)
        }
    }

    suspend fun fetchPrayerTimingsByCoordinates(
        latitude: Double,
        longitude: Double,
        areaLabelArabic: String
    ): PrayerTimings = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.aladhan.com/v1/timings?latitude=$latitude&longitude=$longitude&method=5"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext QuranStaticCatalog.defaultPrayerTimings(areaLabelArabic)
                }
                val bodyStr = response.body?.string().orEmpty()
                val root = JSONObject(bodyStr)
                val data = root.optJSONObject("data")
                    ?: return@withContext QuranStaticCatalog.defaultPrayerTimings(areaLabelArabic)
                val timings = data.optJSONObject("timings")
                    ?: return@withContext QuranStaticCatalog.defaultPrayerTimings(areaLabelArabic)
                val dateObj = data.optJSONObject("date")?.optJSONObject("hijri")
                val hijriDay = dateObj?.optString("day", "١٢") ?: "١٢"
                val hijriMonth = dateObj?.optJSONObject("month")?.optString("ar", "جمادى الأولى") ?: "جمادى الأولى"
                val hijriYear = dateObj?.optString("year", "١٤٤٨") ?: "١٤٤٨"

                val fajr = timings.optString("Fajr", "04:52").substringBefore(" ")
                val sunrise = timings.optString("Sunrise", "06:19").substringBefore(" ")
                val dhuhr = timings.optString("Dhuhr", "11:48").substringBefore(" ")
                val asr = timings.optString("Asr", "14:51").substringBefore(" ")
                val maghrib = timings.optString("Maghrib", "17:16").substringBefore(" ")
                val isha = timings.optString("Isha", "18:36").substringBefore(" ")

                val (nextName, nextTime, remaining) = computeNextPrayer(fajr, dhuhr, asr, maghrib, isha)

                return@withContext PrayerTimings(
                    city = areaLabelArabic,
                    country = "موقعي الحالي (GPS)",
                    hijriDate = "$hijriDay $hijriMonth $hijriYear هـ",
                    fajr = fajr,
                    sunrise = sunrise,
                    dhuhr = dhuhr,
                    asr = asr,
                    maghrib = maghrib,
                    isha = isha,
                    nextPrayerName = nextName,
                    nextPrayerTime = nextTime,
                    remainingTimeText = remaining
                )
            }
        } catch (e: Exception) {
            Log.w("QuranService", "Fallback coordinate prayer timings: ${e.message}")
            QuranStaticCatalog.defaultPrayerTimings(areaLabelArabic)
        }
    }

    private fun computeNextPrayer(
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String
    ): Triple<String, String, String> {
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val prayers = listOf(
            "الفجر" to fajr,
            "الظهر" to dhuhr,
            "العصر" to asr,
            "المغرب" to maghrib,
            "العشاء" to isha
        )
        for ((name, timeStr) in prayers) {
            val hm = timeStr.split(":")
            val h = hm.getOrNull(0)?.toIntOrNull() ?: 0
            val m = hm.getOrNull(1)?.toIntOrNull() ?: 0
            val totalMin = h * 60 + m
            if (totalMin > nowMinutes) {
                val diff = totalMin - nowMinutes
                val hrs = diff / 60
                val mins = diff % 60
                return Triple(name, timeStr, "متبقٍ ${hrs} س و ${mins} د")
            }
        }
        return Triple("الفجر", fajr, "صلاة الفجر القادمة")
    }

    fun getDownloadedSurahs(): List<DownloadedSurahAudio> {
        val baseDir = File(context.filesDir, "quran_audio")
        if (!baseDir.exists()) return emptyList()
        val result = mutableListOf<DownloadedSurahAudio>()
        baseDir.listFiles()?.forEach { reciterDir ->
            if (reciterDir.isDirectory) {
                reciterDir.listFiles()?.forEach { mp3File ->
                    val surahNum = mp3File.nameWithoutExtension.removePrefix("surah_").toIntOrNull()
                    if (surahNum != null && mp3File.length() > 0L) {
                        result.add(
                            DownloadedSurahAudio(
                                reciterId = reciterDir.name,
                                surahNumber = surahNum,
                                filePath = mp3File.absolutePath,
                                fileSizeBytes = mp3File.length()
                            )
                        )
                    }
                }
            }
        }
        return result
    }

    fun getLocalSurahFile(reciterId: String, surahNumber: Int): File {
        val dir = File(File(context.filesDir, "quran_audio"), reciterId)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "surah_$surahNumber.mp3")
    }

    suspend fun downloadSurahAudio(
        reciter: ReciterInfo,
        surahNumber: Int,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val targetFile = getLocalSurahFile(reciter.id, surahNumber)
            if (targetFile.exists() && targetFile.length() > 10_000L) {
                onProgress(100)
                return@runCatching targetFile
            }
            val url = "${reciter.surahAudioBaseUrl}/$surahNumber.mp3"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("فشل تحميل السورة الصوتية (HTTP ${response.code})")
                }
                val body = response.body ?: throw IllegalStateException("ملف صوتي فارغ")
                val totalBytes = body.contentLength()
                val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var downloaded = 0L
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (totalBytes > 0) {
                                val pct = ((downloaded * 100L) / totalBytes).toInt().coerceIn(1, 99)
                                onProgress(pct)
                            }
                        }
                        output.flush()
                    }
                }
                tempFile.renameTo(targetFile)
                onProgress(100)
                targetFile
            }
        }
    }

    fun deleteDownloadedSurah(reciterId: String, surahNumber: Int): Boolean {
        val file = getLocalSurahFile(reciterId, surahNumber)
        return if (file.exists()) file.delete() else false
    }
}
