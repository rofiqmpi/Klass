package com.mpi.klass

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.SendIcon
import com.mpi.klass.theme.SparkleIcon
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AiChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val sourceBadge: String? = null,
    val blocks: List<ZedA2UiBlock> = emptyList(),
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
    var isAiTyping by remember { mutableStateOf(false) }

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
                userFullProfile = if (userEmail.isNotBlank()) fetchLocalProfile(userEmail) else null,
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
                val teacherPrefsDef = async { runCatching { fetchAllTeacherSchedulingPrefsFromFirestore() }.getOrElse { emptyList() } }
                val classroomsDef = async { runCatching { fetchClassroomsFromFirestore() }.getOrElse { emptyList() } }
                val routineDef = async {
                    runCatching {
                        fetchRoutinePlanFromFirestore("master_6m_published")
                            ?: fetchRoutinePlanFromFirestore("master_6m")
                    }.getOrNull()
                }
                val donorsDef = async { runCatching { fetchBloodDonorsFromFirestore() }.getOrElse { emptyList() } }
                val studentProfDef = async {
                    if (userEmail.isNotBlank()) {
                        runCatching { RoleConfig.fetchStudentProfile(userEmail) }.getOrNull()
                            ?: fetchLocalStudentProfile(userEmail)
                    } else null
                }
                val fullProfDef = async {
                    if (userEmail.isNotBlank()) {
                        runCatching { RoleConfig.fetchRemoteUserProfile(userEmail) }.getOrNull()
                            ?: fetchLocalProfile(userEmail)
                    } else null
                }

                contextSnapshot = ZedContextSnapshot(
                    books = booksDef.await(),
                    teachers = teachersDef.await(),
                    teacherPrefs = teacherPrefsDef.await(),
                    classrooms = classroomsDef.await(),
                    routinePlan = routineDef.await(),
                    donors = donorsDef.await(),
                    userProfile = studentProfDef.await(),
                    userFullProfile = fullProfDef.await(),
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
                text = "হাই $firstName! আমি Zed।\nMPI Klass অ্যাপের যেকোনো তথ্য কিংবা দুনিয়ার যেকোনো সাধারণ প্রশ্ন নির্দ্বিধায় জিজ্ঞাসা করুন।",
                isFromUser = false,
                sourceBadge = "Zed AI",
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = "Zed AI Assistant",
                        subtitle = "Campus Knowledge + General Gemini AI • A2UI",
                        domain = "GENERAL",
                    )
                ),
            )
        )
    }

    fun sendMessage(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isBlank() || isAiTyping) return
        inputText = ""

        val priorHistory = messages.filter { !it.id.startsWith("zed_welcome") && !it.id.startsWith("zed_reset_") }
        val baseId = "${messages.size}_${getCurrentTimeMillis()}"
        val replyId = "z_${baseId}_res"
        messages.add(
            AiChatMessage(
                id = "u_${baseId}_req",
                text = trimmed,
                isFromUser = true,
            )
        )

        isAiTyping = true
        scope.launch {
            var hasStreamBubble = false
            try {
                val reply = askZedWithGemini(
                    prompt = trimmed,
                    snapshot = contextSnapshot,
                    todayEnglishDay = currentDayNameEnglish(),
                    history = priorHistory,
                    onPartialText = { partial ->
                        val clean = partial.trimStart()
                        if (clean.isNotBlank()) {
                            val idx = messages.indexOfFirst { it.id == replyId }
                            val streamMsg = AiChatMessage(
                                id = replyId,
                                text = clean,
                                isFromUser = false,
                                sourceBadge = "Zed AI",
                                blocks = emptyList(),
                            )
                            if (idx >= 0) {
                                messages[idx] = streamMsg
                            } else {
                                messages.add(streamMsg)
                                hasStreamBubble = true
                                isAiTyping = false
                            }
                        }
                    },
                )
                val finalMsg = AiChatMessage(
                    id = replyId,
                    text = reply.answerText,
                    isFromUser = false,
                    sourceBadge = reply.sourceBadge,
                    blocks = reply.blocks,
                )
                val existingIdx = messages.indexOfFirst { it.id == replyId }
                if (existingIdx >= 0) {
                    messages[existingIdx] = finalMsg
                } else {
                    messages.add(finalMsg)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                val detail = t.localizedMessage?.takeIf { it.isNotBlank() } ?: t::class.simpleName ?: "Unknown error"
                val isBn = ZedAiEngine.prefersBengali(trimmed)
                val errMsg = if (isBn) {
                    "Zed AI রেসপন্স করতে পারেনি: $detail"
                } else {
                    "Zed AI could not respond: $detail"
                }
                val errBubble = AiChatMessage(
                    id = if (hasStreamBubble) replyId else "z_${baseId}_err",
                    text = errMsg,
                    isFromUser = false,
                    sourceBadge = "AI Error",
                    blocks = listOf(ZedA2UiBlock.CalloutCard(message = errMsg, isWarning = true)),
                )
                val existingIdx = messages.indexOfFirst { it.id == replyId }
                if (existingIdx >= 0) {
                    messages[existingIdx] = errBubble
                } else {
                    messages.add(errBubble)
                }
            } finally {
                isAiTyping = false
            }
        }
    }

    LaunchedEffect(messages.size, isAiTyping) {
        val targetIndex = (messages.size + (if (isAiTyping) 1 else 0) - 1).coerceAtLeast(0)
        if (messages.isNotEmpty() || isAiTyping) {
            listState.animateScrollToItem(targetIndex)
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
                            if (!isAiTyping) {
                                messages.clear()
                                messages.add(
                                    AiChatMessage(
                                        id = "zed_reset_${getCurrentTimeMillis()}",
                                        text = "নতুন চ্যাট শুরু হলো, $firstName! ক্যাম্পাস বা যেকোনো বিষয়ে প্রশ্ন লিখুন।",
                                        isFromUser = false,
                                        sourceBadge = "Zed AI",
                                        blocks = listOf(
                                            ZedA2UiBlock.HeaderBanner(
                                                title = "Zed AI Assistant",
                                                subtitle = "Ready for your query",
                                                domain = "GENERAL",
                                            )
                                        ),
                                    )
                                )
                            }
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
                val bubbleMaxWidth: Dp = if (maxWidth >= 680.dp) 580.dp else 334.dp

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
                            )
                        }
                        if (isAiTyping) {
                            item(key = "zed_typing_indicator") {
                                ZedTypingIndicatorBubble(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                )
                            }
                        }
                    }

                    // Clean Composer Pill (zero preset chips above)
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
                                        text = "যেকোনো প্রশ্ন লিখুন...",
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
                                        .onPreviewKeyEvent { event ->
                                            if (event.type == KeyEventType.KeyDown &&
                                                (event.key == Key.Enter || event.key == Key.NumPadEnter) &&
                                                !event.isShiftPressed
                                            ) {
                                                if (inputText.isNotBlank() && !isAiTyping) {
                                                    sendMessage(inputText)
                                                }
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        .testTag("ai_prompt_input"),
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            val canSend = inputText.isNotBlank() && !isAiTyping
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
                                    com.mpi.klass.theme.TelegramAnimatedIcon(
                                        icon = SendIcon,
                                        contentDescription = "Send to Zed",
                                        tint = if (canSend) Color.White else aiAccent.primary.copy(alpha = 0.55f),
                                        size = 18.dp,
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

@Composable
private fun ZedTypingIndicatorBubble(
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    val aiAccent = KlassFeaturePalette.ai(isDark)
    val transition = rememberInfiniteTransition(label = "ZedTypingDots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Restart,
        ),
        label = "TypingDotPhase",
    )

    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            elapsedSeconds++
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_typing_indicator"),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(aiAccent.primary),
            contentAlignment = Alignment.Center,
        ) {
            com.mpi.klass.theme.TelegramAnimatedIcon(
                icon = SparkleIcon,
                contentDescription = "Zed typing",
                tint = Color.White,
                size = 15.dp,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            color = if (isDark) cs.surfaceContainerHigh.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f),
            border = BorderStroke(0.75.dp, aiAccent.borderTint.copy(alpha = 0.7f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (elapsedSeconds >= 3) "Zed is thinking (${elapsedSeconds}s)" else "Zed is thinking",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = hindSiliguri,
                    color = aiAccent.primary,
                )
                for (dotIdx in 0..2) {
                    val dist = kotlin.math.abs(phase - dotIdx).coerceAtMost(1f)
                    val dotScale = 0.7f + (1f - dist) * 0.55f
                    val dotAlpha = 0.35f + (1f - dist) * 0.65f
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer {
                                scaleX = dotScale
                                scaleY = dotScale
                            }
                            .alpha(dotAlpha)
                            .clip(CircleShape)
                            .background(aiAccent.primary),
                    )
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
    maxBubbleWidth: Dp,
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
                com.mpi.klass.theme.TelegramAnimatedIcon(
                    icon = SparkleIcon,
                    contentDescription = "Zed",
                    tint = Color.White,
                    size = 15.dp,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = maxBubbleWidth),
            horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val bubbleBg = if (message.isFromUser) {
                aiAccent.primary
            } else {
                if (isDark) cs.surfaceContainerHigh.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.92f)
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
                            fontSize = 11.5.sp,
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

            if (!message.isFromUser && message.blocks.isNotEmpty()) {
                ZedA2UiSurfaceView(
                    blocks = message.blocks,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ZedA2UiSurfaceView(
    blocks: List<ZedA2UiBlock>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val defaultAccent = KlassFeaturePalette.ai(isDark)

    fun accentForDomain(domain: String) = when (domain.uppercase()) {
        "BOOKS" -> KlassFeaturePalette.books(isDark)
        "BLOOD" -> KlassFeaturePalette.error(isDark)
        "TEACHERS" -> KlassFeaturePalette.teachers(isDark)
        "ROUTINE" -> KlassFeaturePalette.routine(isDark)
        else -> defaultAccent
    }

    val bannerDomain = blocks.filterIsInstance<ZedA2UiBlock.HeaderBanner>().firstOrNull()?.domain ?: "GENERAL"
    val domainAccent = accentForDomain(bannerDomain)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is ZedA2UiBlock.HeaderBanner -> {
                    val bAccent = accentForDomain(block.domain)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bAccent.softTint,
                        border = BorderStroke(0.75.dp, bAccent.borderTint),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(bAccent.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = SparkleIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = block.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = hindSiliguri,
                                    color = bAccent.primary,
                                )
                                if (block.subtitle.isNotBlank()) {
                                    Text(
                                        text = block.subtitle,
                                        fontSize = 12.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                is ZedA2UiBlock.StatRow -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        block.stats.take(3).forEach { stat ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) cs.surfaceContainerHigh else Color.White,
                                border = BorderStroke(0.75.dp, domainAccent.borderTint),
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = stat.value,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = domainAccent.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = stat.label,
                                        fontSize = 11.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                is ZedA2UiBlock.KeyValueCard -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) cs.surfaceContainerHigh else Color.White,
                        border = BorderStroke(0.75.dp, cs.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = block.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = domainAccent.primary,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            block.items.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = item.label,
                                        fontSize = 13.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                        modifier = Modifier.weight(0.45f),
                                    )
                                    Text(
                                        text = item.value,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurface,
                                        modifier = Modifier.weight(0.55f),
                                    )
                                }
                                if (idx < block.items.lastIndex) {
                                    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }

                is ZedA2UiBlock.EntityListCard -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) cs.surfaceContainerHigh else Color.White,
                        border = BorderStroke(0.75.dp, cs.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = block.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = domainAccent.primary,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            block.entities.forEachIndexed { idx, entity ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (entity.badge.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = domainAccent.softTint,
                                            border = BorderStroke(0.75.dp, domainAccent.borderTint),
                                        ) {
                                            Text(
                                                text = entity.badge,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = domainAccent.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entity.primary,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (entity.secondary.isNotBlank()) {
                                            Text(
                                                text = entity.secondary,
                                                fontSize = 12.5.sp,
                                                fontFamily = hindSiliguri,
                                                color = cs.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    val phone = entity.phone?.trim()
                                    if (!phone.isNullOrBlank()) {
                                        IconButton(
                                            onClick = { openPhoneDialer(phone) },
                                            modifier = Modifier.size(48.dp),
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = domainAccent.softTint,
                                                modifier = Modifier.size(34.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = PhoneSolidIcon,
                                                        contentDescription = "Call ${entity.primary}",
                                                        tint = domainAccent.primary,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (idx < block.entities.lastIndex) {
                                    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }

                is ZedA2UiBlock.ScheduleListCard -> {
                    val routineAccent = KlassFeaturePalette.routine(isDark)
                    val labAccent = KlassFeaturePalette.teaching(isDark)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) cs.surfaceContainerHigh else Color.White,
                        border = BorderStroke(0.75.dp, cs.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = block.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = routineAccent.primary,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            block.slots.forEachIndexed { idx, slot ->
                                val slotAccent = if (slot.isPractical) labAccent else routineAccent
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = slotAccent.softTint,
                                        border = BorderStroke(0.75.dp, slotAccent.borderTint),
                                    ) {
                                        Text(
                                            text = slot.dayAndPeriod,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = slotAccent.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = slot.subject,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = listOf(slot.timeRange, slot.detail)
                                                .filter { it.isNotBlank() }
                                                .joinToString(" • "),
                                            fontSize = 12.5.sp,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurfaceVariant,
                                        )
                                    }
                                }
                                if (idx < block.slots.lastIndex) {
                                    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }

                is ZedA2UiBlock.CalloutCard -> {
                    val calloutAccent = if (block.isWarning) {
                        KlassFeaturePalette.error(isDark)
                    } else {
                        defaultAccent
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = calloutAccent.softTint,
                        border = BorderStroke(0.75.dp, calloutAccent.borderTint),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = InfoCircleIcon,
                                contentDescription = null,
                                tint = calloutAccent.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = block.message,
                                fontSize = 13.sp,
                                fontFamily = hindSiliguri,
                                color = cs.onSurface,
                                modifier = Modifier.weight(1f),
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
