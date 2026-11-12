package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.QuranStaticCatalog
import com.example.data.model.ZekrItem
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealHighlight
import com.example.ui.viewmodel.FirestoreListState
import com.example.ui.viewmodel.NoorMainViewModel
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerAndAdhkarScreen(
    viewModel: NoorMainViewModel
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val selectedCityPair by viewModel.selectedCityPair.collectAsState()
    val prayerTimings by viewModel.prayerTimings.collectAsState()
    val userGeoLocation by viewModel.userGeoLocation.collectAsState()
    val isUpdatingLocation by viewModel.isUpdatingLocation.collectAsState()
    val compassHeading by viewModel.compassHeadingDegrees.collectAsState()
    val prayerAlertsEnabled by viewModel.prayerNotificationsEnabled.collectAsState()
    val adhkarAlertsEnabled by viewModel.adhkarNotificationsEnabled.collectAsState()
    val adhkarLogsState by viewModel.adhkarLogsState.collectAsState()

    val adhkarCategories = remember {
        listOf(
            "أذكار الصباح",
            "أذكار المساء",
            "بعد الصلاة",
            "تسبيح واستغفار",
            "أذكار النوم والاستيقاظ"
        )
    }
    var selectedCategory by remember { mutableStateOf(adhkarCategories.first()) }
    val zekrCounts = remember { mutableStateMapOf<String, Int>() }
    val checklistStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put("wird_dua_creator", true)
        }
    }

    val sebhaPhrases = remember {
        listOf(
            "سُبْحَانَ اللهِ وَبِحَمْدِهِ",
            "أَسْتَغْفِرُ اللهَ وَأَتُوبُ إِلَيْهِ",
            "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ"
        )
    }
    var selectedSebhaPhrase by remember { mutableStateOf(sebhaPhrases.first()) }
    var sebhaTarget by remember { mutableIntStateOf(33) }
    var digitalSebhaCounter by remember { mutableIntStateOf(0) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.markLocationPromptShown()
        if (granted) {
            viewModel.updateDeviceLocationFromGps()
        }
    }

    // Request location automatically ONLY the first time the user opens Prayer & Qibla
    LaunchedEffect(Unit) {
        if (!viewModel.hasRequestedLocationBefore()) {
            val hasLocPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasLocPerm) {
                viewModel.updateDeviceLocationFromGps()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    fun requestOrUpdateGpsLocation() {
        val hasLocPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocPerm) {
            viewModel.updateDeviceLocationFromGps()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    var pendingNotificationIsPrayer by remember { mutableStateOf(true) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.sendInstantNotificationTest(context, pendingNotificationIsPrayer)
    }

    fun triggerAlertWithPermission(isPrayer: Boolean) {
        pendingNotificationIsPrayer = isPrayer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.sendInstantNotificationTest(context, isPrayer)
    }

    val totalSavedTasbeehCount = remember(adhkarLogsState) {
        val items = (adhkarLogsState as? FirestoreListState.Success)?.items.orEmpty()
        items.sumOf { it.completedCount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        // 1. GPS LOCATION, PRAYER TIMES & NEXT PRAYER HERO CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مواقيت الصلاة • ${prayerTimings.city}",
                                style = MaterialTheme.typography.titleLarge,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${prayerTimings.hijriDate} • ${prayerTimings.country}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldLight,
                                fontSize = 12.sp
                            )
                        }

                        // Update Location Button (whenever the user changes location)
                        Button(
                            onClick = { requestOrUpdateGpsLocation() },
                            enabled = !isUpdatingLocation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealHighlight,
                                contentColor = EmeraldDeep
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("update_gps_location_button")
                        ) {
                            if (isUpdatingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = EmeraldDeep,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحديث موقعي", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Next Prayer Countdown Banner
                    Surface(
                        color = EmeraldElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الصلاة القادمة: ${prayerTimings.nextPrayerName}",
                                color = TealHighlight,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${prayerTimings.nextPrayerTime} (${prayerTimings.remainingTimeText})",
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual City Fallback Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QuranStaticCatalog.supportedCities, key = { it.first }) { pair ->
                            FilterChip(
                                selected = pair.first == selectedCityPair.first && userGeoLocation == null,
                                onClick = { viewModel.selectCityForPrayer(pair) },
                                label = { Text(pair.first) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = EmeraldDeep
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6 Prayer Times Grid Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrayerTimePill("الفجر", prayerTimings.fajr, prayerTimings.nextPrayerName == "الفجر")
                        PrayerTimePill("الشروق", prayerTimings.sunrise, false)
                        PrayerTimePill("الظهر", prayerTimings.dhuhr, prayerTimings.nextPrayerName == "الظهر")
                        PrayerTimePill("العصر", prayerTimings.asr, prayerTimings.nextPrayerName == "العصر")
                        PrayerTimePill("المغرب", prayerTimings.maghrib, prayerTimings.nextPrayerName == "المغرب")
                        PrayerTimePill("العشاء", prayerTimings.isha, prayerTimings.nextPrayerName == "العشاء")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notification Toggles & Instant Notification Test Buttons
                    Surface(
                        color = EmeraldElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إشعارات الأذان ومواقيت الصلاة", color = GoldLight, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = prayerAlertsEnabled,
                                    onCheckedChange = { viewModel.togglePrayerNotifications(it) }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("منبه أذكار الصباح والمساء والورد", color = GoldLight, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = adhkarAlertsEnabled,
                                    onCheckedChange = { viewModel.toggleAdhkarNotifications(it) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { triggerAlertWithPermission(isPrayer = true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = EmeraldDeep
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_prayer_notification_button")
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إرسال تنبيه الصلاة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { triggerAlertWithPermission(isPrayer = false) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_adhkar_notification_button")
                                ) {
                                    Text("تنبيه الورد والأذكار", color = GoldLight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. LIVE QIBLA COMPASS LINKED TO USER GPS LOCATION
        item {
            val qiblaBearing = userGeoLocation?.qiblaBearingDegrees ?: 136.0f
            val distanceKm = userGeoLocation?.distanceToKaabaKm ?: 1280
            val locName = userGeoLocation?.areaNameArabic ?: prayerTimings.city

            QiblaCompassCard(
                locationLabel = locName,
                qiblaBearingDegrees = qiblaBearing,
                compassHeadingDegrees = compassHeading,
                distanceToKaabaKm = distanceKm,
                onRefreshLocation = { requestOrUpdateGpsLocation() }
            )
        }

        // 3. DAILY DHIKR TOOLKIT: DAILY WIRD CHECKLIST & PROGRESS
        item {
            val totalChecklist = QuranStaticCatalog.dailyDhikrChecklist.size
            val completedChecklist = QuranStaticCatalog.dailyDhikrChecklist.count { checklistStates[it.first] == true }
            val progressFraction = (completedChecklist.toFloat() / totalChecklist.coerceAtLeast(1)).coerceIn(0f, 1f)

            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "أدوات الذكر والورد اليومي",
                                style = MaterialTheme.typography.titleLarge,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "حافظ على أورادك اليومية • إجمالي تسبيحاتك المحفوظة: $totalSavedTasbeehCount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            color = TealHighlight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$completedChecklist / $totalChecklist مكتمل",
                                color = TealHighlight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = TealHighlight,
                        trackColor = EmeraldDeep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    QuranStaticCatalog.dailyDhikrChecklist.forEach { (id, title) ->
                        val checked = checklistStates[id] == true
                        Surface(
                            color = if (checked) EmeraldElevated else EmeraldDeep.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    val nextState = !checked
                                    checklistStates[id] = nextState
                                    if (nextState) {
                                        viewModel.recordAdhkarCompleted(
                                            category = "الورد اليومي",
                                            zekrTitle = title,
                                            count = 1,
                                            target = 1
                                        )
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (checked) TealHighlight else GoldLight.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = title,
                                        color = if (checked) GoldPrimary else GoldLight,
                                        fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (checked) {
                                    Text(
                                        text = "تم بحمد الله",
                                        fontSize = 11.sp,
                                        color = TealHighlight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. ADVANCED MULTI-PHRASE DIGITAL SEBHA (المسبحة الإلكترونية المتطورة)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "المسبحة الإلكترونية الذكية",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Phrase Selector Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sebhaPhrases, key = { it }) { phrase ->
                            FilterChip(
                                selected = phrase == selectedSebhaPhrase,
                                onClick = {
                                    selectedSebhaPhrase = phrase
                                    digitalSebhaCounter = 0
                                },
                                label = { Text(phrase, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = EmeraldDeep
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Selector (33 / 100 / 1000)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("الهدف:", color = GoldLight, fontSize = 12.sp)
                        listOf(33, 100, 1000).forEach { targetVal ->
                            FilterChip(
                                selected = sebhaTarget == targetVal,
                                onClick = { sebhaTarget = targetVal },
                                label = { Text("$targetVal مرة") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSebhaPhrase,
                                fontFamily = AmiriFontFamily,
                                fontSize = 22.sp,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "العدد الحالي: $digitalSebhaCounter من $sebhaTarget",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TealHighlight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        if (digitalSebhaCounter > 0) {
                                            viewModel.recordAdhkarCompleted(
                                                category = "تسبيح واستغفار",
                                                zekrTitle = selectedSebhaPhrase,
                                                count = digitalSebhaCounter,
                                                target = sebhaTarget
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("حفظ في السجل ($digitalSebhaCounter)", color = TealHighlight, fontSize = 12.sp)
                                }
                                IconButton(onClick = { digitalSebhaCounter = 0 }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "تصفير العداد", tint = GoldLight)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(94.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    digitalSebhaCounter++
                                    if (digitalSebhaCounter == sebhaTarget) {
                                        viewModel.recordAdhkarCompleted(
                                            category = "تسبيح واستغفار",
                                            zekrTitle = selectedSebhaPhrase,
                                            count = digitalSebhaCounter,
                                            target = sebhaTarget
                                        )
                                    }
                                }
                                .testTag("digital_sebha_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "اضغط للتسبيح",
                                    tint = EmeraldDeep,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "$digitalSebhaCounter",
                                    color = EmeraldDeep,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. DAILY ADHKAR CATEGORIES & INTERACTIVE COUNTER CARDS
        item {
            Text(
                text = "حصن المسلم وأذكار اليوم والليلة",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                adhkarCategories.forEach { cat ->
                    FilterChip(
                        selected = cat == selectedCategory,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = EmeraldDeep
                        )
                    )
                }
            }
        }

        val categoryItems = QuranStaticCatalog.adhkarCatalog.filter { it.category == selectedCategory }
        items(categoryItems, key = { it.id }) { zekr ->
            val currentCount = zekrCounts[zekr.id] ?: 0
            ZekrInteractiveCard(
                zekr = zekr,
                currentCount = currentCount,
                onTapCount = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val next = currentCount + 1
                    zekrCounts[zekr.id] = next
                    if (next == zekr.targetCount) {
                        viewModel.recordAdhkarCompleted(
                            category = zekr.category,
                            zekrTitle = zekr.arabicText.take(60),
                            count = next,
                            target = zekr.targetCount
                        )
                    }
                },
                onReset = { zekrCounts[zekr.id] = 0 }
            )
        }
    }
}

@Composable
private fun QiblaCompassCard(
    locationLabel: String,
    qiblaBearingDegrees: Float,
    compassHeadingDegrees: Float,
    distanceToKaabaKm: Int,
    onRefreshLocation: () -> Unit
) {
    val relativeQiblaAngle = ((qiblaBearingDegrees - compassHeadingDegrees + 360f) % 360f)
    val isAlignedWithQibla = abs(relativeQiblaAngle) <= 10f || abs(relativeQiblaAngle - 360f) <= 10f

    Card(
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (isAlignedWithQibla) TealHighlight else GoldPrimary.copy(alpha = 0.45f),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "اتجاه القبلة الشريفة (مرتبط بموقعك)",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$locationLabel • المسافة للكعبة: $distanceToKaabaKm كم",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "${qiblaBearingDegrees.toInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = TealHighlight,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Qibla Compass Dial
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Outer ring
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.35f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6f)
                    )
                    drawCircle(
                        color = EmeraldDeep,
                        radius = radius - 6f,
                        center = center
                    )

                    // Rotate needle toward Qibla relative to phone heading
                    rotate(degrees = relativeQiblaAngle, pivot = center) {
                        val arrowPath = Path().apply {
                            moveTo(center.x, center.y - radius + 22f)
                            lineTo(center.x - 16f, center.y + 18f)
                            lineTo(center.x, center.y + 4f)
                            lineTo(center.x + 16f, center.y + 18f)
                            close()
                        }
                        drawPath(
                            path = arrowPath,
                            color = if (isAlignedWithQibla) TealHighlight else GoldPrimary
                        )
                    }

                    // Center pivot dot
                    drawCircle(
                        color = GoldLight,
                        radius = 8f,
                        center = center
                    )
                }

                Text(
                    text = "🕋",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isAlignedWithQibla)
                    "هاتفك متجه الآن نحو الكعبة المشرفة مباشرة ✓"
                else
                    "زاوية القبلة من الشمال: ${qiblaBearingDegrees.toInt()}° • إذا تغير موقعك اضغط «تحديث موقعي»",
                color = if (isAlignedWithQibla) TealHighlight else GoldLight,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PrayerTimePill(
    name: String,
    time: String,
    isNext: Boolean
) {
    Surface(
        color = if (isNext) GoldPrimary else EmeraldElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNext) EmeraldDeep else GoldLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNext) EmeraldDeep else GoldPrimary
            )
        }
    }
}

@Composable
private fun ZekrInteractiveCard(
    zekr: ZekrItem,
    currentCount: Int,
    onTapCount: () -> Unit,
    onReset: () -> Unit
) {
    val isDone = currentCount >= zekr.targetCount

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) EmeraldElevated else EmeraldCard
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isDone) TealHighlight else GoldPrimary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onTapCount() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = zekr.arabicText,
                fontFamily = AmiriFontFamily,
                fontSize = 22.sp,
                lineHeight = 38.sp,
                color = GoldLight,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الفضل: ${zekr.virtue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTapCount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) TealHighlight else GoldPrimary,
                        contentColor = EmeraldDeep
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isDone) "تم بحمد الله ($currentCount / ${zekr.targetCount})"
                        else "اضغط للعد ($currentCount / ${zekr.targetCount})",
                        fontWeight = FontWeight.Bold
                    )
                }
                if (currentCount > 0) {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة العداد", tint = GoldLight)
                    }
                }
            }
        }
    }
}
