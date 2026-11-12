package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealHighlight
import com.example.ui.viewmodel.AiChatMessage
import com.example.ui.viewmodel.AiInsightState
import com.example.ui.viewmodel.QuranAiCompanionViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiQuranCompanionScreen(
    aiViewModel: QuranAiCompanionViewModel
) {
    val messages by aiViewModel.messages.collectAsState()
    val isGenerating by aiViewModel.isGeneratingChat.collectAsState()
    val chatError by aiViewModel.chatError.collectAsState()
    val insightState by aiViewModel.insightState.collectAsState()

    var inputQuestion by remember { mutableStateOf("") }

    val quickPrompts = remember {
        listOf(
            "اشرح لي أحكام التجويد في سورة الفاتحة",
            "ضع لي خطة عملية لحفظ القرآن ومراجعته",
            "ما الفرق بين التفسير والتدبر؟",
            "آيات قرآنية تبعث السكينة والطمأنينة في القلب"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "المعلم والمساعد القرآني الذكي (AI)",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary
                )
                Text(
                    text = "محادثات فورية للتفسير، أحكام التجويد، المتشابهات، وتدبر الآيات",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Prompt Suggestions
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            quickPrompts.forEach { prompt ->
                AssistChip(
                    onClick = { aiViewModel.sendChatMessage(prompt) },
                    label = { Text(prompt, fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = EmeraldCard,
                        labelColor = GoldLight
                    )
                )
            }
        }

        // Structured Ayah Insight Card (when triggered from Mushaf or analysis)
        AnimatedVisibility(visible = insightState !is AiInsightState.Idle) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldElevated),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.5.dp, TealHighlight, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بطاقة التدبر والتجويد المعمّق بالذكاء الاصطناعي",
                            style = MaterialTheme.typography.titleMedium,
                            color = TealHighlight,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { aiViewModel.clearInsight() }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = GoldLight)
                        }
                    }

                    when (val st = insightState) {
                        is AiInsightState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = TealHighlight,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("جارٍ استخراج التدبر وأحكام التجويد للآية الكريمة...", color = GoldLight)
                            }
                        }
                        is AiInsightState.Error -> {
                            Text(text = st.message, color = MaterialTheme.colorScheme.error)
                        }
                        is AiInsightState.Success -> {
                            val data = st.insight
                            Text(
                                text = data.ayahReference,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• التدبر والمعنى: ${data.coreMeaning}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldLight
                            )
                            if (data.tajweedRules.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• أحكام التجويد المستخرجة:",
                                    color = TealHighlight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                data.tajweedRules.forEach { rule ->
                                    Text(
                                        text = "  - $rule",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GoldLight
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• العمل بالآية: ${data.practicalAction}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• نصيحة التثبيت والحفظ: ${data.memorizationTip}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldPrimary
                            )
                        }
                        AiInsightState.Idle -> {}
                    }
                }
            }
        }

        // Chat Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("ai_chat_messages_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                AiChatBubble(message = message)
            }
        }

        if (!chatError.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = chatError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuestion,
                onValueChange = { inputQuestion = it },
                placeholder = { Text("اسأل عن تفسير آية، حكم تجويد، أو خطة حفظ...") },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_question_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val q = inputQuestion
                    inputQuestion = ""
                    aiViewModel.sendChatMessage(q)
                },
                enabled = inputQuestion.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (inputQuestion.isNotBlank() && !isGenerating) GoldPrimary else EmeraldCard)
                    .testTag("send_ai_question_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال السؤال",
                        tint = if (inputQuestion.isNotBlank()) EmeraldDeep else GoldLight
                    )
                }
            }
        }
    }
}

@Composable
private fun AiChatBubble(message: AiChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.Start else Arrangement.End
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) GoldPrimary else EmeraldCard
            ),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 4.dp else 18.dp,
                bottomEnd = if (message.isUser) 18.dp else 4.dp
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(
                    width = 1.dp,
                    color = if (message.isUser) GoldPrimary else GoldPrimary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (message.isUser) "أنت" else "المعلم القرآني الذكي • نور المرتل",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (message.isUser) EmeraldDeep.copy(alpha = 0.75f) else GoldPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text.ifBlank { "جارٍ كتابة الإجابة..." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.isUser) EmeraldDeep else GoldLight
                )
            }
        }
    }
}
