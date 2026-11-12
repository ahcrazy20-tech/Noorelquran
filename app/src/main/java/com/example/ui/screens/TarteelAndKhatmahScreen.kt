package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.KhatmahPlan
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealHighlight
import com.example.ui.viewmodel.FirestoreListState
import com.example.ui.viewmodel.NoorMainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarteelAndKhatmahScreen(
    viewModel: NoorMainViewModel
) {
    val context = LocalContext.current
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val selectedReciter by viewModel.selectedReciter.collectAsState()
    val hideWords by viewModel.hideWordsForMemorization.collectAsState()
    val isListeningMic by viewModel.isListeningMic.collectAsState()
    val spokenTranscript by viewModel.liveSpokenTranscript.collectAsState()
    val lastAccuracy by viewModel.lastRecitationAccuracy.collectAsState()
    val shadowingMode by viewModel.shadowingModeEnabled.collectAsState()
    val khatmahPlansState by viewModel.khatmahPlansState.collectAsState()
    val sessionsState by viewModel.recitationSessionsState.collectAsState()
    val bookmarksState by viewModel.bookmarksState.collectAsState()

    var currentPracticeAyahIndex by remember(ayahs) { mutableIntStateOf(0) }
    val activeAyah = ayahs.getOrElse(currentPracticeAyahIndex.coerceIn(0, (ayahs.size - 1).coerceAtLeast(0))) {
        ayahs.first()
    }

    var showCreateKhatmahDialog by remember { mutableStateOf(false) }
    var newKhatmahTitle by remember { mutableStateOf("ختمة التدبر والترتيل الشهرية") }
    var newKhatmahDays by remember { mutableStateOf("30") }
    var newKhatmahMode by remember { mutableStateOf("shadowing") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startLiveVoiceRecitation(activeAyah)
        } else {
            viewModel.evaluateManualOrSimulatedRecitation(activeAyah, activeAyah.textUthmani)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        // 1. LIVE TARTEEL VOICE RECITATION & SHEIKH SHADOWING STUDIO
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TealHighlight.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = TealHighlight
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "المقرأة الصوتية الذكية (زي ترتيل)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "سورة ${selectedSurah.nameArabic} • الآية (${activeAyah.numberInSurah}/${ayahs.size}) • ${selectedReciter.nameArabic}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleHideWordsForMemorization() },
                            modifier = Modifier.testTag("toggle_hide_words_button")
                        ) {
                            Icon(
                                imageVector = if (hideWords) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "إخفاء الكلمات لاختبار الحفظ",
                                tint = if (hideWords) TealHighlight else GoldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verse display box (supports Memorization Masked Words mode!)
                    Surface(
                        color = EmeraldDeep.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val displayedVerse = if (hideWords && !isListeningMic && spokenTranscript.isBlank()) {
                                val words = activeAyah.textUthmani.split(" ")
                                words.mapIndexed { idx, w ->
                                    if (idx == 0 || idx == words.lastIndex) w else "••••"
                                }.joinToString(" ")
                            } else {
                                activeAyah.textUthmani
                            }

                            Text(
                                text = "$displayedVerse ﴿${activeAyah.numberInSurah}﴾",
                                fontFamily = AmiriFontFamily,
                                fontSize = 26.sp,
                                lineHeight = 46.sp,
                                color = GoldLight,
                                textAlign = TextAlign.Center
                            )

                            if (hideWords) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "وضع اختبار الحفظ الغيبي مفعل (اضغط رمز العين لإظهار الكلمات)",
                                    fontSize = 11.sp,
                                    color = TealHighlight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Spoken Transcript & Accuracy Result Badge
                    if (spokenTranscript.isNotBlank() || lastAccuracy != null) {
                        Surface(
                            color = EmeraldElevated,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "قراءتك المسجلة:",
                                        fontSize = 11.sp,
                                        color = GoldPrimary
                                    )
                                    Text(
                                        text = spokenTranscript.ifBlank { activeAyah.textUthmani },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GoldLight
                                    )
                                }
                                lastAccuracy?.let { score ->
                                    Surface(
                                        color = if (score >= 80) TealHighlight.copy(alpha = 0.2f) else GoldPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "إتقان $score%",
                                            color = if (score >= 80) TealHighlight else GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Action Buttons: Listen to Sheikh & Repeat, or Live Voice Tarteel
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isListeningMic) {
                                    viewModel.stopLiveVoiceRecitation()
                                } else {
                                    val hasPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        viewModel.startLiveVoiceRecitation(activeAyah)
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListeningMic) MaterialTheme.colorScheme.error else TealHighlight,
                                contentColor = EmeraldDeep
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("start_voice_tarteel_button")
                        ) {
                            Icon(
                                imageVector = if (isListeningMic) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isListeningMic) "إيقاف الميكروفون" else "تسميع بالصوت (مباشر)",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (!shadowingMode) viewModel.toggleShadowingMode()
                                viewModel.playAyah(activeAyah)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = EmeraldDeep
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("listen_and_repeat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استمع للشيخ وردد خلفه", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.evaluateManualOrSimulatedRecitation(activeAyah, activeAyah.textUthmani)
                                if (currentPracticeAyahIndex + 1 < ayahs.size) {
                                    currentPracticeAyahIndex++
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("complete_ayah_recitation_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TealHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أتممت قراءة الآية • التالية", color = GoldLight)
                        }
                    }
                }
            }
        }

        // 2. KHATMAH TRACKER SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "متتبع الختمات القرآنية",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary
                    )
                    Text(
                        text = "تتبع ختمتك بالصفحات والأجزاء بالتلاوة أو الترديد الصوتي",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateKhatmahDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = EmeraldDeep
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("new_khatmah_plan_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ختمة جديدة", fontWeight = FontWeight.Bold)
                }
            }
        }

        when (val state = khatmahPlansState) {
            is FirestoreListState.Loading -> {
                item {
                    Text(
                        text = "جارٍ تحميل خطط الختمة المحفوظة...",
                        color = GoldLight,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            is FirestoreListState.Error -> {
                item {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            is FirestoreListState.Success -> {
                if (state.items.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "لم تبدأ خطة ختمة بعد",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "أنشئ ختمتك الآن وحدد عدد الأيام (مثلاً ٣٠ يوماً = جزء يومياً / ٢٠ صفحة) وسنتتبع تقدمك تلقائياً بالصوت والقراءة.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.createNewKhatmahPlan(
                                            title = "ختمة الترتيل الشهرية",
                                            targetDays = 30,
                                            recitationMode = "shadowing"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = EmeraldDeep
                                    )
                                ) {
                                    Text("ابدأ ختمة ٣٠ يوماً الآن", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(state.items, key = { it.id }) { plan ->
                        KhatmahPlanCard(
                            plan = plan,
                            onAdvancePages = { delta -> viewModel.advanceKhatmahProgress(plan, delta) },
                            onDelete = { viewModel.deleteKhatmahPlan(plan.id) }
                        )
                    }
                }
            }
        }

        // 3. RECENT VOICE RECITATION SESSIONS & BOOKMARKS
        item {
            Text(
                text = "سجل جلسات التسميع والفواصل المرجعية",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        val sessions = (sessionsState as? FirestoreListState.Success)?.items.orEmpty()
        if (sessions.isNotEmpty()) {
            items(sessions.take(5), key = { it.id }) { session ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${session.surahName} (الآيات ${session.fromAyah} - ${session.toAyah})",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${session.mode} • الشيخ ${session.reciterName}",
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
                                text = "إتقان ${session.accuracyScore}%",
                                color = TealHighlight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        val bookmarks = (bookmarksState as? FirestoreListState.Success)?.items.orEmpty()
        if (bookmarks.isNotEmpty()) {
            items(bookmarks.take(5), key = { it.id }) { bm ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldElevated),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "فاصلة: سورة ${bm.surahName} ﴿${bm.ayahNumber}﴾ • ص ${bm.pageNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bm.ayahSnippet,
                                fontFamily = AmiriFontFamily,
                                fontSize = 18.sp,
                                color = GoldLight,
                                maxLines = 1
                            )
                            if (bm.note.isNotBlank()) {
                                Text(
                                    text = "ملاحظة: ${bm.note}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TealHighlight,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.removeBookmark(bm.id) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "حذف الفاصلة",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateKhatmahDialog) {
        AlertDialog(
            onDismissRequest = { showCreateKhatmahDialog = false },
            containerColor = EmeraldCard,
            title = {
                Text("إنشاء خطة ختمة جديدة", color = GoldPrimary, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newKhatmahTitle,
                        onValueChange = { newKhatmahTitle = it },
                        label = { Text("اسم الختمة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newKhatmahDays,
                        onValueChange = { newKhatmahDays = it.filter { c -> c.isDigit() } },
                        label = { Text("المدة المستهدفة بالأيام (مثلاً 30 أو 15 أو 60)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("أسلوب الختمة المفضل:", color = GoldLight, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = newKhatmahMode == "shadowing",
                            onClick = { newKhatmahMode = "shadowing" },
                            label = { Text("ترديد بالصوت") }
                        )
                        FilterChip(
                            selected = newKhatmahMode == "reading",
                            onClick = { newKhatmahMode = "reading" },
                            label = { Text("قراءة وتدبر") }
                        )
                        FilterChip(
                            selected = newKhatmahMode == "listening",
                            onClick = { newKhatmahMode = "listening" },
                            label = { Text("استماع") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = newKhatmahDays.toIntOrNull()?.coerceIn(1, 365) ?: 30
                        viewModel.createNewKhatmahPlan(
                            title = newKhatmahTitle,
                            targetDays = days,
                            recitationMode = newKhatmahMode
                        )
                        showCreateKhatmahDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = EmeraldDeep
                    )
                ) {
                    Text("حفظ وبدء الختمة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateKhatmahDialog = false }) {
                    Text("إلغاء", color = GoldLight)
                }
            }
        )
    }
}

@Composable
private fun KhatmahPlanCard(
    plan: KhatmahPlan,
    onAdvancePages: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val progressFraction = (plan.completedPages.toFloat() / 604f).coerceIn(0f, 1f)
    val percent = (progressFraction * 100).toInt()
    val dailyPagesTarget = (604 / plan.targetDays.coerceAtLeast(1L)).coerceAtLeast(1L)
    val currentJuz = ((plan.completedPages / 20L) + 1L).coerceIn(1L, 30L)

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    val modeArabic = when (plan.recitationMode) {
                        "shadowing" -> "ترديد وتسميع صوتي"
                        "listening" -> "استماع وتدبر"
                        else -> "قراءة بالمصحف"
                    }
                    Text(
                        text = "$modeArabic • الورد اليومي: $dailyPagesTarget صفحة (${plan.targetDays} يوماً)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف الختمة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "أنجزت: ${plan.completedPages} من 604 صفحة ($percent%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldLight,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "الجزء الحالي: $currentJuz من 30",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TealHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAdvancePages(1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldElevated,
                        contentColor = GoldLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+١ صفحة")
                }
                Button(
                    onClick = { onAdvancePages(5) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldElevated,
                        contentColor = GoldLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+٥ صفحات (حزب/٢)")
                }
                Button(
                    onClick = { onAdvancePages(20) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = EmeraldDeep
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text("أتممت جزءاً (+٢٠ ص)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
