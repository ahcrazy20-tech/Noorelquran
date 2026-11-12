package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.data.model.AyahItem
import com.example.data.model.ReciterInfo
import java.io.File

class QuranAudioAndSpeechManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var speechRecognizer: SpeechRecognizer? = null

    fun playAyahAudio(
        reciter: ReciterInfo,
        ayah: AyahItem,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: (String) -> Unit
    ) {
        stopAudio()
        val url = "${reciter.ayahAudioBaseUrl}/${ayah.globalNumber}.mp3"
        try {
            val mp = MediaPlayer()
            mediaPlayer = mp
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(url)
            mp.setOnPreparedListener {
                onPrepared()
                it.start()
            }
            mp.setOnCompletionListener {
                onCompletion()
            }
            mp.setOnErrorListener { _, what, extra ->
                Log.w("QuranAudio", "MediaPlayer error what=$what extra=$extra for $url")
                onError("تعذر تشغيل الصوت من الخادم حالياً (تأكد من الاتصال بالإنترنت)")
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            Log.e("QuranAudio", "Failed to play ayah audio", e)
            onError(e.localizedMessage ?: "تعذر تشغيل الآية الصوتية")
        }
    }

    fun playSurahAudio(
        reciter: ReciterInfo,
        surahNumber: Int,
        localFile: File?,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: (String) -> Unit
    ) {
        stopAudio()
        val source = if (localFile != null && localFile.exists() && localFile.length() > 10_000L) {
            localFile.absolutePath
        } else {
            "${reciter.surahAudioBaseUrl}/$surahNumber.mp3"
        }
        try {
            val mp = MediaPlayer()
            mediaPlayer = mp
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(source)
            mp.setOnPreparedListener {
                onPrepared()
                it.start()
            }
            mp.setOnCompletionListener {
                onCompletion()
            }
            mp.setOnErrorListener { _, what, extra ->
                Log.w("QuranAudio", "Surah MediaPlayer error what=$what extra=$extra")
                onError("تعذر تشغيل السورة الصوتية")
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            Log.e("QuranAudio", "Failed to play surah audio", e)
            onError(e.localizedMessage ?: "تعذر تشغيل السورة")
        }
    }

    fun pauseOrResumeAudio(): Boolean {
        val mp = mediaPlayer ?: return false
        return if (mp.isPlaying) {
            mp.pause()
            false
        } else {
            mp.start()
            true
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    fun startArabicSpeechRecognition(
        onPartialTranscript: (String) -> Unit,
        onFinalTranscript: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopSpeechRecognition()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("خدمة التعرف الصوتي المباشر غير متوفرة على هذا المحاكي — يمكنك استخدام زر التسميع التجريبي أو إدخال التلاوة")
            return
        }
        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التعرف على صوت واضح، حاول الترتيل مرة أخرى"
                        SpeechRecognizer.ERROR_AUDIO -> "تعذر الوصول إلى الميكروفون"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "يرجى منح صلاحية الميكروفون للتسميع الصوتي"
                        else -> "تنبيه الميكروفون ($error) — يمكنك متابعة الترديد أو المحاكاة الصوتية"
                    }
                    onError(msg)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    onFinalTranscript(text)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    if (text.isNotBlank()) {
                        onPartialTranscript(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e("QuranSpeech", "SpeechRecognizer error", e)
            onError(e.localizedMessage ?: "تعذر بدء التعرف الصوتي")
        }
    }

    fun stopSpeechRecognition() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
    }

    fun releaseAll() {
        stopAudio()
        stopSpeechRecognition()
    }

    companion object {
        private val DIACRITICS_REGEX = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")

        fun normalizeArabic(input: String): String {
            return input
                .replace(DIACRITICS_REGEX, "")
                .replace('أ', 'ا')
                .replace('إ', 'ا')
                .replace('آ', 'ا')
                .replace('ٱ', 'ا')
                .replace('ة', 'ه')
                .replace('ى', 'ي')
                .trim()
        }

        fun calculateRecitationAccuracy(expectedUthmani: String, spokenText: String): Int {
            if (spokenText.isBlank()) return 0
            val expectedWords = normalizeArabic(expectedUthmani)
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
            val spokenWords = normalizeArabic(spokenText)
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
            if (expectedWords.isEmpty()) return 100
            var matched = 0
            for (ew in expectedWords) {
                if (spokenWords.any { sw -> sw == ew || sw.contains(ew) || ew.contains(sw) }) {
                    matched++
                }
            }
            return ((matched * 100) / expectedWords.size).coerceIn(15, 100)
        }
    }
}
