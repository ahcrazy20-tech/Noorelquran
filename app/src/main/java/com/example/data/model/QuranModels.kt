package com.example.data.model

data class SurahMeta(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val revelationType: String, // "مكية" or "مدنية"
    val ayahCount: Int,
    val startPage: Int,
    val juzNumber: Int
)

data class AyahItem(
    val numberInSurah: Int,
    val globalNumber: Int,
    val textUthmani: String,
    val page: Int,
    val juz: Int,
    val tafsirMuyassar: String,
    val tafsirJalalayn: String = ""
)

data class ReciterInfo(
    val id: String,
    val nameArabic: String,
    val titleArabic: String,
    val style: String, // "مرتل" / "المصحف المعلم" / "مجود"
    val country: String,
    val surahAudioBaseUrl: String,
    val ayahAudioBaseUrl: String
)

data class DownloadedSurahAudio(
    val reciterId: String,
    val surahNumber: Int,
    val filePath: String,
    val fileSizeBytes: Long
)

data class PrayerTimings(
    val city: String,
    val country: String,
    val hijriDate: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val remainingTimeText: String
)

data class ZekrItem(
    val id: String,
    val category: String,
    val arabicText: String,
    val virtue: String,
    val targetCount: Int
)
