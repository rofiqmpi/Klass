package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mpi.klass.theme.BackArrowIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LockIcon
import com.mpi.klass.theme.MessageBubbleIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.SendIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.TelegramAnimatedIcon
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.UsersGroupIcon
import kotlinx.coroutines.launch

/**
 * Dedicated 1-on-1 Messenger Hub (separated cleanly from Friends/Connections).
 * Displays pinned Zed AI assistant row, ongoing 1-on-1 threads, quick campus peers,
 * and a top-bar shortcut icon to open Friends & Requests.
 */
@Composable
fun MessagesScreen(
    viewerEmail: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    members: List<DirectoryMember>,
    connections: List<ConnectionRecord>,
    isLoading: Boolean = false,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenChat: (DirectoryMember) -> Unit,
    onOpenAiChat: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val cleanViewer = viewerEmail.ifBlank { loadUserSession()?.email ?: "" }.trim().lowercase()
    val fbBlue = Color(0xFF0866FF)
    val aiAccent = KlassFeaturePalette.ai(isDark)

    var searchQuery by remember { mutableStateOf("") }
    var chatThreads by remember(cleanViewer) { mutableStateOf<List<ChatThreadSummary>>(emptyList()) }

    val revision = SocialChatState.chatRevision
    val unreadByPeer = SocialChatState.unreadByPeer
    val incomingRequestCount = remember(connections, cleanViewer) {
        connections.count {
            !it.status.equals("ACCEPTED", ignoreCase = true) &&
                it.receiverEmail.trim().lowercase() == cleanViewer
        }
    }

    LaunchedEffect(cleanViewer, revision, connections) {
        if (cleanViewer.isNotBlank()) {
            chatThreads = loadLocalChatThreads(cleanViewer)
        }
    }

    val acceptedFriends = remember(connections, cleanViewer, members) {
        connections.filter { it.status.equals("ACCEPTED", ignoreCase = true) }.map { rec ->
            val pEmail = rec.peerEmailFor(cleanViewer)
            members.firstOrNull { it.email.equals(pEmail, true) }
                ?: DirectoryMember(
                    email = pEmail,
                    name = rec.peerNameFor(cleanViewer),
                    photoUrl = rec.peerPhotoFor(cleanViewer),
                    profile = StudentProfile(
                        email = pEmail,
                        name = rec.peerNameFor(cleanViewer),
                        photoUrl = rec.peerPhotoFor(cleanViewer),
                    ),
                )
        }
    }

    val q = searchQuery.trim()
    val filteredThreads = remember(chatThreads, q) {
        if (q.isBlank()) chatThreads else chatThreads.filter {
            it.peerName.contains(q, ignoreCase = true) ||
                it.peerEmail.contains(q, ignoreCase = true) ||
                it.lastMessage.contains(q, ignoreCase = true)
        }
    }
    val filteredMembers = remember(members, q) {
        if (q.isBlank()) members else members.filter { m ->
            m.name.contains(q, ignoreCase = true) ||
                m.email.contains(q, ignoreCase = true) ||
                m.profile.department.contains(q, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().rainbowScreenBackground(isDark, fbBlue)) {
        Column(modifier = Modifier.fillMaxSize()) {
            GoogleTopAppBar(
                title = "মেসেজ".localized(),
                hindSiliguri = hindSiliguri,
                onNavigationClick = onBack,
                navigationTestTag = "messages_back_button",
                actions = {
                    Box {
                        KlassHeaderIconButton(
                            icon = UsersGroupIcon,
                            contentDescription = "বন্ধু ও কানেকশন".localized(),
                            isDark = isDark,
                            visualSize = 38.dp,
                            hasActivity = incomingRequestCount > 0,
                            onClick = onOpenFriends,
                        )
                        if (incomingRequestCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF0F172A) else Color.White)
                                    .padding(1.5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444)),
                            )
                        }
                    }
                    KlassHeaderIconButton(
                        icon = RefreshIcon,
                        contentDescription = "Refresh",
                        isDark = isDark,
                        visualSize = 38.dp,
                        isLoading = isLoading,
                        onClick = onRefresh,
                    )
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
            ) {
                MinimalSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "চ্যাট খুঁজুন...".localized(),
                    accent = fbBlue,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    testTag = "messages_search_input",
                    clearTestTag = "messages_search_clear",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 820.dp)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(bottom = 28.dp),
                ) {
                    val showZedAi = q.isBlank() ||
                        "zed ai".contains(q, ignoreCase = true) ||
                        "assistant".contains(q, ignoreCase = true) ||
                        "ai".equals(q, ignoreCase = true) ||
                        "জেড".contains(q)

                    if (showZedAi) {
                        GoogleSectionHeader(
                            title = "AI Assistant",
                            accent = aiAccent.primary,
                            hindSiliguri = hindSiliguri,
                        )
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenAiChat() }
                                    .padding(horizontal = 16.dp, vertical = 13.dp)
                                    .testTag("messenger_zed_ai_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = aiAccent.softTint,
                                    border = BorderStroke(1.2.dp, aiAccent.borderTint),
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        TelegramAnimatedIcon(
                                            icon = SparkleIcon,
                                            contentDescription = "Zed AI",
                                            tint = aiAccent.primary,
                                            size = 22.dp,
                                            hasActivity = true,
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = "Zed AI",
                                            color = cs.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            maxLines = 1,
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = aiAccent.softTint,
                                            border = BorderStroke(0.6.dp, aiAccent.borderTint),
                                        ) {
                                            Text(
                                                text = "AI",
                                                color = aiAccent.primary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Ask 1-on-1 about routine, books, teachers & web",
                                        color = cs.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontFamily = hindSiliguri,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Icon(
                                    imageVector = ChevronRightIcon,
                                    contentDescription = null,
                                    tint = aiAccent.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    if (filteredThreads.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        GoogleSectionHeader(
                            title = "Conversations",
                            accent = fbBlue,
                            hindSiliguri = hindSiliguri,
                        )
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            filteredThreads.forEachIndexed { idx, thread ->
                                val matchedMember = members.firstOrNull { it.email.equals(thread.peerEmail, true) }
                                    ?: DirectoryMember(
                                        email = thread.peerEmail,
                                        name = thread.peerName,
                                        photoUrl = thread.peerPhotoUrl,
                                        profile = StudentProfile(
                                            email = thread.peerEmail,
                                            name = thread.peerName,
                                            photoUrl = thread.peerPhotoUrl,
                                        ),
                                    )
                                val unread = unreadByPeer[thread.peerEmail.lowercase()] ?: thread.unreadCount
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenChat(matchedMember) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    MemberAvatarCircle(
                                        email = matchedMember.email,
                                        name = matchedMember.name,
                                        photoUrl = matchedMember.photoUrl,
                                        size = 48.dp,
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = matchedMember.name.ifBlank { thread.peerName },
                                            color = cs.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = thread.lastMessage.ifBlank { "Tap to chat" },
                                            color = if (unread > 0) cs.onSurface else cs.onSurfaceVariant,
                                            fontWeight = if (unread > 0) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 13.5.sp,
                                            fontFamily = hindSiliguri,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    if (unread > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = fbBlue,
                                        ) {
                                            Text(
                                                text = unread.toString(),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            )
                                        }
                                    }
                                }
                                if (idx < filteredThreads.lastIndex) {
                                    SocialInsetDivider(startIndent = 76.dp)
                                }
                            }
                        }
                    }

                    val threadPeerEmails = filteredThreads.map { it.peerEmail.trim().lowercase() }.toSet()
                    val quickFriends = acceptedFriends.filter { it.email.trim().lowercase() !in threadPeerEmails }
                    val otherPeers = filteredMembers.filter { m ->
                        val em = m.email.trim().lowercase()
                        em !in threadPeerEmails && quickFriends.none { it.email.equals(em, true) }
                    }
                    val startChatList = (quickFriends + otherPeers).take(if (q.isBlank()) 12 else 30)

                    if (startChatList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        GoogleSectionHeader(
                            title = if (quickFriends.isNotEmpty() && q.isBlank()) "Start a Chat" else "Campus People",
                            accent = KlassFeaturePalette.teaching(isDark).primary,
                            hindSiliguri = hindSiliguri,
                        )
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            startChatList.forEachIndexed { idx, peer ->
                                val deptShort = RoutineRules.normalizeDepartmentShort(peer.profile.department)
                                val roleLabel = when (peer.role) {
                                    UserRole.STUDENT -> "Student"
                                    UserRole.TEACHER -> "Teacher"
                                    UserRole.ADMIN -> "Admin"
                                }
                                val metaLine = listOfNotNull(
                                    roleLabel,
                                    deptShort.takeIf { it.isNotBlank() },
                                ).joinToString(" • ")

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenChat(peer) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    MemberAvatarCircle(
                                        email = peer.email,
                                        name = peer.name,
                                        photoUrl = peer.photoUrl,
                                        size = 46.dp,
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = peer.name.ifBlank { peer.email.substringBefore("@") },
                                            color = cs.onSurface,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = metaLine,
                                            color = cs.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            fontFamily = hindSiliguri,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    HeaderSavePillButton(
                                        text = "মেসেজ".localized(),
                                        accent = fbBlue,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        testTag = "message_peer_${peer.email}",
                                        onClick = { onOpenChat(peer) },
                                    )
                                }
                                if (idx < startChatList.lastIndex) {
                                    SocialInsetDivider(startIndent = 74.dp)
                                }
                            }
                        }
                    } else if (isLoading && members.isEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.height(260.dp)) {
                            KlassRowListSkeleton(
                                isDark = isDark,
                                showHeaderBar = true,
                                showActionPill = true,
                                badgeSize = 48.dp,
                                badgeCornerRadius = 24.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MemberAvatarCircle(
    email: String,
    name: String,
    photoUrl: String?,
    size: androidx.compose.ui.unit.Dp,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = cs.surfaceContainerHigh,
        modifier = Modifier.size(size),
    ) {
        val model = resolveProfileImageModel(photoUrl)
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        } else {
            DynamicAvatar(
                seed = email.ifBlank { name },
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        }
    }
}

/**
 * Dedicated 1-on-1 Direct Messaging Screen.
 * Messages persist locally per device while Firestore `chat_relay` acts strictly as a
 * zero-retention delivery queue (recipient deletes cloud doc immediately upon receipt).
 */
@Composable
fun DirectChatScreen(
    viewerEmail: String,
    viewerName: String,
    viewerPhotoUrl: String?,
    peer: DirectoryMember,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBack: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val cleanViewer = viewerEmail.trim().lowercase()
    val cleanPeer = peer.email.trim().lowercase()
    val fbBlue = Color(0xFF0866FF)
    val errorAccent = KlassFeaturePalette.error(isDark)

    var messages by remember(cleanViewer, cleanPeer) { mutableStateOf<List<ChatMessageItem>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val revision = SocialChatState.chatRevision

    DisposableEffect(cleanPeer) {
        SocialChatState.activePeerEmail = cleanPeer
        onDispose {
            if (SocialChatState.activePeerEmail == cleanPeer) {
                SocialChatState.activePeerEmail = null
            }
        }
    }

    LaunchedEffect(cleanViewer, cleanPeer, revision) {
        if (cleanViewer.isNotBlank() && cleanPeer.isNotBlank()) {
            markChatThreadRead(cleanViewer, cleanPeer)
            messages = loadLocalChatMessages(cleanViewer, cleanPeer)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    AppBackHandler(enabled = true) {
        if (showClearDialog) showClearDialog = false else onBack()
    }

    val peerDisplayName = peer.name.ifBlank { peer.email.substringBefore("@") }
    val headerFadeBg = if (isDark) cs.surface.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .rainbowScreenBackground(isDark, fbBlue),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
            // Top-to-bottom fading transparent header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                headerFadeBg,
                                headerFadeBg.copy(alpha = 0.52f),
                                Color.Transparent,
                            )
                        )
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(62.dp)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("chat_back_button"),
                    ) {
                        TelegramAnimatedIcon(
                            icon = BackArrowIcon,
                            contentDescription = "Back",
                            tint = cs.onSurface,
                            size = 22.dp,
                        )
                    }

                    MemberAvatarCircle(
                        email = peer.email,
                        name = peerDisplayName,
                        photoUrl = peer.photoUrl,
                        size = 40.dp,
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = peerDisplayName,
                            color = cs.onSurface,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = peer.role.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = cs.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            fontFamily = hindSiliguri,
                        )
                    }

                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.size(48.dp).testTag("chat_clear_button"),
                    ) {
                        TelegramAnimatedIcon(
                            icon = TrashCanIcon,
                            contentDescription = "Clear chat",
                            tint = cs.onSurfaceVariant,
                            size = 19.dp,
                        )
                    }
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 780.dp)
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = fbBlue.copy(alpha = if (isDark) 0.14f else 0.08f),
                        border = BorderStroke(0.75.dp, fbBlue.copy(alpha = 0.22f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TelegramAnimatedIcon(
                                icon = LockIcon,
                                contentDescription = null,
                                tint = fbBlue,
                                size = 15.dp,
                            )
                            Text(
                                text = "Messages are saved only on your device and auto-deleted from the cloud relay once delivered.",
                                color = cs.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontFamily = hindSiliguri,
                            )
                        }
                    }
                }

                if (messages.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 36.dp)) {
                            KlassEmptyState(
                                icon = MessageBubbleIcon,
                                title = "কোনো মেসেজ নেই".localized(),
                                subtitle = "Say hello to $peerDisplayName!",
                                accent = fbBlue,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                        }
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (msg.isMine) 18.dp else 4.dp,
                                    bottomEnd = if (msg.isMine) 4.dp else 18.dp,
                                ),
                                color = if (msg.isMine) fbBlue else {
                                    if (isDark) cs.surfaceContainerHigh else Color.White.copy(alpha = 0.92f)
                                },
                                border = if (msg.isMine) null else BorderStroke(0.75.dp, cs.outlineVariant.copy(alpha = 0.45f)),
                                shadowElevation = 0.dp,
                                modifier = Modifier.widthIn(max = 290.dp),
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isMine) Color.White else cs.onSurface,
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    fontFamily = hindSiliguri,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Composer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                headerFadeBg.copy(alpha = 0.92f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 780.dp)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = {
                            Text(
                                text = "মেসেজ লিখুন...".localized(),
                                fontFamily = hindSiliguri,
                                fontSize = 15.sp,
                            )
                        },
                        maxLines = 4,
                        textStyle = TextStyle(fontFamily = hindSiliguri, fontSize = 15.5.sp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = fbBlue,
                            unfocusedBorderColor = cs.outlineVariant.copy(alpha = 0.65f),
                            focusedContainerColor = if (isDark) cs.surfaceContainerLow else Color.White,
                            unfocusedContainerColor = if (isDark) cs.surfaceContainerLow else Color.White.copy(alpha = 0.9f),
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                    )

                    val canSend = draft.trim().isNotBlank() && !isSending
                    Surface(
                        onClick = {
                            val textToSend = draft.trim()
                            if (textToSend.isNotBlank() && !isSending) {
                                isSending = true
                                draft = ""
                                scope.launch {
                                    try {
                                        val res = sendEphemeralMessage(
                                            senderEmail = cleanViewer,
                                            senderName = viewerName,
                                            senderPhotoUrl = viewerPhotoUrl,
                                            receiverEmail = cleanPeer,
                                            receiverName = peerDisplayName,
                                            receiverPhotoUrl = peer.photoUrl,
                                            text = textToSend,
                                        )
                                        if (res.isFailure) {
                                            showToast("Failed to send message")
                                        }
                                        messages = loadLocalChatMessages(cleanViewer, cleanPeer)
                                    } finally {
                                        isSending = false
                                    }
                                }
                            }
                        },
                        enabled = canSend,
                        shape = CircleShape,
                        color = if (canSend) fbBlue else cs.surfaceContainerHigh,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                TelegramAnimatedIcon(
                                    icon = SendIcon,
                                    contentDescription = "Send",
                                    tint = if (canSend) Color.White else cs.onSurfaceVariant,
                                    size = 19.dp,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showClearDialog) {
            KlassConfirmDialog(
                title = "Clear local chat?",
                message = "This removes your conversation with $peerDisplayName from this device.",
                icon = TrashCanIcon,
                accent = errorAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                confirmText = "Clear",
                cancelText = "বাতিল".localized(),
                isDestructive = true,
                onConfirm = {
                    scope.launch {
                        clearLocalChatHistory(cleanViewer, cleanPeer)
                        messages = emptyList()
                        showClearDialog = false
                    }
                },
                onDismiss = { showClearDialog = false },
            )
        }
    }
}
