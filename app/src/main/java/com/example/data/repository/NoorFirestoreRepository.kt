package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AdhkarLog
import com.example.data.model.KhatmahPlan
import com.example.data.model.QuranBookmark
import com.example.data.model.RecitationSession
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NoorFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw FirebaseFirestoreException(
                "User must be signed in with Google before accessing Firestore.",
                FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
    }

    // --- KHATMAH PLANS ---

    fun observeKhatmahPlans(userId: String = requireUserId()): Flow<List<KhatmahPlan>> = flow {
        val path = "users/$userId/khatmah_plans"
        emitAll(
            db.collection("users").document(userId).collection("khatmah_plans")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        KhatmahPlan::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createKhatmahPlan(
        title: String,
        targetDays: Long,
        recitationMode: String,
        currentSurahNumber: Long = 1L,
        currentAyahNumber: Long = 1L,
        completedPages: Long = 0L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("khatmah_plans").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(100).ifEmpty { "ختمة مباركة" },
            "targetDays" to targetDays.coerceIn(1L, 365L),
            "completedPages" to completedPages.coerceIn(0L, 604L),
            "currentSurahNumber" to currentSurahNumber.coerceIn(1L, 114L),
            "currentAyahNumber" to currentAyahNumber.coerceIn(1L, 286L),
            "recitationMode" to if (recitationMode in listOf("reading", "listening", "shadowing")) recitationMode else "shadowing",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getKhatmahPlanById(targetUserId: String, planId: String): Result<KhatmahPlan?> = runCatching {
        val docRef = db.collection("users").document(targetUserId).collection("khatmah_plans").document(planId)
        try {
            val snap = docRef.get().await()
            snap.toObject(KhatmahPlan::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun updateKhatmahProgress(
        planId: String,
        completedPages: Long,
        currentSurahNumber: Long,
        currentAyahNumber: Long
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("khatmah_plans").document(planId)
        val updates = mapOf(
            "completedPages" to completedPages.coerceIn(0L, 604L),
            "currentSurahNumber" to currentSurahNumber.coerceIn(1L, 114L),
            "currentAyahNumber" to currentAyahNumber.coerceIn(1L, 286L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteKhatmahPlan(planId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("khatmah_plans").document(planId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- BOOKMARKS ---

    fun observeBookmarks(userId: String = requireUserId()): Flow<List<QuranBookmark>> = flow {
        val path = "users/$userId/bookmarks"
        emitAll(
            db.collection("users").document(userId).collection("bookmarks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        QuranBookmark::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addBookmark(
        surahNumber: Long,
        surahName: String,
        ayahNumber: Long,
        pageNumber: Long,
        ayahSnippet: String,
        note: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document()
        val payload = mapOf(
            "userId" to uid,
            "surahNumber" to surahNumber.coerceIn(1L, 114L),
            "surahName" to surahName.trim().take(80).ifEmpty { "الفاتحة" },
            "ayahNumber" to ayahNumber.coerceIn(1L, 286L),
            "pageNumber" to pageNumber.coerceIn(1L, 604L),
            "ayahSnippet" to ayahSnippet.trim().take(1000).ifEmpty { "آية كريمة" },
            "note" to note.trim().take(500),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBookmark(bookmarkId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(bookmarkId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- RECITATION SESSIONS ---

    fun observeRecitationSessions(userId: String = requireUserId()): Flow<List<RecitationSession>> = flow {
        val path = "users/$userId/recitation_sessions"
        emitAll(
            db.collection("users").document(userId).collection("recitation_sessions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        RecitationSession::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun logRecitationSession(
        surahNumber: Long,
        surahName: String,
        fromAyah: Long,
        toAyah: Long,
        accuracyScore: Long,
        mode: String,
        reciterName: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val safeFrom = fromAyah.coerceIn(1L, 286L)
        val safeTo = toAyah.coerceIn(safeFrom, 286L)
        val docRef = db.collection("users").document(uid).collection("recitation_sessions").document()
        val payload = mapOf(
            "userId" to uid,
            "surahNumber" to surahNumber.coerceIn(1L, 114L),
            "surahName" to surahName.trim().take(80).ifEmpty { "الفاتحة" },
            "fromAyah" to safeFrom,
            "toAyah" to safeTo,
            "accuracyScore" to accuracyScore.coerceIn(0L, 100L),
            "mode" to mode.trim().take(40).ifEmpty { "تسميع صوتي" },
            "reciterName" to reciterName.trim().take(100).ifEmpty { "مشاري راشد العفاسي" },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- ADHKAR LOGS ---

    fun observeAdhkarLogs(userId: String = requireUserId()): Flow<List<AdhkarLog>> = flow {
        val path = "users/$userId/adhkar_logs"
        emitAll(
            db.collection("users").document(userId).collection("adhkar_logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        AdhkarLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun logAdhkarCompletion(
        category: String,
        zekrTitle: String,
        completedCount: Long,
        targetCount: Long
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("adhkar_logs").document()
        val payload = mapOf(
            "userId" to uid,
            "category" to category.trim().take(80).ifEmpty { "أذكار الصباح" },
            "zekrTitle" to zekrTitle.trim().take(200).ifEmpty { "سبحان الله وبحمده" },
            "completedCount" to completedCount.coerceIn(1L, 10000L),
            "targetCount" to targetCount.coerceIn(1L, 10000L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }
}
