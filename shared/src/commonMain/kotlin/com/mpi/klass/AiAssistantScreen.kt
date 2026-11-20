package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.SendIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.rainbowAccentAt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class AiChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val sourceBadge: String? = null,
    val followUps: List<String> = emptyList(),
)

@Composable
fun AiAssistantScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBack: () -> Unit,
    onNotificationClick: (() -> Unit)? = null,
    userDisplayName: String = "User",
    userRole: UserRole? = null,
    userEmail: String = "",
) {
    val cs = MaterialTheme.colorScheme
    val aiAccent = KlassFeaturePalette.ai(isDark)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }

    val firstName = remember(userDisplayName) {
        userDisplayName.trim().split(" ").firstOrNull()
            ?.takeIf { it.isNotBlank() && !it.equals("User", true) }
            ?: "বন্ধু"
    }

    var contextSnapshot by remember {
        mutableStateOf(
            ZedContextSnapshot(
                books = BtebBooksCatalog.books,
                userRole = userRole,
                userDisplayName = userDisplayName,
                userProfile = if (userEmail.isNotBlank()) fetchLocalStudentProfile(userEmail) else null,
            )
        )
    }

    LaunchedEffect(userEmail, userRole, userDisplayName) {
        try {
            coroutineScope {
                val booksDef = async {
                    runCatching { RoleConfig.fetchLibraryBooks() }.getOrNull()
                        ?.takeIf { it.isNotEmpty() } ?: BtebBooksCatalog.books
                }
                val teachersDef = async { runCatching { RoleConfig.fetchLoggedInTeachers() }.getOrElse { emptyList() } }
                val routineDef = async {
                    runCatching {
                        fetchRoutinePlanFromFirestore("master_6m_published")
                            ?: fetchRoutinePlanFromFirestore("master_6m")
                    }.getOrNull()
                }
                val donorsDef = async { runCatching { fetchBloodDonorsFromFirestore() }.getOrElse { emptyList() } }

                contextSnapshot = ZedContextSnapshot(
                    books = booksDef.await(),
                    teachers = teachersDef.await(),
                    routinePlan = routineDef.await(),
                    donors = donorsDef.await(),
                    userProfile = if (userEmail.isNotBlank()) fetchLocalStudentProfile(userEmail) else null,
                    userRole = userRole,
                    userDisplayName = userDisplayName,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Keep existing cached snapshot
        }
    }

    val messages = remember {
        mutableStateListOf(
            AiChatMessage(
                id = "zed_welcome",
                text = "হাই $firstName! 👋 আমি **Zed**।\nকেবল **Books Library, Blood Donor, Teachers** এবং **Class Routine** সম্পর্কে ছোট ও গোছানো উত্তর দিই।",
                isFromUser = false,
                sourceBadge = "Zed AI",
            )
        )
    }

    val quickChips = remember {
        listOf(
            Triple("Books", "CST ডিপার্টমেন্টের বই দেখাও", BookIcon),
            Triple("Blood Donor", "ব্লাড ডোনার তালিকা দেখাও", DropletIcon),
            Triple("Teachers", "টিচারদের তালিকা দেখাও", PersonIcon),
            Triple("Routine", "আজকের ক্লাস রুটিন দেখাও", CalendarDaysIcon),
        )
    }

    fun sendMessage(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isBlank()) return
        inputText = ""

        val baseId = messages.size
        messages.add(
            AiChatMessage(
                id = "u_${baseId}_req",
                text = trimmed,
                isFromUser = true,
            )
        )

        val reply = ZedAiEngine.evaluate(
            query = trimmed,
            snapshot = contextSnapshot,
            todayEnglishDay = currentDayNameEnglish(),
        )

        messages.add(
            AiChatMessage(
                id = "z_${baseId}_res",
                text = reply.answerText,
                isFromUser = false,
                sourceBadge = reply.sourceBadge,
                followUps = reply.suggestedFollowUps,
            )
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AppBackHandler(enabled = true, onBack = onBack)

    MinimalBackdrop(isDark = isDark, focusAccent = aiAccent.primary) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GoogleTopAppBar(
                title = "Zed AI",
                hindSiliguri = hindSiliguri,
                onNavigationClick = onBack,
                navigationTestTag = "ai_back_button",
                actions = {
                    TextButton(
                        onClick = {
                            messages.clear()
                            messages.add(
                                AiChatMessage(
                                    id = "zed_reset_${messages.size}",
                                    text = "নতুন চ্যাট শুরু হলো, $firstName! 😊 **Books, Blood Donor, Teachers** বা **Routine** সম্পর্কে জিজ্ঞেস করো।",
                                    isFromUser = false,
                                    sourceBadge = "Zed AI",
                                )
                            )
                        },
                        shape = CircleShape,
                        modifier = Modifier.testTag("ai_clear_chat_button"),
                    ) {
                        Text(
                            text = "New",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = hindSiliguri,
                            color = aiAccent.primary,
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
                val bubbleMaxWidth: Dp = if (maxWidth >= 680.dp) 560.dp else 326.dp

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
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageBubble(
                                message = msg,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                maxBubbleWidth = bubbleMaxWidth,
                                onQuickFollowUp = { q -> sendMessage(q) },
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = messages.size <= 2,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            itemsIndexed(quickChips) { index, (label, promptBody, icon) ->
                                MinimalIconChip(
                                    label = label,
                                    selected = false,
                                    accent = rainbowAccentAt(index + 3, isDark),
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    icon = icon,
                                    testTag = "ai_prompt_chip_$index",
                                    onClick = { sendMessage(promptBody) },
                                )
                            }
                        }
                    }

                    // Clean Composer Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = if (isDark) cs.surfaceContainerHigh.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.94f),
                        border = BorderStroke(
                            width = if (inputText.isNotEmpty()) 1.5.dp else 1.dp,
                            color = if (inputText.isNotEmpty()) aiAccent.primary else aiAccent.borderTint,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
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
                                        text = "Books, Blood, Teachers বা Routine...",
                                        fontSize = 15.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    maxLines = 4,
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurface,
                                    ),
                                    cursorBrush = SolidColor(aiAccent.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_prompt_input"),
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            val canSend = inputText.isNotBlank()
                            IconButton(
                                onClick = { sendMessage(inputText) },
                                enabled = canSend,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("ai_send_button"),
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (canSend) aiAccent.primary else aiAccent.softTint),
                                ) {
                                    Icon(
                                        imageVector = SendIcon,
                                        contentDescription = "Send to Zed",
                                        tint = if (canSend) Color.White else aiAccent.primary.copy(alpha = 0.55f),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessageBubble(
    message: AiChatMessage,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    maxBubbleWidth: Dp,
    onQuickFollowUp: (String) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val aiAccent = KlassFeaturePalette.ai(isDark)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(aiAccent.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SparkleIcon,
                    contentDescription = "Zed",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = maxBubbleWidth),
            horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start,
        ) {
            val bubbleBg = if (message.isFromUser) {
                aiAccent.primary
            } else {
                if (isDark) cs.surfaceContainerHigh.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f)
            }
            val bubbleBorder = if (message.isFromUser) {
                null
            } else {
                BorderStroke(0.75.dp, aiAccent.borderTint.copy(alpha = 0.7f))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isFromUser) 20.dp else 6.dp,
                    topEnd = if (message.isFromUser) 6.dp else 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                ),
                color = bubbleBg,
                border = bubbleBorder,
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (!message.isFromUser && !message.sourceBadge.isNullOrBlank()) {
                        Text(
                            text = message.sourceBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = aiAccent.primary,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }

                    val bodyColor = if (message.isFromUser) Color.White else cs.onSurface
                    val styledBody = remember(message.text, bodyColor) {
                        formatCompactBoldText(message.text, bodyColor)
                    }
                    Text(
                        text = styledBody,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        fontFamily = hindSiliguri,
                        color = bodyColor,
                    )
                }
            }

            if (!message.isFromUser && message.followUps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    message.followUps.take(4).forEachIndexed { idx, followUp ->
                        val chipAccent = rainbowAccentAt(idx + 2, isDark)
                        Surface(
                            shape = CircleShape,
                            color = chipAccent.copy(alpha = if (isDark) 0.16f else 0.10f),
                            border = BorderStroke(0.75.dp, chipAccent.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onQuickFollowUp(followUp) },
                        ) {
                            Text(
                                text = followUp,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = hindSiliguri,
                                color = chipAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatCompactBoldText(raw: String, baseColor: Color) = buildAnnotatedString {
    var i = 0
    while (i < raw.length) {
        if (i + 1 < raw.length && raw[i] == '*' && raw[i + 1] == '*') {
            val close = raw.indexOf("**", startIndex = i + 2)
            if (close > i + 2) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                    append(raw.substring(i + 2, close))
                }
                i = close + 2
                continue
            }
        }
        append(raw[i])
        i++
    }
}
