package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoorFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveKhatmahPlan_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = NoorFirestoreRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createKhatmahPlan(
                title = "ختمة الترتيل الشهرية",
                targetDays = 30L,
                recitationMode = "shadowing"
            )
        }
        assertTrue(createResult.isSuccess)
        val planId = createResult.getOrThrow()

        val plans = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeKhatmahPlans(aliceUid).first { list -> list.any { it.id == planId } }
        }
        assertTrue(plans.any { it.id == planId })
    }

    @Test
    fun getKhatmahPlanById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = NoorFirestoreRepository(firestore)
        val planId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createKhatmahPlan("ختمة خاصة", 30L, "reading").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = NoorFirestoreRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getKhatmahPlanById(aliceUid, planId)
        }
        assertTrue(result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeKhatmahPlans_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = NoorFirestoreRepository(firestore)

        var caughtException: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeKhatmahPlans(aliceUid).first()
            }
        } catch (e: Throwable) {
            caughtException = e
        }
        assertNotNull(caughtException)
        val firestoreException = checkNotNull(caughtException?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $caughtException"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@noor.test"
        const val BOB_EMAIL = "bob@noor.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
