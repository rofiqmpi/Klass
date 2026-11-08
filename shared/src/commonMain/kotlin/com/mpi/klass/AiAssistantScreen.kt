package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
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
                text = "স্বাগতম! আমি আপনার Klass Gemini AI স্টাডি সহকারী। ক্লাস নোটের সারাংশ, গণিত-বিজ্ঞান সমাধান, কুইজ প্রস্তুতি বা যেকোনো পড়াশোনার বিষয়ে আমাকে প্রশ্ন করতে পারেন।",
                isFromUser = false,
            )
        )
    }

    val promptSuggestions = listOf(
        "ক্লাস নোটের সারাংশ তৈরি করুন" to "দয়া করে আমার পড়া বিষয়ের একটি পরিষ্কার ও পয়েন্ট ভিত্তিক সারাংশ তৈরি করুন:",
        "কঠিন বিষয় সহজে ব্যাখ্যা" to "একটি জটিল বৈজ্ঞানিক বা গাণিতিক বিষয় সহজ উদাহরণ দিয়ে ব্যাখ্যা করুন:",
        "অনুশীলনী প্রশ্ন তৈরি" to "পরীক্ষার প্রস্তুতির জন্য ৫টি গুরুত্বপূর্ণ প্রশ্ন এবং উত্তর তৈরি করুন:",
        "পড়ার রুটিন তৈরি করুন" to "প্রতিদিনের পড়ার একটি কার্যকরী ও ভারসাম্যপূর্ণ সময়সূচি সাজিয়ে দিন।",
    )

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
                // Start streaming with Gemini 3.8 Flash via Firebase AI Logic
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
                    e.localizedMessage ?: "AI সংযোগে ত্রুটি হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = cardBg,
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                        modifier = Modifier.size(48.dp).testTag("ai_back_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = CloseIcon,
                                contentDescription = "ফিরে যান",
                                tint = textColor,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = SparkleIcon,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gemini AI সহকারী".localized(),
                                fontSize = 17.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                            )
                        }
                        Text(
                            text = "Powered by Gemini 3.8 Flash",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = subTextColor,
                        )
                    }
                }

                // Clear Chat Button
                Surface(
                    onClick = {
                        messages.clear()
                        messages.add(
                            AiChatMessage(
                                id = "welcome_reset",
                                text = "নতুন চ্যাট শুরু হয়েছে। আপনি কী জানতে চান?",
                                isFromUser = false,
                            )
                        )
                        uiState = AiUiState.Idle
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                    modifier = Modifier.testTag("ai_clear_chat_button"),
                ) {
                    Text(
                        text = "নতুন চ্যাট".localized(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    geminiGradient = geminiGradient,
                )
            }

            // Streaming or Loading active indicator
            if (uiState is AiUiState.Loading) {
                item(key = "loading_indicator") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = SparkleIcon,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF8B5CF6),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini চিন্তা করছে...".localized(),
                                fontSize = 13.sp,
                                fontFamily = hindSiliguri,
                                color = subTextColor,
                            )
                        }
                    }
                }
            } else if (uiState is AiUiState.Streaming) {
                val streamText = (uiState as AiUiState.Streaming).partialText
                item(key = "streaming_bubble") {
                    ChatMessageBubble(
                        message = AiChatMessage(id = "streaming_active", text = streamText, isFromUser = false),
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        geminiGradient = geminiGradient,
                        isStreaming = true,
                    )
                }
            }

            // Error banner with retry
            if (uiState is AiUiState.Error) {
                val errorMsg = (uiState as AiUiState.Error).message
                item(key = "error_banner") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = errorMsg,
                                color = Color(0xFFDC2626),
                                fontSize = 13.sp,
                                fontFamily = hindSiliguri,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = {
                                    val lastUserMsg = messages.lastOrNull { it.isFromUser }
                                    if (lastUserMsg != null) {
                                        sendMessage(lastUserMsg.text)
                                    } else {
                                        uiState = AiUiState.Idle
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEF4444),
                                modifier = Modifier.testTag("ai_retry_button"),
                            ) {
                                Text(
                                    text = "আবার চেষ্টা করুন".localized(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prompt Suggestions (Chips)
        AnimatedVisibility(
            visible = messages.size <= 2 && uiState !is AiUiState.Loading && uiState !is AiUiState.Streaming,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text(
                    text = "দ্রুত জিজ্ঞাসা করুন:",
                    fontSize = 12.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.SemiBold,
                    color = subTextColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                ) {
                    itemsIndexed(promptSuggestions) { index, (label, promptPrefix) ->
                        Surface(
                            onClick = {
                                inputText = promptPrefix
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            modifier = Modifier.testTag("ai_prompt_chip_$index"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = SparkleIcon,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label.localized(),
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = cardBg,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input"),
                    placeholder = {
                        Text(
                            text = "AI-কে যেকোনো প্রশ্ন করুন...".localized(),
                            fontSize = 14.sp,
                            fontFamily = hindSiliguri,
                            color = subTextColor,
                        )
                    },
                    maxLines = 4,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        color = textColor,
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        focusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                )

                Spacer(modifier = Modifier.width(8.dp))

                val isBusy = uiState is AiUiState.Loading || uiState is AiUiState.Streaming
                Surface(
                    onClick = { sendMessage(inputText) },
                    enabled = inputText.isNotBlank() && !isBusy,
                    shape = CircleShape,
                    color = if (inputText.isNotBlank() && !isBusy) Color(0xFF8B5CF6) else if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    modifier = Modifier.size(48.dp).testTag("ai_send_button"),
                    shadowElevation = if (inputText.isNotBlank()) 4.dp else 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = SendIcon,
                            contentDescription = "পাঠান",
                            tint = if (inputText.isNotBlank() && !isBusy) Color.White else subTextColor,
                            modifier = Modifier.size(20.dp),
                        )
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
    isStreaming: Boolean = false,
) {
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val userBubbleBg = Color(0xFF2563EB)
    val aiBubbleBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(geminiGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SparkleIcon,
                    contentDescription = "Gemini",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
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
            border = if (!message.isFromUser) BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)) else null,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 310.dp),
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
