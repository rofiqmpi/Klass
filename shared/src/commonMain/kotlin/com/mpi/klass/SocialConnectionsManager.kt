package com.mpi.klass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ConnectionRelationStatus {
    NONE,
    OUTGOING_PENDING,
    INCOMING_PENDING,
    CONNECTED,
}

data class DirectoryMember(
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.STUDENT,
    val photoUrl: String? = null,
    val coverPhotoUrl: String? = null,
    val bio: String = "",
    val profile: StudentProfile = StudentProfile(),
    val updatedAt: Long = 0L,
)

data class ConnectionRecord(
    val id: String = "",
    val requesterEmail: String = "",
    val receiverEmail: String = "",
    val requesterName: String = "",
    val receiverName: String = "",
    val requesterPhotoUrl: String? = null,
    val receiverPhotoUrl: String? = null,
    val status: String = "PENDING", // "PENDING" or "ACCEPTED"
    val updatedAt: Long = 0L,
) {
    fun peerEmailFor(viewerEmail: String): String {
        val clean = viewerEmail.trim().lowercase()
        return if (requesterEmail.trim().lowercase() == clean) receiverEmail else requesterEmail
    }

    fun peerNameFor(viewerEmail: String): String {
        val clean = viewerEmail.trim().lowercase()
        return if (requesterEmail.trim().lowercase() == clean) {
            receiverName.ifBlank { receiverEmail.substringBefore("@") }
        } else {
            requesterName.ifBlank { requesterEmail.substringBefore("@") }
        }
    }

    fun peerPhotoFor(viewerEmail: String): String? {
        val clean = viewerEmail.trim().lowercase()
        return if (requesterEmail.trim().lowercase() == clean) receiverPhotoUrl else requesterPhotoUrl
    }
}

data class ChatMessageItem(
    val id: String = "",
    val senderEmail: String = "",
    val receiverEmail: String = "",
    val text: String = "",
    val sentAt: Long = 0L,
    val isMine: Boolean = false,
)

data class ChatThreadSummary(
    val peerEmail: String = "",
    val peerName: String = "",
    val peerPhotoUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val unreadCount: Int = 0,
)

object SocialChatState {
    var totalUnreadMessages by mutableStateOf(0)
        internal set

    var unreadByPeer by mutableStateOf<Map<String, Int>>(emptyMap())
        internal set

    var chatRevision by mutableStateOf(0)
        internal set

    var activePeerEmail by mutableStateOf<String?>(null)

    var connections by mutableStateOf<List<ConnectionRecord>>(emptyList())
        internal set

    var connectionsOwnerEmail by mutableStateOf<String?>(null)
        internal set

    fun publishConnections(ownerEmail: String, list: List<ConnectionRecord>) {
        val clean = ownerEmail.trim().lowercase()
        connectionsOwnerEmail = clean.ifBlank { null }
        connections = list.sortedByDescending { it.updatedAt }
    }

    fun clearConnections() {
        connectionsOwnerEmail = null
        connections = emptyList()
    }

    fun incomingRequestsCountFor(viewerEmail: String): Int {
        val clean = viewerEmail.trim().lowercase()
        if (clean.isBlank()) return 0
        if (connectionsOwnerEmail != null && connectionsOwnerEmail != clean) return 0
        return connections.count {
            !it.status.equals("ACCEPTED", ignoreCase = true) &&
                it.receiverEmail.trim().lowercase() == clean
        }
    }
}

fun buildPairConnectionId(emailA: String, emailB: String): String {
    val cleanA = emailA.trim().lowercase()
    val cleanB = emailB.trim().lowercase()
    val sorted = listOf(cleanA, cleanB).sorted()
    val raw = "${sorted[0]}__${sorted[1]}"
    val safe = raw.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    return if (safe.length <= 140) "conn_$safe" else "conn_${safe.take(100)}_${raw.hashCode().toUInt()}"
}

fun resolveConnectionRelation(
    viewerEmail: String,
    targetEmail: String,
    connections: List<ConnectionRecord>,
): Pair<ConnectionRelationStatus, ConnectionRecord?> {
    val cleanViewer = viewerEmail.trim().lowercase()
    val cleanTarget = targetEmail.trim().lowercase()
    if (cleanViewer.isBlank() || cleanTarget.isBlank()) return ConnectionRelationStatus.NONE to null

    val rec = connections.firstOrNull {
        val req = it.requesterEmail.trim().lowercase()
        val recEmail = it.receiverEmail.trim().lowercase()
        (req == cleanViewer && recEmail == cleanTarget) || (req == cleanTarget && recEmail == cleanViewer)
    } ?: return ConnectionRelationStatus.NONE to null

    val status = when {
        rec.status.equals("ACCEPTED", ignoreCase = true) -> ConnectionRelationStatus.CONNECTED
        rec.requesterEmail.trim().lowercase() == cleanViewer -> ConnectionRelationStatus.OUTGOING_PENDING
        else -> ConnectionRelationStatus.INCOMING_PENDING
    }
    return status to rec
}

expect suspend fun fetchCampusDirectoryMembers(viewerEmail: String): List<DirectoryMember>

expect suspend fun fetchConnectionsForUser(userEmail: String): List<ConnectionRecord>

expect suspend fun sendFriendRequest(
    fromEmail: String,
    fromName: String,
    fromPhotoUrl: String?,
    toMember: DirectoryMember,
): Result<ConnectionRecord>

expect suspend fun respondToFriendRequest(
    connectionId: String,
    accept: Boolean,
): Result<Unit>

expect suspend fun removeConnection(connectionId: String): Result<Unit>

expect suspend fun loadLocalChatMessages(ownerEmail: String, peerEmail: String): List<ChatMessageItem>

expect suspend fun loadLocalChatThreads(ownerEmail: String): List<ChatThreadSummary>

expect suspend fun markChatThreadRead(ownerEmail: String, peerEmail: String)

expect suspend fun clearLocalChatHistory(ownerEmail: String, peerEmail: String)

expect suspend fun sendEphemeralMessage(
    senderEmail: String,
    senderName: String,
    senderPhotoUrl: String?,
    receiverEmail: String,
    receiverName: String,
    receiverPhotoUrl: String?,
    text: String,
): Result<ChatMessageItem>

expect fun startChatRelayListener(userEmail: String)

expect fun stopChatRelayListener()
