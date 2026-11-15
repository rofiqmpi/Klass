package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.SendIcon
import com.mpi.klass.theme.SparkleIcon
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class AiChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val partialText: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

@Composable
fun AiAssistantScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBack: () -> Unit,
    userDisplayName: String = "User",
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accent = Color(0xFF8B5CF6)
    val geminiGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4285F4),
            Color(0xFF9B72CF),
            Color(0xFFD96570),
        )
    )

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var uiState by remember { mutableStateOf<AiUiState>(AiUiState.Idle) }
    val messages = remember {
        mutableStateListOf(
            AiChatMessage(
                id = "welcome",
                text = "Hi${if (userDisplayName.isNotBlank() && userDisplayName != "User") " $userDisplayName" else ""}! I am your AI study assistant. Ask me for lecture summaries, problem solving, or exam prep.",
                isFromUser = false,
            )
        )
    }

    val promptSuggestions = remember {
        listOf(
            "Summarize Notes" to "Summarize my class topic into clear bullet points:",
            "Explain Topic" to "Explain this engineering concept with a simple example:",
            "Practice Quiz" to "Create 5 exam practice questions with short answers about:",
            "Study Plan" to "Organize a balanced weekly study plan for my semester courses.",
        )
    }

    fun sendMessage(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isBlank() || uiState is AiUiState.Loading || uiState is AiUiState.Streaming) return

        inputText = ""
        val userMsgId = "u_${messages.size}"
        val aiMsgId = "ai_${messages.size + 1}"
        messages.add(AiChatMessage(id = userMsgId, text = trimmed, isFromUser = true))

        uiState = AiUiState.Loading

        scope.launch {
            try {
                var accumulated = ""
                val finalAnswer = GeminiAiService.generateStream(trimmed) { chunk ->
                    accumulated += chunk
                    uiState = AiUiState.Streaming(accumulated)
                }

                messages.add(AiChatMessage(id = aiMsgId, text = finalAnswer, isFromUser = false))
                uiState = AiUiState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = AiUiState.Error(
                    e.localizedMessage ?: "AI connection error. Please try again."
                )
            }
        }
    }

    LaunchedEffect(messages.size, uiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AppBackHandler(enabled = true, onBack = onBack)

    MinimalBackdrop(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Clean Full-Width Header with Centered Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardBg,
                shadowElevation = 2.dp,
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 860.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            SwipeToCenterIconSurface(
                                dragId = "ai_back",
                                previewTitle = "Back",
                                accentColor = accent,
                                onTrigger = onBack,
                                isLeftSide = true,
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, borderCol),
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(48.dp).testTag("ai_back_button"),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CloseIcon,
                                        contentDescription = "Back",
                                        tint = textColor,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
                                modifier = Modifier.size(38.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = SparkleIcon,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "AI Study Assistant",
                                    fontSize = 16.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                )
                                Text(
                                    text = "Instant help & summaries",
                                    fontSize = 11.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Medium,
                                    color = subTextColor,
                                )
                            }
                        }

                        MinimalIconChip(
                            label = "New Chat",
                            selected = false,
                            accent = accent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "ai_clear_chat_button",
                            onClick = {
                                messages.clear()
                                messages.add(
                                    AiChatMessage(
                                        id = "welcome_reset",
                                        text = "New conversation started. How can I help you study?",
                                        isFromUser = false,
                                    )
                                )
                                uiState = AiUiState.Idle
                            },
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(borderCol),
                    )
                }
            }

            // Responsive Centered Conversation Pane
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val bubbleMaxWidth: Dp = if (maxWidth >= 680.dp) 520.dp else 310.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 820.dp),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageBubble(
                                message = msg,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                geminiGradient = geminiGradient,
                                maxBubbleWidth = bubbleMaxWidth,
                            )
                        }

                        if (uiState is AiUiState.Loading) {
                            item(key = "loading_indicator") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = accent.copy(alpha = 0.14f),
                                        modifier = Modifier.size(34.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = SparkleIcon,
                                                contentDescription = null,
                                                tint = accent,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = accent,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Thinking...",
                                        fontSize = 13.sp,
                                        fontFamily = hindSiliguri,
                                        color = subTextColor,
                                    )
                                }
                            }
                        } else if (uiState is AiUiState.Streaming) {
                            val streamText = (uiState as AiUiState.Streaming).partialText
                            item(key = "streaming_bubble") {
                                ChatMessageBubble(
                                    message = AiChatMessage(
                                        id = "streaming_active",
                                        text = streamText,
                                        isFromUser = false,
                                    ),
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    geminiGradient = geminiGradient,
                                    maxBubbleWidth = bubbleMaxWidth,
                                    isStreaming = true,
                                )
                            }
                        }

                        if (uiState is AiUiState.Error) {
                            val errorMsg = (uiState as AiUiState.Error).message
                            item(key = "error_banner") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.10f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = errorMsg,
                                            color = Color(0xFFDC2626),
                                            fontSize = 13.sp,
                                            fontFamily = hindSiliguri,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        WelcomeMinimal3DButton(
                                            text = "Retry",
                                            hindSiliguri = hindSiliguri,
                                            isDestructive = true,
                                            isDark = isDark,
                                            height = 36.dp,
                                            cornerRadius = 9.dp,
                                            testTag = "ai_retry_button",
                                            modifier = Modifier.width(84.dp),
                                            onClick = {
                                                val lastUserMsg = messages.lastOrNull { it.isFromUser }
                                                if (lastUserMsg != null) {
                                                    sendMessage(lastUserMsg.text)
                                                } else {
                                                    uiState = AiUiState.Idle
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Prompt Suggestions
                    AnimatedVisibility(
                        visible = messages.size <= 2 && uiState !is AiUiState.Loading && uiState !is AiUiState.Streaming,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            itemsIndexed(promptSuggestions) { index, (label, promptPrefix) ->
                                MinimalIconChip(
                                    label = label,
                                    selected = false,
                                    accent = accent,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    icon = SparkleIcon,
                                    testTag = "ai_prompt_chip_$index",
                                    onClick = { inputText = promptPrefix },
                                )
                            }
                        }
                    }

                    // Clean Minimal Composer Bar (single hairline border, zero nested boxes)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderCol),
                        shadowElevation = 2.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Ask anything about your studies...",
                                        fontSize = 14.sp,
                                        fontFamily = hindSiliguri,
                                        color = subTextColor,
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    maxLines = 4,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = hindSiliguri,
                                        color = textColor,
                                    ),
                                    cursorBrush = SolidColor(accent),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_prompt_input"),
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            val isBusy = uiState is AiUiState.Loading || uiState is AiUiState.Streaming
                            val canSend = inputText.isNotBlank() && !isBusy
                            WelcomeMinimal3DIconButton(
                                icon = SendIcon,
                                contentDescription = "Send",
                                isDark = isDark,
                                containerColor = if (canSend) accent else cardBg,
                                iconTint = if (canSend) Color.White else subTextColor,
                                enabled = canSend,
                                testTag = "ai_send_button",
                                onClick = { sendMessage(inputText) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: AiChatMessage,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    geminiGradient: Brush,
    maxBubbleWidth: Dp,
    isStreaming: Boolean = false,
) {
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val userBubbleBg = Color(0xFF2563EB)
    val aiBubbleBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(geminiGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SparkleIcon,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp,
            ),
            color = if (message.isFromUser) userBubbleBg else aiBubbleBg,
            border = if (!message.isFromUser) BorderStroke(1.dp, borderCol) else null,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = maxBubbleWidth),
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    fontFamily = hindSiliguri,
                    color = if (message.isFromUser) Color.White else textColor,
                    lineHeight = 20.sp,
                )
                if (isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "●",
                        color = Color(0xFF8B5CF6),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
