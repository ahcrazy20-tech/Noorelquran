package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AdhkarLog
import com.example.data.model.KhatmahPlan
import com.example.data.model.QuranBookmark
import com.example.data.model.RecitationSession
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Entity(tableName = "local_khatmah_plans")
data class LocalKhatmahEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetDays: Long,
    val completedPages: Long,
    val currentSurahNumber: Long,
    val currentAyahNumber: Long,
    val recitationMode: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(userId: String = "local_user") = KhatmahPlan(
        id = id,
        userId = userId,
        title = title,
        targetDays = targetDays,
        completedPages = completedPages,
        currentSurahNumber = currentSurahNumber,
        currentAyahNumber = currentAyahNumber,
        recitationMode = recitationMode,
        createdAt = Timestamp(Date(createdAtMillis)),
        updatedAt = Timestamp(Date(updatedAtMillis))
    )
}

@Entity(tableName = "local_bookmarks")
data class LocalBookmarkEntity(
    @PrimaryKey val id: String,
    val surahNumber: Long,
    val surahName: String,
    val ayahNumber: Long,
    val pageNumber: Long,
    val ayahSnippet: String,
    val note: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(userId: String = "local_user") = QuranBookmark(
        id = id,
        userId = userId,
        surahNumber = surahNumber,
        surahName = surahName,
        ayahNumber = ayahNumber,
        pageNumber = pageNumber,
        ayahSnippet = ayahSnippet,
        note = note,
        createdAt = Timestamp(Date(createdAtMillis)),
        updatedAt = Timestamp(Date(createdAtMillis))
    )
}

@Entity(tableName = "local_recitation_sessions")
data class LocalRecitationEntity(
    @PrimaryKey val id: String,
    val surahNumber: Long,
    val surahName: String,
    val fromAyah: Long,
    val toAyah: Long,
    val accuracyScore: Long,
    val mode: String,
    val reciterName: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(userId: String = "local_user") = RecitationSession(
        id = id,
        userId = userId,
        surahNumber = surahNumber,
        surahName = surahName,
        fromAyah = fromAyah,
        toAyah = toAyah,
        accuracyScore = accuracyScore,
        mode = mode,
        reciterName = reciterName,
        createdAt = Timestamp(Date(createdAtMillis)),
        updatedAt = Timestamp(Date(createdAtMillis))
    )
}

@Entity(tableName = "local_adhkar_logs")
data class LocalAdhkarLogEntity(
    @PrimaryKey val id: String,
    val category: String,
    val zekrTitle: String,
    val completedCount: Long,
    val targetCount: Long,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(userId: String = "local_user") = AdhkarLog(
        id = id,
        userId = userId,
        category = category,
        zekrTitle = zekrTitle,
        completedCount = completedCount,
        targetCount = targetCount,
        createdAt = Timestamp(Date(createdAtMillis)),
        updatedAt = Timestamp(Date(createdAtMillis))
    )
}

@Dao
interface NoorLocalDao {
    @Query("SELECT * FROM local_khatmah_plans ORDER BY createdAtMillis DESC")
    fun observeKhatmahPlans(): Flow<List<LocalKhatmahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKhatmahPlan(entity: LocalKhatmahEntity)

    @Query("UPDATE local_khatmah_plans SET completedPages = :pages, currentSurahNumber = :surah, currentAyahNumber = :ayah, updatedAtMillis = :now WHERE id = :id")
    suspend fun updateKhatmahProgress(id: String, pages: Long, surah: Long, ayah: Long, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM local_khatmah_plans WHERE id = :id")
    suspend fun deleteKhatmahPlan(id: String)

    @Query("SELECT * FROM local_bookmarks ORDER BY createdAtMillis DESC")
    fun observeBookmarks(): Flow<List<LocalBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(entity: LocalBookmarkEntity)

    @Query("DELETE FROM local_bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    @Query("SELECT * FROM local_recitation_sessions ORDER BY createdAtMillis DESC")
    fun observeRecitationSessions(): Flow<List<LocalRecitationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecitationSession(entity: LocalRecitationEntity)

    @Query("SELECT * FROM local_adhkar_logs ORDER BY createdAtMillis DESC")
    fun observeAdhkarLogs(): Flow<List<LocalAdhkarLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdhkarLog(entity: LocalAdhkarLogEntity)
}

@Database(
    entities = [
        LocalKhatmahEntity::class,
        LocalBookmarkEntity::class,
        LocalRecitationEntity::class,
        LocalAdhkarLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NoorLocalDatabase : RoomDatabase() {
    abstract fun noorDao(): NoorLocalDao

    companion object {
        @Volatile
        private var INSTANCE: NoorLocalDatabase? = null

        fun getInstance(context: Context): NoorLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoorLocalDatabase::class.java,
                    "noor_almurattil_local.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
