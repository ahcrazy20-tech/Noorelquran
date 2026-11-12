package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuranStaticCatalog
import com.example.data.model.ReciterInfo
import com.example.data.model.SurahMeta
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealHighlight
import com.example.ui.viewmodel.NoorMainViewModel

@Composable
fun RecitersAndDownloadsScreen(
    viewModel: NoorMainViewModel
) {
    val selectedReciter by viewModel.selectedReciter.collectAsState()
    val downloadedSurahs by viewModel.downloadedSurahs.collectAsState()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val downloadedForSelectedReciter = remember(downloadedSurahs, selectedReciter) {
        downloadedSurahs.filter { it.reciterId == selectedReciter.id }
            .associateBy { it.surahNumber }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "أشهر قراء العالم الإسلامي والتحميل بدون إنترنت",
            style = MaterialTheme.typography.titleLarge,
            color = GoldPrimary
        )
        Text(
            text = "اختر قارئك المفضل واستمع مباشرة أو نزّل السور كاملة على جهازك",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Carousel of World-Famous Sheikhs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("world_reciters_row")
        ) {
            items(QuranStaticCatalog.worldReciters, key = { it.id }) { reciter ->
                val isSelected = reciter.id == selectedReciter.id
                ReciterCard(
                    reciter = reciter,
                    isSelected = isSelected,
                    downloadedCount = downloadedSurahs.count { it.reciterId == reciter.id },
                    onSelect = { viewModel.selectReciter(reciter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search & Filter Surahs to Download or Play
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن سورة لتحميلها أو تشغيلها بصوت ${selectedReciter.nameArabic}...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reciter_surah_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        val filteredSurahs = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                QuranStaticCatalog.allSurahs
            } else {
                QuranStaticCatalog.allSurahs.filter {
                    it.nameArabic.contains(searchQuery.trim()) ||
                        it.number.toString() == searchQuery.trim()
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredSurahs, key = { it.number }) { surah ->
                val progressKey = "${selectedReciter.id}_${surah.number}"
                val activeProgress = downloadProgressMap[progressKey]
                val downloadedInfo = downloadedForSelectedReciter[surah.number]

                SurahDownloadItemCard(
                    surah = surah,
                    reciter = selectedReciter,
                    isDownloaded = downloadedInfo != null,
                    fileSizeKb = (downloadedInfo?.fileSizeBytes ?: 0L) / 1024L,
                    downloadProgress = activeProgress,
                    onPlayClick = {
                        viewModel.selectSurah(surah)
                        viewModel.playFullSurah(surah.number)
                    },
                    onDownloadClick = {
                        viewModel.downloadSurahForOffline(selectedReciter, surah)
                    },
                    onDeleteOfflineClick = {
                        viewModel.deleteOfflineSurah(selectedReciter.id, surah.number)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReciterCard(
    reciter: ReciterInfo,
    isSelected: Boolean,
    downloadedCount: Int,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EmeraldElevated else EmeraldCard
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .width(195.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = if (isSelected) EmeraldDeep else GoldPrimary
                    )
                }
                Surface(
                    color = EmeraldDeep.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = reciter.country,
                        fontSize = 11.sp,
                        color = GoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = reciter.nameArabic,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) GoldPrimary else GoldLight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = reciter.titleArabic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (downloadedCount > 0) "محمّل: $downloadedCount سورة" else reciter.style,
                fontSize = 11.sp,
                color = if (downloadedCount > 0) TealHighlight else GoldPrimary
            )
        }
    }
}

@Composable
private fun SurahDownloadItemCard(
    surah: SurahMeta,
    reciter: ReciterInfo,
    isDownloaded: Boolean,
    fileSizeKb: Long,
    downloadProgress: Int?,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteOfflineClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "سورة ${surah.nameArabic}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    val subText = if (isDownloaded) {
                        "محمّلة بدون إنترنت (${(fileSizeKb / 1024f).let { String.format("%.1f", it) }} MB) • ${reciter.nameArabic}"
                    } else {
                        "${surah.revelationType} • ${surah.ayahCount} آية • بصوت ${reciter.nameArabic}"
                    }
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDownloaded) TealHighlight else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل السورة",
                        tint = GoldPrimary
                    )
                }

                when {
                    downloadProgress != null -> {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = TealHighlight,
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "$downloadProgress%",
                                fontSize = 9.sp,
                                color = GoldLight
                            )
                        }
                    }
                    isDownloaded -> {
                        IconButton(onClick = onDeleteOfflineClick) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "حذف التحميل",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "محمّلة",
                            tint = TealHighlight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    else -> {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.testTag("download_surah_${surah.number}_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "تنزيل السورة",
                                tint = GoldLight
                            )
                        }
                    }
                }
            }
        }
    }
}
