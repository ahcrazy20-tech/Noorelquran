package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuranStaticCatalog
import com.example.data.model.AyahItem
import com.example.data.model.SurahMeta
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MushafBorderGold
import com.example.ui.theme.MushafInkDark
import com.example.ui.theme.MushafParchment
import com.example.ui.theme.TealHighlight
import com.example.ui.viewmodel.FirestoreListState
import com.example.ui.viewmodel.MushafDisplayMode
import com.example.ui.viewmodel.NoorMainViewModel
import com.example.ui.viewmodel.TafsirSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MushafScreen(
    viewModel: NoorMainViewModel,
    onAskAiAboutAyah: (SurahMeta, AyahItem) -> Unit
) {
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val isLoadingSurah by viewModel.isLoadingSurah.collectAsState()
    val displayMode by viewModel.mushafDisplayMode.collectAsState()
    val tafsirSource by viewModel.selectedTafsirSource.collectAsState()
    val currentPageIndex by viewModel.currentPageIndex.collectAsState()
    val selectedReciter by viewModel.selectedReciter.collectAsState()
    val playingAyahNumber by viewModel.playingAyahNumber.collectAsState()
    val isPlayingFullSurah by viewModel.isPlayingFullSurah.collectAsState()
    val shadowingMode by viewModel.shadowingModeEnabled.collectAsState()
    val awaitingShadowRepeat by viewModel.awaitingUserShadowRepeat.collectAsState()
    val bookmarksState by viewModel.bookmarksState.collectAsState()

    var showSurahPicker by remember { mutableStateOf(false) }
    var surahSearchQuery by remember { mutableStateOf("") }
    var bookmarkDialogAyah by remember { mutableStateOf<AyahItem?>(null) }
    var bookmarkNoteInput by remember { mutableStateOf("") }

    val bookmarkedSet = remember(bookmarksState, selectedSurah) {
        val items = (bookmarksState as? FirestoreListState.Success)?.items.orEmpty()
        items.filter { it.surahNumber.toInt() == selectedSurah.number }
            .map { it.ayahNumber.toInt() }
            .toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Surah Header & Reciter Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Surah Picker Button
                    Surface(
                        color = EmeraldElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clickable { showSurahPicker = true }
                            .testTag("open_surah_picker_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${selectedSurah.number}",
                                    color = EmeraldDeep,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "سورة ${selectedSurah.nameArabic}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${selectedSurah.revelationType} • ${selectedSurah.ayahCount} آية • جزء ${selectedSurah.juzNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "اختيار السورة",
                                tint = GoldPrimary
                            )
                        }
                    }

                    // Play / Stop Full Surah Button
                    Button(
                        onClick = {
                            if (isPlayingFullSurah || playingAyahNumber != null) {
                                viewModel.stopAudioPlayback()
                            } else {
                                viewModel.playFullSurah(selectedSurah.number)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingFullSurah || playingAyahNumber != null)
                                MaterialTheme.colorScheme.error
                            else GoldPrimary,
                            contentColor = EmeraldDeep
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("play_stop_surah_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingFullSurah || playingAyahNumber != null)
                                Icons.Default.Stop
                            else Icons.Default.PlayArrow,
                            contentDescription = "تشغيل السورة",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingFullSurah || playingAyahNumber != null) "إيقاف" else "تلاوة السورة",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Switcher: Ayah-by-Ayah vs Page-by-Page Mushaf + Shadowing Toggle
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = displayMode == MushafDisplayMode.AYAH_BY_AYAH,
                        onClick = { viewModel.setMushafDisplayMode(MushafDisplayMode.AYAH_BY_AYAH) },
                        label = { Text("آية بآية + التفسير") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = EmeraldDeep
                        ),
                        modifier = Modifier.testTag("mode_ayah_by_ayah_chip")
                    )
                    FilterChip(
                        selected = displayMode == MushafDisplayMode.PAGE_BY_PAGE,
                        onClick = { viewModel.setMushafDisplayMode(MushafDisplayMode.PAGE_BY_PAGE) },
                        label = { Text("صفحة بصفحة (مصحف)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = EmeraldDeep
                        ),
                        modifier = Modifier.testTag("mode_page_by_page_chip")
                    )
                    FilterChip(
                        selected = shadowingMode,
                        onClick = { viewModel.toggleShadowingMode() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("أردد خلف الشيخ") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealHighlight,
                            selectedLabelColor = EmeraldDeep
                        ),
                        modifier = Modifier.testTag("toggle_shadowing_chip")
                    )
                }

                // Active Sheikh Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "القارئ: ${selectedReciter.nameArabic}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldLight
                        )
                    }

                    // Tafsir Switcher
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (tafsirSource == TafsirSource.MUYASSAR) "التفسير الميسر ✓" else "التفسير الميسر",
                            fontSize = 12.sp,
                            color = if (tafsirSource == TafsirSource.MUYASSAR) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.setTafsirSource(TafsirSource.MUYASSAR) }
                        )
                        Text(text = "•", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(
                            text = if (tafsirSource == TafsirSource.JALALAYN) "الجلالين ✓" else "الجلالين",
                            fontSize = 12.sp,
                            color = if (tafsirSource == TafsirSource.JALALAYN) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.setTafsirSource(TafsirSource.JALALAYN) }
                        )
                    }
                }
            }
        }

        // Shadowing Repeat Prompt Bar when active
        AnimatedVisibility(visible = awaitingShadowRepeat) {
            Card(
                colors = CardDefaults.cardColors(containerColor = TealHighlight.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .border(1.dp, TealHighlight, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "دورك للترديد خلف الشيخ!",
                            color = TealHighlight,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "ردد الآية (${playingAyahNumber ?: 1}) بصوتك بتأنٍّ ثم اضغط للانتقال للآية التالية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldLight
                        )
                    }
                    Button(
                        onClick = { viewModel.completeShadowRepeatAndAdvance() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealHighlight,
                            contentColor = EmeraldDeep
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("أتممت الترديد • التالي", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoadingSurah) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جارٍ مزامنة المصحف والتفسير الكامل لسورة ${selectedSurah.nameArabic}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldLight
                    )
                }
            }
        }

        // Main Content: Ayah-by-Ayah OR Page-by-Page Mushaf
        if (displayMode == MushafDisplayMode.AYAH_BY_AYAH) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("ayah_by_ayah_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (selectedSurah.number != 1 && selectedSurah.number != 9) {
                    item {
                        BasmalahHeaderCard()
                    }
                }
                items(ayahs, key = { it.numberInSurah }) { ayah ->
                    val isPlayingThis = playingAyahNumber == ayah.numberInSurah
                    val isBookmarked = bookmarkedSet.contains(ayah.numberInSurah)
                    AyahWithTafsirCard(
                        surah = selectedSurah,
                        ayah = ayah,
                        tafsirSource = tafsirSource,
                        isPlaying = isPlayingThis,
                        isBookmarked = isBookmarked,
                        onPlayClick = {
                            if (isPlayingThis) {
                                viewModel.stopAudioPlayback()
                            } else {
                                viewModel.playAyah(ayah)
                            }
                        },
                        onBookmarkClick = {
                            bookmarkDialogAyah = ayah
                            bookmarkNoteInput = ""
                        },
                        onAskAiClick = {
                            onAskAiAboutAyah(selectedSurah, ayah)
                        }
                    )
                }
            }
        } else {
            // PAGE-BY-PAGE MUSHAF VIEW
            val pagesMap = remember(ayahs) {
                ayahs.groupBy { it.page }.toSortedMap()
            }
            val pageNumbers = remember(pagesMap) { pagesMap.keys.toList() }
            val safeIndex = currentPageIndex.coerceIn(0, (pageNumbers.size - 1).coerceAtLeast(0))
            val activePageNum = pageNumbers.getOrElse(safeIndex) { selectedSurah.startPage }
            val pageAyahs = pagesMap[activePageNum] ?: ayahs

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp)
            ) {
                // Illuminated Parchment Mushaf Page Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MushafParchment),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(2.5.dp, MushafBorderGold, RoundedCornerShape(22.dp))
                        .testTag("mushaf_page_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        // Top Mushaf Page Frame Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MushafBorderGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سُورَةُ ${selectedSurah.nameArabic}",
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MushafInkDark
                            )
                            Text(
                                text = "صَفْحَة $activePageNum",
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MushafBorderGold
                            )
                            Text(
                                text = "الجزء ${pageAyahs.firstOrNull()?.juz ?: selectedSurah.juzNumber}",
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MushafInkDark
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                val combinedPageText = buildString {
                                    pageAyahs.forEach { a ->
                                        append(a.textUthmani)
                                        append(" ﴿${a.numberInSurah}﴾ ")
                                    }
                                }
                                Text(
                                    text = combinedPageText,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 27.sp,
                                    lineHeight = 50.sp,
                                    color = MushafInkDark,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MushafBorderGold.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "تفسير آيات الصفحة ($activePageNum):",
                                            fontWeight = FontWeight.Bold,
                                            color = MushafInkDark,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        pageAyahs.forEach { a ->
                                            val tafsirText = if (tafsirSource == TafsirSource.MUYASSAR)
                                                a.tafsirMuyassar
                                            else a.tafsirJalalayn.ifBlank { a.tafsirMuyassar }
                                            Text(
                                                text = "﴿${a.numberInSurah}﴾ $tafsirText",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MushafInkDark.copy(alpha = 0.85f),
                                                modifier = Modifier.padding(vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Page Turn Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setCurrentPageIndex((safeIndex - 1).coerceAtLeast(0)) },
                        enabled = safeIndex > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "الصفحة السابقة")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الصفحة السابقة")
                    }

                    Text(
                        text = "صفحة ${safeIndex + 1} من ${pageNumbers.size.coerceAtLeast(1)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight
                    )

                    OutlinedButton(
                        onClick = {
                            if (safeIndex + 1 < pageNumbers.size) {
                                viewModel.setCurrentPageIndex(safeIndex + 1)
                            }
                        },
                        enabled = safeIndex + 1 < pageNumbers.size,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("الصفحة التالية")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "الصفحة التالية")
                    }
                }
            }
        }
    }

    // Surah Picker ModalBottomSheet
    if (showSurahPicker) {
        ModalBottomSheet(
            onDismissRequest = { showSurahPicker = false },
            containerColor = EmeraldCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "فهرس سور القرآن الكريم (١١٤ سورة)",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = surahSearchQuery,
                    onValueChange = { surahSearchQuery = it },
                    placeholder = { Text("ابحث باسم السورة أو رقمها...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                val filteredSurahs = remember(surahSearchQuery) {
                    if (surahSearchQuery.isBlank()) {
                        QuranStaticCatalog.allSurahs
                    } else {
                        QuranStaticCatalog.allSurahs.filter {
                            it.nameArabic.contains(surahSearchQuery.trim()) ||
                                it.nameEnglish.contains(surahSearchQuery.trim(), ignoreCase = true) ||
                                it.number.toString() == surahSearchQuery.trim()
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSurahs, key = { it.number }) { s ->
                        Surface(
                            color = if (s.number == selectedSurah.number)
                                GoldPrimary.copy(alpha = 0.22f)
                            else EmeraldElevated,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectSurah(s)
                                    showSurahPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${s.number}",
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "سورة ${s.nameArabic}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${s.revelationType} • ${s.ayahCount} آية • ص ${s.startPage}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "جزء ${s.juzNumber}",
                                    color = GoldPrimary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bookmark Note Dialog
    bookmarkDialogAyah?.let { targetAyah ->
        AlertDialog(
            onDismissRequest = { bookmarkDialogAyah = null },
            containerColor = EmeraldCard,
            title = {
                Text(
                    text = "حفظ علامة مرجعية — سورة ${selectedSurah.nameArabic} ﴿${targetAyah.numberInSurah}﴾",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = targetAyah.textUthmani,
                        fontFamily = AmiriFontFamily,
                        fontSize = 20.sp,
                        color = GoldLight,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = bookmarkNoteInput,
                        onValueChange = { bookmarkNoteInput = it },
                        label = { Text("خاطرة تدبرية أو ملاحظة حفظ (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveAyahBookmark(targetAyah, bookmarkNoteInput)
                        bookmarkDialogAyah = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = EmeraldDeep
                    )
                ) {
                    Text("حفظ الفاصلة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookmarkDialogAyah = null }) {
                    Text("إلغاء", color = GoldLight)
                }
            }
        )
    }
}

@Composable
private fun BasmalahHeaderCard() {
    Surface(
        color = EmeraldElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            fontFamily = AmiriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = GoldLight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun AyahWithTafsirCard(
    surah: SurahMeta,
    ayah: AyahItem,
    tafsirSource: TafsirSource,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onAskAiClick: () -> Unit
) {
    var showTafsir by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) EmeraldElevated else EmeraldCard
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPlaying) 2.dp else 1.dp,
                color = if (isPlaying) TealHighlight else GoldPrimary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) TealHighlight else GoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${ayah.numberInSurah}",
                            color = if (isPlaying) EmeraldDeep else GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ص ${ayah.page} • جزء ${ayah.juz}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPlayClick) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "استماع للآية",
                            tint = if (isPlaying) TealHighlight else GoldPrimary
                        )
                    }
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ الآية",
                            tint = GoldPrimary
                        )
                    }
                    IconButton(onClick = onAskAiClick) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "تدبر وتجويد الآية بالذكاء الاصطناعي",
                            tint = TealHighlight
                        )
                    }
                    IconButton(onClick = { showTafsir = !showTafsir }) {
                        Icon(
                            imageVector = if (showTafsir) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "إظهار أو إخفاء التفسير",
                            tint = GoldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Uthmani Quranic Verse Text
            Text(
                text = "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾",
                fontFamily = AmiriFontFamily,
                fontSize = 26.sp,
                lineHeight = 46.sp,
                color = if (isPlaying) TealHighlight else GoldLight,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = showTafsir) {
                val tafsirText = if (tafsirSource == TafsirSource.MUYASSAR) {
                    ayah.tafsirMuyassar
                } else {
                    ayah.tafsirJalalayn.ifBlank { ayah.tafsirMuyassar }
                }
                val tafsirTitle = if (tafsirSource == TafsirSource.MUYASSAR) "التفسير الميسر:" else "تفسير الجلالين:"

                Surface(
                    color = EmeraldDeep.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tafsirTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldPrimary
                            )
                            Text(
                                text = "اطلب إعراب وتجويد الآية بالـ AI ←",
                                fontSize = 11.sp,
                                color = TealHighlight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onAskAiClick() }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tafsirText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}
