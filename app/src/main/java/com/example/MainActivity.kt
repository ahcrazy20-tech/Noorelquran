package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.NoorLocalDatabase
import com.example.data.remote.QuranAndPrayerService
import com.example.data.repository.NoorFirestoreRepository
import com.example.ui.screens.AiQuranCompanionScreen
import com.example.ui.screens.DedicationDuaScreen
import com.example.ui.screens.MushafScreen
import com.example.ui.screens.PrayerAndAdhkarScreen
import com.example.ui.screens.RecitersAndDownloadsScreen
import com.example.ui.screens.TarteelAndKhatmahScreen
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealHighlight
import com.example.ui.viewmodel.NoorMainViewModel
import com.example.ui.viewmodel.QuranAiCompanionViewModel
import com.example.util.LocationAndQiblaManager
import com.example.util.QuranAudioAndSpeechManager
import com.example.util.configureAppCheck
import com.google.firebase.firestore.FirebaseFirestore

enum class NoorDestination(val labelRes: Int, val icon: ImageVector, val tag: String) {
    MUSHAF(R.string.nav_mushaf, Icons.Default.MenuBook, "nav_mushaf"),
    RECITERS(R.string.nav_reciters, Icons.Default.Headphones, "nav_reciters"),
    TARTEEL_KHATMAH(R.string.nav_tarteel_khatmah, Icons.Default.GraphicEq, "nav_tarteel_khatmah"),
    AI_COMPANION(R.string.nav_ai_companion, Icons.Default.AutoAwesome, "nav_ai_companion"),
    PRAYER_ADHKAR(R.string.nav_prayer_adhkar, Icons.Default.NotificationsActive, "nav_prayer_adhkar")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                NoorRootApp()
            }
        }
    }
}

@Composable
fun NoorRootApp() {
    var showDedicationDuaScreen by rememberSaveable { mutableStateOf(true) }

    if (showDedicationDuaScreen) {
        DedicationDuaScreen(
            onEnterApp = {
                showDedicationDuaScreen = false
            }
        )
    } else {
        NoorMainShell(
            onOpenDedicationDua = {
                showDedicationDuaScreen = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoorMainShell(
    onOpenDedicationDua: () -> Unit
) {
    val mainViewModel: NoorMainViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val localDb = NoorLocalDatabase.getInstance(app)
                NoorMainViewModel(
                    localDao = localDb.noorDao(),
                    cloudRepository = NoorFirestoreRepository(db),
                    quranService = QuranAndPrayerService(app),
                    audioSpeechManager = QuranAudioAndSpeechManager(app),
                    locationManager = LocationAndQiblaManager(app)
                )
            }
        }
    )

    val aiViewModel: QuranAiCompanionViewModel = viewModel()

    var currentTab by rememberSaveable { mutableStateOf(NoorDestination.MUSHAF) }
    var showApkInfoDialog by rememberSaveable { mutableStateOf(false) }
    val statusBanner by mainViewModel.statusBannerMessage.collectAsState()

    if (currentTab != NoorDestination.MUSHAF) {
        BackHandler {
            currentTab = NoorDestination.MUSHAF
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = EmeraldDeep,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldDeep,
                    titleContentColor = GoldPrimary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = EmeraldCard,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable { onOpenDedicationDua() }
                        ) {
                            Text(
                                text = "AhmedSoliman",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onOpenDedicationDua() },
                        modifier = Modifier.testTag("open_dua_screen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "دعاء لصاحب الفكرة والعمل AhmedSoliman",
                            tint = GoldPrimary
                        )
                    }
                    IconButton(
                        onClick = { showApkInfoDialog = true },
                        modifier = Modifier.testTag("apk_compatibility_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "تثبيت APK والتوافق مع الهواتف",
                            tint = TealHighlight
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = EmeraldCard,
                contentColor = GoldLight
            ) {
                NoorDestination.entries.forEach { dest ->
                    val selected = currentTab == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = stringResource(id = dest.labelRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = dest.labelRes),
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDeep,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = GoldLight.copy(alpha = 0.7f),
                            unselectedTextColor = GoldLight.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag(dest.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EmeraldDeep)
        ) {
            AnimatedVisibility(visible = !statusBanner.isNullOrBlank()) {
                Surface(
                    color = EmeraldElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clickable { mainViewModel.clearBannerMessage() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusBanner.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TealHighlight,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    NoorDestination.MUSHAF -> {
                        MushafScreen(
                            viewModel = mainViewModel,
                            onAskAiAboutAyah = { surah, ayah ->
                                currentTab = NoorDestination.AI_COMPANION
                                aiViewModel.analyzeAyahStructured(
                                    surahName = surah.nameArabic,
                                    ayahNumber = ayah.numberInSurah,
                                    ayahText = ayah.textUthmani
                                )
                            }
                        )
                    }
                    NoorDestination.RECITERS -> {
                        RecitersAndDownloadsScreen(viewModel = mainViewModel)
                    }
                    NoorDestination.TARTEEL_KHATMAH -> {
                        TarteelAndKhatmahScreen(viewModel = mainViewModel)
                    }
                    NoorDestination.AI_COMPANION -> {
                        AiQuranCompanionScreen(aiViewModel = aiViewModel)
                    }
                    NoorDestination.PRAYER_ADHKAR -> {
                        PrayerAndAdhkarScreen(viewModel = mainViewModel)
                    }
                }
            }
        }
    }

    if (showApkInfoDialog) {
        AlertDialog(
            onDismissRequest = { showApkInfoDialog = false },
            containerColor = EmeraldCard,
            title = {
                Text(
                    text = "تحميل وتثبيت التطبيق (APK) والتوافق",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• متوافق بالكامل مع أحدث الهواتف (Samsung Galaxy S26 / Android 16 - API 36) وكذلك جميع الهواتف القديمة بدءاً من Android 7.0 (API 24).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldLight
                    )
                    Text(
                        text = "• لتحميل ملف التثبيت المباشر (APK) على هاتفك: اضغط على قائمة الإعدادات / التصدير (Settings / Export) أعلى واجهة AI Studio ثم اختر «Download APK» أو «Build APK» لتنزيل ملف التطبيق الجاهز للتثبيت مباشرة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TealHighlight
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showApkInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = EmeraldDeep
                    )
                ) {
                    Text("حسناً", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
