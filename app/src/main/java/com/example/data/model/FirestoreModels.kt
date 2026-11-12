package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class KhatmahPlan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "ختمة التدبر والترتيل",
    val targetDays: Long = 30L,
    val completedPages: Long = 0L,
    val currentSurahNumber: Long = 1L,
    val currentAyahNumber: Long = 1L,
    val recitationMode: String = "shadowing",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class QuranBookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val surahNumber: Long = 1L,
    val surahName: String = "الفاتحة",
    val ayahNumber: Long = 1L,
    val pageNumber: Long = 1L,
    val ayahSnippet: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RecitationSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val surahNumber: Long = 1L,
    val surahName: String = "الفاتحة",
    val fromAyah: Long = 1L,
    val toAyah: Long = 7L,
    val accuracyScore: Long = 95L,
    val mode: String = "ترديد خلف الشيخ",
    val reciterName: String = "مشاري راشد العفاسي",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdhkarLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "أذكار الصباح",
    val zekrTitle: String = "سبحان الله وبحمده",
    val completedCount: Long = 33L,
    val targetCount: Long = 33L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
