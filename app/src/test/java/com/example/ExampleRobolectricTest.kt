package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.QuranAudioAndSpeechManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("نور المرتل", appName)
    }

    @Test
    fun `calculateRecitationAccuracy matches Arabic words ignoring diacritics`() {
        val uthmani = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ"
        val spoken = "الحمد لله رب العالمين"
        val score = QuranAudioAndSpeechManager.calculateRecitationAccuracy(uthmani, spoken)
        assertTrue(score >= 95)
    }
}
