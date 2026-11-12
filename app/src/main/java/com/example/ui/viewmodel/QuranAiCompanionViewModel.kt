package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "QuranAiCompanionVM"

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

data class StructuredAyahInsight(
    val ayahReference: String,
    val coreMeaning: String,
    val tajweedRules: List<String>,
    val practicalAction: String,
    val memorizationTip: String
)

sealed interface AiInsightState {
    data object Idle : AiInsightState
    data object Loading : AiInsightState
    data class Success(val insight: StructuredAyahInsight) : AiInsightState
    data class Error(val message: String) : AiInsightState
}

class QuranAiCompanionViewModel : ViewModel() {

    private val systemPrompt = """
        أنت "نور المرتل"، عالم ومعلّم قرآني ذكي ومتخصص في علوم القرآن الكريم، التفسير الميسر والتحليلي، أحكام التجويد ومخارج الحروف، القراءات العشر، وتثبيت الحفظ والختمات.
        تحدث بلغة عربية فصيحة، دافئة، واضحة، ومنظمة بالنقاط، مع الاستشهاد بالآيات الكريمة بدقة وذكر اسم السورة ورقم الآية.
    """.trimIndent()

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content { text(systemPrompt) }
        )
    }

    private val structuredInsightSchema = Schema.obj(
        properties = mapOf(
            "ayahReference" to Schema.string(description = "اسم السورة ورقم الآية"),
            "coreMeaning" to Schema.string(description = "التفسير والتدبر الإيماني العميق للآية"),
            "tajweedRules" to Schema.array(
                items = Schema.string(),
                description = "أبرز أحكام التجويد ومخارج الحروف في هذه الآية"
            ),
            "practicalAction" to Schema.string(description = "كيف نطبق هذه الآية عملياً في حياتنا اليومية"),
            "memorizationTip" to Schema.string(description = "نصيحة ذهبية لربط الآية وتثبيت حفظها"
            )
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = structuredInsightSchema
            }
        )
    }

    private val _messages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "welcome",
                isUser = false,
                text = "السلام عليكم ورحمة الله وبركاته! أنا رفيقك القرآني الذكي في **نور المرتل**.\n\nيمكنك سؤالي مباشرة عن:\n• **تفسير وتدبر** أي آية أو سورة في القرآن الكريم.\n• **أحكام التجويد** (الإخفاء، الإدغام، المدود، القلقلة) وكيفية نطق الآيات.\n• **خطة حفظ ومراجعة** مخصصة أو تثبيت المتشابهات القرآنية.\n• **البحث الموضوعي** عن الآيات التي تتحدث عن الصبر، الرزق، السكينة، أو الدعاء."
            )
        )
    )
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _isGeneratingChat = MutableStateFlow(false)
    val isGeneratingChat: StateFlow<Boolean> = _isGeneratingChat.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _insightState = MutableStateFlow<AiInsightState>(AiInsightState.Idle)
    val insightState: StateFlow<AiInsightState> = _insightState.asStateFlow()

    fun sendChatMessage(userPrompt: String) {
        val cleanPrompt = userPrompt.trim()
        if (cleanPrompt.isEmpty() || _isGeneratingChat.value) return

        _chatError.value = null
        val userMsg = AiChatMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = cleanPrompt
        )
        val assistantMsgId = "a_${System.currentTimeMillis()}"
        val placeholderMsg = AiChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "",
            isStreaming = true
        )
        _messages.value = _messages.value + userMsg + placeholderMsg
        _isGeneratingChat.value = true

        viewModelScope.launch {
            val accumulatedText = StringBuilder()
            try {
                withTimeout(60_000L) {
                    chatModel.generateContentStream(cleanPrompt).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        accumulatedText.append(chunkText)
                        val updatedText = accumulatedText.toString()
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == assistantMsgId) {
                                msg.copy(text = updatedText, isStreaming = true)
                            } else {
                                msg
                            }
                        }
                    }
                }
                val finalOut = accumulatedText.toString().trim()
                if (finalOut.isNotEmpty()) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(text = finalOut, isStreaming = false)
                        } else {
                            msg
                        }
                    }
                } else {
                    _messages.value = _messages.value.filterNot { it.id == assistantMsgId }
                    _chatError.value = "أعاد النموذج استجابة فارغة، يرجى المحاولة مرة أخرى."
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI chat streaming timed out", e)
                _messages.value = _messages.value.filterNot { it.id == assistantMsgId && it.text.isBlank() }
                _chatError.value = "انتهت مهلة الاتصال بالمساعد الذكي بعد 60 ثانية. يرجى المحاولة مرة أخرى."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI chat generation failed", e)
                _messages.value = _messages.value.filterNot { it.id == assistantMsgId && it.text.isBlank() }
                _chatError.value = e.localizedMessage ?: "تعذر الاتصال بالمساعد الذكي."
            } finally {
                _isGeneratingChat.value = false
            }
        }
    }

    fun analyzeAyahStructured(surahName: String, ayahNumber: Int, ayahText: String) {
        viewModelScope.launch {
            _insightState.value = AiInsightState.Loading
            val prompt = """
                قم بتحليل هذه الآية الكريمة تحليلاً قرآنياً شاملاً:
                سورة $surahName - الآية ($ayahNumber):
                «$ayahText»
                اذكر التفسير والتدبر، أهم أحكام التجويد في كلماتها، التطبيق العملي، ونصيحة لتثبيت حفظها.
            """.trimIndent()

            try {
                val response = withTimeout(60_000L) {
                    structuredModel.generateContent(prompt)
                }
                val jsonString = response.text ?: throw IllegalStateException("استجابة فارغة من النموذج.")
                val json = JSONObject(jsonString)
                val ref = json.optString("ayahReference", "سورة $surahName - آية $ayahNumber")
                val meaning = json.optString("coreMeaning", "")
                val rulesArr = json.optJSONArray("tajweedRules")
                val rules = if (rulesArr != null) {
                    (0 until rulesArr.length()).map { rulesArr.getString(it) }
                } else {
                    emptyList()
                }
                val action = json.optString("practicalAction", "")
                val tip = json.optString("memorizationTip", "")

                _insightState.value = AiInsightState.Success(
                    StructuredAyahInsight(
                        ayahReference = ref,
                        coreMeaning = meaning,
                        tajweedRules = rules,
                        practicalAction = action,
                        memorizationTip = tip
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Structured Ayah analysis timed out", e)
                _insightState.value = AiInsightState.Error("انتهت مهلة تحليل الآية (60 ثانية). حاول مجدداً.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Structured Ayah analysis failed", e)
                _insightState.value = AiInsightState.Error(e.localizedMessage ?: "تعذر تحليل الآية الكريمة.")
            }
        }
    }

    fun clearInsight() {
        _insightState.value = AiInsightState.Idle
    }
}
