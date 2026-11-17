package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onNotificationClick: (() -> Unit)? = null,
    userDisplayName: String = "User",
) {
    val cs = MaterialTheme.colorScheme
    val geminiGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF4285F4),
                Color(0xFF9B72CF),
                Color(0xFFD96570),
            )
        )
    }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var uiState by remember { mutableStateOf<AiUiState>(AiUiState.Idle) }
    val messages = remember {
        mutableStateListOf(
            AiChatMessage(
                id = "welcome",
                text = "Hi${if (userDisplayName.isNotBlank() && userDisplayName != "User") " $userDisplayName" else ""}! Ask me for lecture summaries, concept explanations, or exam prep.",
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
                    e.localizedMessage ?: "Connection error. Please try again."
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
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GoogleTopAppBar(
                title = "Study Assistant",
                hindSiliguri = hindSiliguri,
                onNavigationClick = onBack,
                navigationTestTag = "ai_back_button",
                actions = {
                    TextButton(
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
                        shape = CircleShape,
                        modifier = Modifier.testTag("ai_clear_chat_button"),
                    ) {
                        Text(
                            text = "New chat",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = hindSiliguri,
                        )
                    }
                    if (onNotificationClick != null) {
                        HeaderAiNotificationActions(
                            isDark = isDark,
                            showAi = false,
                            showNotification = true,
                            onNotificationClick = onNotificationClick,
                        )
                    }
                },
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val bubbleMaxWidth: Dp = if (maxWidth >= 680.dp) 560.dp else 320.dp

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
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageBubble(
                                message = msg,
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
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(geminiGradient),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = SparkleIcon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = cs.primary,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Thinking...",
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
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
                                    shape = RoundedCornerShape(16.dp),
                                    color = cs.errorContainer,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = errorMsg,
                                            color = cs.onErrorContainer,
                                            fontSize = 13.5.sp,
                                            fontFamily = hindSiliguri,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Button(
                                            onClick = {
                                                val lastUserMsg = messages.lastOrNull { it.isFromUser }
                                                if (lastUserMsg != null) {
                                                    sendMessage(lastUserMsg.text)
                                                } else {
                                                    uiState = AiUiState.Idle
                                                }
                                            },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = cs.error,
                                                contentColor = cs.onError,
                                            ),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                            modifier = Modifier
                                                .height(36.dp)
                                                .testTag("ai_retry_button"),
                                        ) {
                                            Text(
                                                text = "Retry",
                                                fontSize = 13.sp,
                                                fontFamily = hindSiliguri,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

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
                                    accent = cs.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    icon = SparkleIcon,
                                    testTag = "ai_prompt_chip_$index",
                                    onClick = { inputText = promptPrefix },
                                )
                            }
                        }
                    }

                    // Gemini-style Pill Composer
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = cs.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
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
                                        text = "Ask anything about your studies",
                                        fontSize = 14.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    maxLines = 4,
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurface,
                                    ),
                                    cursorBrush = SolidColor(cs.primary),
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
                            IconButton(
                                onClick = { sendMessage(inputText) },
                                enabled = canSend,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("ai_send_button"),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (canSend) cs.primary else Color.Transparent,
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = SendIcon,
                                            contentDescription = "Send",
                                            tint = if (canSend) cs.onPrimary else cs.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
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
    hindSiliguri: FontFamily,
    geminiGradient: Brush,
    maxBubbleWidth: Dp,
    isStreaming: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(geminiGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SparkleIcon,
                    contentDescription = "Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isFromUser) 20.dp else 6.dp,
                bottomEnd = if (message.isFromUser) 6.dp else 20.dp,
            ),
            color = if (message.isFromUser) cs.primaryContainer else cs.surfaceContainerLow,
            modifier = Modifier.widthIn(max = maxBubbleWidth),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = message.text,
                    fontSize = 14.5.sp,
                    fontFamily = hindSiliguri,
                    color = if (message.isFromUser) cs.onPrimaryContainer else cs.onSurface,
                    lineHeight = 21.sp,
                )
                if (isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "●",
                        color = cs.primary,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
