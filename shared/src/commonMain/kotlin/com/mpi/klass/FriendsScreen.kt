package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.BackArrowIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.MessageBubbleIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.UserPlusIcon
import com.mpi.klass.theme.UsersGroupIcon
import kotlinx.coroutines.launch

/**
 * Dedicated Social Friends & Connections Screen (separated from Messenger).
 * Features:
 * - Prominent Incoming Friend Requests pinned at the top with immediate Accept & Decline buttons
 * - Tab filters: All People / Requests (N) / Friends (N)
 * - Campus member directory search
 * - Tap any member or requester to inspect their read-only Profile with Accept/Decline/Add/Message actions
 */
@Composable
fun FriendsScreen(
    viewerEmail: String,
    viewerName: String,
    viewerPhotoUrl: String?,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    members: List<DirectoryMember>,
    connections: List<ConnectionRecord>,
    isLoading: Boolean = false,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenChat: (DirectoryMember) -> Unit,
    onConnectionsChanged: (List<ConnectionRecord>) -> Unit = {},
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val currentLang = LocalAppLanguage.current
    val cleanViewer = viewerEmail.ifBlank { loadUserSession()?.email ?: "" }.trim().lowercase()
    val fbBlue = Color(0xFF0866FF)
    val errorAccent = KlassFeaturePalette.error(isDark)

    // 0 = Discover / All People, 1 = Requests, 2 = Friends
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var inspectedPeer by remember { mutableStateOf<DirectoryMember?>(null) }
    var busyPeerEmail by remember { mutableStateOf<String?>(null) }
    val unreadMessagesCount = SocialChatState.totalUnreadMessages

    LaunchedEffect(inspectedPeer) {
        onFullScreenOverlayChange(inspectedPeer != null)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    val incomingRequests = remember(connections, cleanViewer) {
        connections.filter {
            !it.status.equals("ACCEPTED", ignoreCase = true) &&
                it.receiverEmail.trim().lowercase() == cleanViewer
        }
    }
    val outgoingRequests = remember(connections, cleanViewer) {
        connections.filter {
            !it.status.equals("ACCEPTED", ignoreCase = true) &&
                it.requesterEmail.trim().lowercase() == cleanViewer
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
    val filteredMembers = remember(members, q) {
        if (q.isBlank()) members else members.filter { m ->
            m.name.contains(q, ignoreCase = true) ||
                m.email.contains(q, ignoreCase = true) ||
                m.profile.department.contains(q, ignoreCase = true) ||
                m.profile.bloodGroup.contains(q, ignoreCase = true) ||
                m.profile.rollNumber.contains(q, ignoreCase = true) ||
                m.role.name.contains(q, ignoreCase = true)
        }
    }
    val filteredFriends = remember(acceptedFriends, q) {
        if (q.isBlank()) acceptedFriends else acceptedFriends.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.email.contains(q, ignoreCase = true) ||
                it.profile.department.contains(q, ignoreCase = true)
        }
    }

    fun handleFriendAction(member: DirectoryMember) {
        if (cleanViewer.isBlank()) {
            showToast("অনুগ্রহ করে অ্যাকাউন্টে সাইন ইন করুন।".localized(currentLang))
            return
        }
        if (busyPeerEmail != null) return
        val (relation, record) = resolveConnectionRelation(cleanViewer, member.email, connections)
        busyPeerEmail = member.email
        scope.launch {
            try {
                when (relation) {
                    ConnectionRelationStatus.NONE -> {
                        val res = sendFriendRequest(
                            fromEmail = cleanViewer,
                            fromName = viewerName,
                            fromPhotoUrl = viewerPhotoUrl,
                            toMember = member,
                        )
                        val created = res.getOrNull()
                        if (res.isSuccess && created != null) {
                            val updated = connections.filterNot { it.id == created.id } + created
                            onConnectionsChanged(updated)
                            SocialChatState.publishConnections(cleanViewer, updated)
                            val toastText = if (created.status.equals("ACCEPTED", true)) {
                                "সংযুক্ত".localized(currentLang)
                            } else {
                                "অনুরোধ পাঠানো হয়েছে".localized(currentLang)
                            }
                            showToast(toastText)
                            onRefresh()
                        } else {
                            showToast(res.exceptionOrNull()?.message ?: "Request failed")
                        }
                    }
                    ConnectionRelationStatus.OUTGOING_PENDING -> {
                        val id = record?.id ?: buildPairConnectionId(cleanViewer, member.email)
                        val res = removeConnection(id)
                        if (res.isSuccess) {
                            val updated = connections.filterNot { it.id == id }
                            onConnectionsChanged(updated)
                            SocialChatState.publishConnections(cleanViewer, updated)
                            showToast("অনুরোধ বাতিল".localized(currentLang))
                            onRefresh()
                        } else {
                            showToast(res.exceptionOrNull()?.message ?: "Failed cancelling request")
                        }
                    }
                    ConnectionRelationStatus.INCOMING_PENDING -> {
                        val id = record?.id ?: buildPairConnectionId(cleanViewer, member.email)
                        val res = respondToFriendRequest(id, accept = true)
                        if (res.isSuccess) {
                            val now = System.currentTimeMillis()
                            val updated = connections.map {
                                if (it.id == id) it.copy(status = "ACCEPTED", updatedAt = now) else it
                            }
                            onConnectionsChanged(updated)
                            SocialChatState.publishConnections(cleanViewer, updated)
                            showToast("সংযুক্ত".localized(currentLang))
                            onRefresh()
                        } else {
                            showToast(res.exceptionOrNull()?.message ?: "Failed accepting request")
                        }
                    }
                    ConnectionRelationStatus.CONNECTED -> {
                        val id = record?.id ?: buildPairConnectionId(cleanViewer, member.email)
                        val res = removeConnection(id)
                        if (res.isSuccess) {
                            val updated = connections.filterNot { it.id == id }
                            onConnectionsChanged(updated)
                            SocialChatState.publishConnections(cleanViewer, updated)
                            showToast("Connection removed")
                            onRefresh()
                        } else {
                            showToast(res.exceptionOrNull()?.message ?: "Failed removing connection")
                        }
                    }
                }
            } finally {
                busyPeerEmail = null
            }
        }
    }

    fun handleIncomingDecision(rec: ConnectionRecord, accept: Boolean) {
        val peerEmail = rec.peerEmailFor(cleanViewer)
        if (busyPeerEmail != null) return
        busyPeerEmail = peerEmail
        scope.launch {
            try {
                val res = respondToFriendRequest(rec.id, accept = accept)
                if (res.isSuccess) {
                    val now = System.currentTimeMillis()
                    val updated = if (accept) {
                        connections.map { if (it.id == rec.id) it.copy(status = "ACCEPTED", updatedAt = now) else it }
                    } else {
                        connections.filterNot { it.id == rec.id }
                    }
                    onConnectionsChanged(updated)
                    SocialChatState.publishConnections(cleanViewer, updated)
                    showToast(if (accept) "সংযুক্ত".localized(currentLang) else "Request declined")
                    onRefresh()
                } else {
                    showToast(res.exceptionOrNull()?.message ?: "Failed updating request")
                }
            } finally {
                busyPeerEmail = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().rainbowScreenBackground(isDark, fbBlue)) {
        Column(modifier = Modifier.fillMaxSize()) {
            GoogleTopAppBar(
                title = "বন্ধু ও কানেকশন".localized(),
                hindSiliguri = hindSiliguri,
                onNavigationClick = onBack,
                navigationTestTag = "friends_back_button",
                actions = {
                    Box {
                        WelcomeMinimal3DIconButton(
                            icon = MessageBubbleIcon,
                            contentDescription = "মেসেজ".localized(),
                            isDark = isDark,
                            visualSize = 38.dp,
                            onClick = onOpenMessages,
                        )
                        if (unreadMessagesCount > 0) {
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
                    WelcomeMinimal3DIconButton(
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
                    placeholder = "সবাইকে খুঁজুন".localized(),
                    accent = fbBlue,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    testTag = "friends_search_input",
                    clearTestTag = "friends_search_clear",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(10.dp))

                val tabs = listOf(
                    Triple(0, "সবাই".localized(), 0),
                    Triple(1, "ফ্রেন্ড রিকোয়েস্ট".localized(), incomingRequests.size),
                    Triple(2, "কানেকশন".localized(), acceptedFriends.size),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tabs.forEach { (idx, label, count) ->
                        val chipLabel = if (count > 0 && idx > 0) "$label ($count)" else label
                        MinimalIconChip(
                            label = chipLabel,
                            selected = selectedTab == idx,
                            accent = if (idx == 1 && count > 0) KlassFeaturePalette.blood(isDark).primary else fbBlue,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "friends_tab_$idx",
                            onClick = { selectedTab = idx },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (isLoading && members.isEmpty()) {
                    KlassRowListSkeleton(
                        isDark = isDark,
                        showHeaderBar = true,
                        showActionPill = true,
                        badgeSize = 48.dp,
                        badgeCornerRadius = 24.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 820.dp)
                            .testTag("friends_skeleton_loader"),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 820.dp)
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(bottom = 28.dp),
                    ) {
                        when (selectedTab) {
                            // 0: All People (+ prominent Incoming Requests pinned at top when > 0!)
                            0 -> {
                                if (incomingRequests.isNotEmpty() && q.isBlank()) {
                                    GoogleSectionHeader(
                                        title = "${"ফ্রেন্ড রিকোয়েস্ট".localized()} (${incomingRequests.size})",
                                        accent = fbBlue,
                                        hindSiliguri = hindSiliguri,
                                    )
                                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                        incomingRequests.forEachIndexed { idx, rec ->
                                            val pEmail = rec.peerEmailFor(cleanViewer)
                                            val matchedMember = members.firstOrNull { it.email.equals(pEmail, true) }
                                                ?: DirectoryMember(
                                                    email = pEmail,
                                                    name = rec.peerNameFor(cleanViewer),
                                                    photoUrl = rec.peerPhotoFor(cleanViewer),
                                                )
                                            IncomingRequestRow(
                                                member = matchedMember,
                                                isBusy = busyPeerEmail == matchedMember.email,
                                                isDark = isDark,
                                                hindSiliguri = hindSiliguri,
                                                fbBlue = fbBlue,
                                                declineAccent = errorAccent.primary,
                                                onInspect = { inspectedPeer = matchedMember },
                                                onAccept = { handleIncomingDecision(rec, accept = true) },
                                                onDecline = { handleIncomingDecision(rec, accept = false) },
                                            )
                                            if (idx < incomingRequests.lastIndex) {
                                                SocialInsetDivider(startIndent = 76.dp)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                if (filteredMembers.isEmpty()) {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                        KlassEmptyState(
                                            icon = UsersGroupIcon,
                                            title = "No members found",
                                            subtitle = "Try searching with another name, department, or blood group.",
                                            accent = fbBlue,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                        )
                                    }
                                } else {
                                    GoogleSectionHeader(
                                        title = "${filteredMembers.size} Campus Members",
                                        accent = fbBlue,
                                        hindSiliguri = hindSiliguri,
                                    )
                                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                        filteredMembers.forEachIndexed { idx, member ->
                                            val (rel, rec) = resolveConnectionRelation(cleanViewer, member.email, connections)
                                            if (rel == ConnectionRelationStatus.INCOMING_PENDING && rec != null) {
                                                IncomingRequestRow(
                                                    member = member,
                                                    isBusy = busyPeerEmail == member.email,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    fbBlue = fbBlue,
                                                    declineAccent = errorAccent.primary,
                                                    onInspect = { inspectedPeer = member },
                                                    onAccept = { handleIncomingDecision(rec, accept = true) },
                                                    onDecline = { handleIncomingDecision(rec, accept = false) },
                                                )
                                            } else {
                                                val btnText = when (rel) {
                                                    ConnectionRelationStatus.NONE -> "বন্ধু যোগ করুন".localized()
                                                    ConnectionRelationStatus.OUTGOING_PENDING -> "অনুরোধ বাতিল".localized()
                                                    ConnectionRelationStatus.INCOMING_PENDING -> "গ্রহণ করুন".localized()
                                                    ConnectionRelationStatus.CONNECTED -> "সংযুক্ত".localized()
                                                }
                                                FriendsMemberRow(
                                                    member = member,
                                                    relationStatus = rel,
                                                    isBusy = busyPeerEmail == member.email,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    fbBlue = fbBlue,
                                                    primaryButtonText = btnText,
                                                    onRowClick = { inspectedPeer = member },
                                                    onPrimaryAction = { handleFriendAction(member) },
                                                    onQuickMessageClick = { onOpenChat(member) },
                                                )
                                            }
                                            if (idx < filteredMembers.lastIndex) {
                                                SocialInsetDivider(startIndent = 76.dp)
                                            }
                                        }
                                    }
                                }
                            }

                            // 1: Requests (Incoming + Sent)
                            1 -> {
                                if (incomingRequests.isEmpty() && outgoingRequests.isEmpty()) {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                        KlassEmptyState(
                                            icon = UserPlusIcon,
                                            title = "ফ্রেন্ড রিকোয়েস্ট".localized(),
                                            subtitle = "No pending friend requests right now.",
                                            accent = fbBlue,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                            actionLabel = "সবাইকে খুঁজুন".localized(),
                                            onActionClick = { selectedTab = 0 },
                                        )
                                    }
                                } else {
                                    if (incomingRequests.isNotEmpty()) {
                                        GoogleSectionHeader(
                                            title = "Received (${incomingRequests.size})",
                                            accent = fbBlue,
                                            hindSiliguri = hindSiliguri,
                                        )
                                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                            incomingRequests.forEachIndexed { idx, rec ->
                                                val pEmail = rec.peerEmailFor(cleanViewer)
                                                val matchedMember = members.firstOrNull { it.email.equals(pEmail, true) }
                                                    ?: DirectoryMember(
                                                        email = pEmail,
                                                        name = rec.peerNameFor(cleanViewer),
                                                        photoUrl = rec.peerPhotoFor(cleanViewer),
                                                    )
                                                IncomingRequestRow(
                                                    member = matchedMember,
                                                    isBusy = busyPeerEmail == matchedMember.email,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    fbBlue = fbBlue,
                                                    declineAccent = errorAccent.primary,
                                                    onInspect = { inspectedPeer = matchedMember },
                                                    onAccept = { handleIncomingDecision(rec, accept = true) },
                                                    onDecline = { handleIncomingDecision(rec, accept = false) },
                                                )
                                                if (idx < incomingRequests.lastIndex) {
                                                    SocialInsetDivider(startIndent = 76.dp)
                                                }
                                            }
                                        }
                                    }

                                    if (outgoingRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        GoogleSectionHeader(
                                            title = "Sent Requests (${outgoingRequests.size})",
                                            accent = cs.onSurfaceVariant,
                                            hindSiliguri = hindSiliguri,
                                        )
                                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                            outgoingRequests.forEachIndexed { idx, rec ->
                                                val pEmail = rec.peerEmailFor(cleanViewer)
                                                val matchedMember = members.firstOrNull { it.email.equals(pEmail, true) }
                                                    ?: DirectoryMember(
                                                        email = pEmail,
                                                        name = rec.peerNameFor(cleanViewer),
                                                        photoUrl = rec.peerPhotoFor(cleanViewer),
                                                    )
                                                FriendsMemberRow(
                                                    member = matchedMember,
                                                    relationStatus = ConnectionRelationStatus.OUTGOING_PENDING,
                                                    isBusy = busyPeerEmail == matchedMember.email,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    fbBlue = fbBlue,
                                                    primaryButtonText = "অনুরোধ বাতিল".localized(),
                                                    onRowClick = { inspectedPeer = matchedMember },
                                                    onPrimaryAction = { handleFriendAction(matchedMember) },
                                                    onQuickMessageClick = { onOpenChat(matchedMember) },
                                                )
                                                if (idx < outgoingRequests.lastIndex) {
                                                    SocialInsetDivider(startIndent = 76.dp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2: Accepted Connections / Friends
                            else -> {
                                if (filteredFriends.isEmpty()) {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                        KlassEmptyState(
                                            icon = UsersGroupIcon,
                                            title = "কোনো কানেকশন নেই",
                                            subtitle = "Search campus people and send friend requests to build your network.",
                                            accent = fbBlue,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                            actionLabel = "সবাইকে খুঁজুন".localized(),
                                            onActionClick = { selectedTab = 0 },
                                        )
                                    }
                                } else {
                                    GoogleSectionHeader(
                                        title = "${filteredFriends.size} কানেকশন".localized(),
                                        accent = fbBlue,
                                        hindSiliguri = hindSiliguri,
                                    )
                                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                        filteredFriends.forEachIndexed { idx, friend ->
                                            FriendsMemberRow(
                                                member = friend,
                                                relationStatus = ConnectionRelationStatus.CONNECTED,
                                                isBusy = busyPeerEmail == friend.email,
                                                isDark = isDark,
                                                hindSiliguri = hindSiliguri,
                                                fbBlue = fbBlue,
                                                primaryButtonText = "মেসেজ".localized(),
                                                onRowClick = { inspectedPeer = friend },
                                                onPrimaryAction = { onOpenChat(friend) },
                                                onQuickMessageClick = { onOpenChat(friend) },
                                            )
                                            if (idx < filteredFriends.lastIndex) {
                                                SocialInsetDivider(startIndent = 76.dp)
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

        // Level 1 Overlay: Strictly Read-Only Peer Profile Viewer
        val activePeer = inspectedPeer
        KlassSubScreenHost(visible = activePeer != null, zIndex = 60f) {
            if (activePeer != null) {
                var peerActiveCategory by remember(activePeer.email) { mutableStateOf<String?>(null) }
                val (relStatus, peerRecord) = resolveConnectionRelation(cleanViewer, activePeer.email, connections)

                AppBackHandler(enabled = true) {
                    if (peerActiveCategory != null) {
                        peerActiveCategory = null
                    } else {
                        inspectedPeer = null
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    FacebookProfileContent(
                        profile = activePeer.profile,
                        photoUrl = activePeer.photoUrl,
                        coverPhotoUrl = activePeer.coverPhotoUrl,
                        fallbackName = activePeer.name,
                        cleanEmail = activePeer.email,
                        role = activePeer.role,
                        isOwnProfile = false,
                        relationStatus = relStatus,
                        isRelationBusy = busyPeerEmail == activePeer.email,
                        connections = emptyList(),
                        directoryMembers = members,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        activeDetailCategory = peerActiveCategory,
                        onSelectDetailCategory = { peerActiveCategory = it },
                        onFriendAction = { handleFriendAction(activePeer) },
                        onDeclineFriendRequest = if (relStatus == ConnectionRelationStatus.INCOMING_PENDING && peerRecord != null) {
                            { handleIncomingDecision(peerRecord, accept = false) }
                        } else {
                            null
                        },
                        onMessageClick = { onOpenChat(activePeer) },
                    )

                    AnimatedVisibility(
                        visible = peerActiveCategory == null,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(8f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.50f),
                                            Color.Black.copy(alpha = 0.16f),
                                            Color.Transparent,
                                        )
                                    )
                                ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .height(56.dp)
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    onClick = { inspectedPeer = null },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.36f),
                                    border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.28f)),
                                    shadowElevation = 0.dp,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(40.dp)
                                        .testTag("peer_profile_back_button"),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = BackArrowIcon,
                                            contentDescription = "Back",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
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
private fun IncomingRequestRow(
    member: DirectoryMember,
    isBusy: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    fbBlue: Color,
    declineAccent: Color,
    onInspect: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val displayName = member.name.ifBlank { member.email.substringBefore("@") }
    val deptShort = RoutineRules.normalizeDepartmentShort(member.profile.department)
    val roleLabel = when (member.role) {
        UserRole.STUDENT -> "Student"
        UserRole.TEACHER -> "Teacher"
        UserRole.ADMIN -> "Admin"
    }
    val metaLine = listOfNotNull(
        roleLabel,
        deptShort.takeIf { it.isNotBlank() },
        member.email.takeIf { it.isNotBlank() },
    ).joinToString(" • ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onInspect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MemberAvatarCircle(
            email = member.email,
            name = displayName,
            photoUrl = member.photoUrl,
            size = 48.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
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
                fontSize = 12.5.sp,
                fontFamily = hindSiliguri,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderSavePillButton(
                    text = "গ্রহণ করুন".localized(),
                    accent = fbBlue,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    isLoading = isBusy,
                    enabled = !isBusy,
                    testTag = "accept_request_${member.email}",
                    onClick = onAccept,
                )
                KlassTonalButton(
                    text = "প্রত্যাখ্যান".localized(),
                    accent = declineAccent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 36.dp,
                    enabled = !isBusy,
                    testTag = "decline_request_${member.email}",
                    onClick = onDecline,
                )
            }
        }
    }
}

@Composable
private fun FriendsMemberRow(
    member: DirectoryMember,
    relationStatus: ConnectionRelationStatus,
    isBusy: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    fbBlue: Color,
    primaryButtonText: String,
    onRowClick: () -> Unit,
    onPrimaryAction: () -> Unit,
    onQuickMessageClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val displayName = member.name.ifBlank { member.email.substringBefore("@") }
    val deptShort = RoutineRules.normalizeDepartmentShort(member.profile.department)
    val roleLabel = when (member.role) {
        UserRole.STUDENT -> "Student"
        UserRole.TEACHER -> "Teacher"
        UserRole.ADMIN -> "Admin"
    }
    val metaLine = listOfNotNull(
        roleLabel,
        deptShort.takeIf { it.isNotBlank() },
        member.profile.bloodGroup.takeIf { it.isNotBlank() },
    ).joinToString(" • ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MemberAvatarCircle(
            email = member.email,
            name = displayName,
            photoUrl = member.photoUrl,
            size = 48.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
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
        if (relationStatus != ConnectionRelationStatus.CONNECTED) {
            Surface(
                onClick = onQuickMessageClick,
                shape = CircleShape,
                color = fbBlue.copy(alpha = if (isDark) 0.18f else 0.10f),
                border = BorderStroke(0.75.dp, fbBlue.copy(alpha = 0.30f)),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(36.dp)
                    .testTag("directory_message_${member.email}"),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = MessageBubbleIcon,
                        contentDescription = "Message $displayName",
                        tint = fbBlue,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        val isPrimaryAction = relationStatus == ConnectionRelationStatus.NONE ||
            relationStatus == ConnectionRelationStatus.CONNECTED
        if (isPrimaryAction) {
            HeaderSavePillButton(
                text = primaryButtonText,
                accent = fbBlue,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                isLoading = isBusy,
                enabled = !isBusy,
                testTag = "directory_action_${member.email}",
                onClick = onPrimaryAction,
            )
        } else {
            KlassTonalButton(
                text = primaryButtonText,
                accent = cs.onSurfaceVariant,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                height = 36.dp,
                enabled = !isBusy,
                testTag = "directory_action_${member.email}",
                onClick = onPrimaryAction,
            )
        }
    }
}
