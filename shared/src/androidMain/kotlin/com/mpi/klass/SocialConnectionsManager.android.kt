package com.mpi.klass

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.mpi.klass.theme.getAppContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

private const val SOCIAL_TAG = "SocialChat"
private const val SOCIAL_PREFS = "klass_social_cache"
private val chatIoLock = Any()
private val relayScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
private val activeRelayListeners = mutableListOf<ListenerRegistration>()
private var activeRelayEmail: String? = null

private fun getSocialFirestoreInstances(): List<FirebaseFirestore> {
    val ctx = getAppContext()
    val customDbId = if (ctx != null) {
        val resId = ctx.resources.getIdentifier("firestore_database_id", "string", ctx.packageName)
        if (resId != 0) ctx.getString(resId).trim().takeIf { it.isNotEmpty() && it != "(default)" } else null
    } else null

    if (customDbId != null) {
        try {
            return listOf(FirebaseFirestore.getInstance(customDbId))
        } catch (e: Exception) {
            Log.e(SOCIAL_TAG, "Failed custom db ($customDbId): ${e.message}")
        }
    }
    return try {
        listOf(FirebaseFirestore.getInstance())
    } catch (e: Exception) {
        Log.e(SOCIAL_TAG, "Failed default db: ${e.message}")
        emptyList()
    }
}

private fun sanitizeRelayPhotoUrl(url: String?): String {
    val clean = url?.trim().orEmpty()
    if (clean.isEmpty()) return ""
    // Never duplicate large base64 data:image strings into connections/chat_relay docs (1MB Firestore limit)
    if (clean.startsWith("data:", ignoreCase = true) || clean.length > 500) return ""
    return clean
}

private fun sha256Short(value: String): String {
    val bytes = MessageDigest.getInstance("SHA-256")
        .digest(value.trim().lowercase().toByteArray(Charsets.UTF_8))
    return bytes.take(12).joinToString("") { "%02x".format(it) }
}

private fun chatDirForOwner(ownerEmail: String): File? {
    val ctx = getAppContext() ?: return null
    val root = File(ctx.filesDir, "klass_chats/${sha256Short(ownerEmail)}")
    if (!root.exists()) root.mkdirs()
    return root
}

private fun chatMessagesFile(ownerEmail: String, peerEmail: String): File? {
    val dir = chatDirForOwner(ownerEmail) ?: return null
    return File(dir, "msg_${sha256Short(peerEmail)}.json")
}

private fun chatThreadsIndexFile(ownerEmail: String): File? {
    val dir = chatDirForOwner(ownerEmail) ?: return null
    return File(dir, "threads_index.json")
}

private fun readJsonMessagesLocked(file: File, ownerEmail: String): MutableList<ChatMessageItem> {
    if (!file.exists()) return mutableListOf()
    val cleanOwner = ownerEmail.trim().lowercase()
    return try {
        val raw = file.readText(Charsets.UTF_8)
        if (raw.isBlank()) return mutableListOf()
        val arr = JSONArray(raw)
        val out = mutableListOf<ChatMessageItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id").trim()
            val sender = obj.optString("sender_email").trim().lowercase()
            val receiver = obj.optString("receiver_email").trim().lowercase()
            val text = obj.optString("text")
            val sentAt = obj.optLong("sent_at", 0L)
            if (id.isNotBlank() && text.isNotBlank()) {
                out.add(
                    ChatMessageItem(
                        id = id,
                        senderEmail = sender,
                        receiverEmail = receiver,
                        text = text,
                        sentAt = sentAt,
                        isMine = sender == cleanOwner,
                    )
                )
            }
        }
        out.sortBy { it.sentAt }
        out
    } catch (_: Exception) {
        mutableListOf()
    }
}

private fun writeJsonMessagesLocked(file: File, items: List<ChatMessageItem>) {
    try {
        val arr = JSONArray()
        items.sortedBy { it.sentAt }.takeLast(400).forEach { msg ->
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("sender_email", msg.senderEmail.trim().lowercase())
            obj.put("receiver_email", msg.receiverEmail.trim().lowercase())
            obj.put("text", msg.text)
            obj.put("sent_at", msg.sentAt)
            arr.put(obj)
        }
        file.writeText(arr.toString(), Charsets.UTF_8)
    } catch (e: Exception) {
        Log.w(SOCIAL_TAG, "Failed writing local chat messages: ${e.message}")
    }
}

private fun readThreadsIndexLocked(file: File): MutableMap<String, ChatThreadSummary> {
    if (!file.exists()) return linkedMapOf()
    return try {
        val raw = file.readText(Charsets.UTF_8)
        if (raw.isBlank()) return linkedMapOf()
        val arr = JSONArray(raw)
        val map = linkedMapOf<String, ChatThreadSummary>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val peer = obj.optString("peer_email").trim().lowercase()
            if (peer.isBlank()) continue
            map[peer] = ChatThreadSummary(
                peerEmail = peer,
                peerName = obj.optString("peer_name").ifBlank { peer.substringBefore("@") },
                peerPhotoUrl = obj.optString("peer_photo_url").ifBlank { null },
                lastMessage = obj.optString("last_message"),
                lastMessageAt = obj.optLong("last_message_at", 0L),
                unreadCount = obj.optInt("unread_count", 0).coerceAtLeast(0),
            )
        }
        map
    } catch (_: Exception) {
        linkedMapOf()
    }
}

private fun writeThreadsIndexLocked(file: File, map: Map<String, ChatThreadSummary>) {
    try {
        val arr = JSONArray()
        map.values.sortedByDescending { it.lastMessageAt }.forEach { thread ->
            val obj = JSONObject()
            obj.put("peer_email", thread.peerEmail.trim().lowercase())
            obj.put("peer_name", thread.peerName)
            obj.put("peer_photo_url", thread.peerPhotoUrl ?: "")
            obj.put("last_message", thread.lastMessage)
            obj.put("last_message_at", thread.lastMessageAt)
            obj.put("unread_count", thread.unreadCount)
            arr.put(obj)
        }
        file.writeText(arr.toString(), Charsets.UTF_8)
    } catch (e: Exception) {
        Log.w(SOCIAL_TAG, "Failed writing threads index: ${e.message}")
    }
}

private fun publishUnreadSummaryLocked(threads: Map<String, ChatThreadSummary>) {
    val unreadMap = threads
        .filterValues { it.unreadCount > 0 }
        .mapValues { it.value.unreadCount }
    val total = unreadMap.values.sum()
    relayScope.launch(Dispatchers.Main) {
        SocialChatState.unreadByPeer = unreadMap
        SocialChatState.totalUnreadMessages = total
        SocialChatState.chatRevision++
    }
}

private fun appendMessageLocally(
    ownerEmail: String,
    peerEmail: String,
    peerName: String,
    peerPhotoUrl: String?,
    msg: ChatMessageItem,
    incrementUnreadIfIncoming: Boolean,
): Boolean {
    val cleanOwner = ownerEmail.trim().lowercase()
    val cleanPeer = peerEmail.trim().lowercase()
    if (cleanOwner.isBlank() || cleanPeer.isBlank()) return false

    synchronized(chatIoLock) {
        val msgFile = chatMessagesFile(cleanOwner, cleanPeer) ?: return false
        val indexFile = chatThreadsIndexFile(cleanOwner) ?: return false

        val list = readJsonMessagesLocked(msgFile, cleanOwner)
        if (list.any { it.id == msg.id }) {
            return false
        }
        list.add(msg)
        writeJsonMessagesLocked(msgFile, list)

        val threads = readThreadsIndexLocked(indexFile)
        val existing = threads[cleanPeer]
        val isUnread = incrementUnreadIfIncoming &&
            !msg.isMine &&
            SocialChatState.activePeerEmail?.trim()?.lowercase() != cleanPeer
        val nextUnread = if (isUnread) (existing?.unreadCount ?: 0) + 1 else (existing?.unreadCount ?: 0)
        threads[cleanPeer] = ChatThreadSummary(
            peerEmail = cleanPeer,
            peerName = peerName.ifBlank { existing?.peerName ?: cleanPeer.substringBefore("@") },
            peerPhotoUrl = peerPhotoUrl?.ifBlank { null } ?: existing?.peerPhotoUrl,
            lastMessage = msg.text,
            lastMessageAt = maxOf(existing?.lastMessageAt ?: 0L, msg.sentAt),
            unreadCount = nextUnread,
        )
        writeThreadsIndexLocked(indexFile, threads)
        publishUnreadSummaryLocked(threads)
        return true
    }
}

actual suspend fun fetchCampusDirectoryMembers(viewerEmail: String): List<DirectoryMember> =
    withContext(Dispatchers.IO) {
        val cleanViewer = viewerEmail.trim().lowercase()
        val byEmail = linkedMapOf<String, DirectoryMember>()

        fun mergeMember(incoming: DirectoryMember) {
            val em = incoming.email.trim().lowercase()
            if (em.isBlank() || em == cleanViewer) return
            val existing = byEmail[em]
            if (existing == null) {
                byEmail[em] = incoming.copy(email = em)
            } else {
                val mergedPhoto = when {
                    !incoming.photoUrl.isNullOrBlank() && incoming.photoUrl.startsWith("data:image", ignoreCase = true) -> incoming.photoUrl
                    !existing.photoUrl.isNullOrBlank() && existing.photoUrl.startsWith("data:image", ignoreCase = true) -> existing.photoUrl
                    else -> incoming.photoUrl?.ifBlank { null } ?: existing.photoUrl
                }
                val mergedCover = when {
                    !incoming.coverPhotoUrl.isNullOrBlank() && incoming.coverPhotoUrl.startsWith("data:image", ignoreCase = true) -> incoming.coverPhotoUrl
                    !existing.coverPhotoUrl.isNullOrBlank() && existing.coverPhotoUrl.startsWith("data:image", ignoreCase = true) -> existing.coverPhotoUrl
                    else -> incoming.coverPhotoUrl?.ifBlank { null } ?: existing.coverPhotoUrl
                }
                val pIn = incoming.profile
                val pEx = existing.profile
                val mergedProfile = StudentProfile(
                    email = em,
                    name = pIn.name.ifBlank { pEx.name },
                    bio = pIn.bio.ifBlank { pEx.bio },
                    photoUrl = mergedPhoto,
                    coverPhotoUrl = mergedCover,
                    semester = pIn.semester.ifBlank { pEx.semester },
                    shift = pIn.shift.ifBlank { pEx.shift },
                    department = pIn.department.ifBlank { pEx.department },
                    section = pIn.section.ifBlank { pEx.section },
                    phone = pIn.phone.ifBlank { pEx.phone },
                    bloodGroup = pIn.bloodGroup.ifBlank { pEx.bloodGroup },
                    address = pIn.address.ifBlank { pEx.address },
                    rollNumber = pIn.rollNumber.ifBlank { pEx.rollNumber },
                    fatherName = pIn.fatherName.ifBlank { pEx.fatherName },
                    motherName = pIn.motherName.ifBlank { pEx.motherName },
                    dob = pIn.dob.ifBlank { pEx.dob },
                    religion = pIn.religion.ifBlank { pEx.religion },
                    gender = pIn.gender.ifBlank { pEx.gender },
                    maritalStatus = pIn.maritalStatus.ifBlank { pEx.maritalStatus },
                    contactEmail = pIn.contactEmail.ifBlank { pEx.contactEmail },
                    permanentAddress = pIn.permanentAddress.ifBlank { pEx.permanentAddress },
                    registrationNumber = pIn.registrationNumber.ifBlank { pEx.registrationNumber },
                    profileSetupComplete = pIn.profileSetupComplete || pEx.profileSetupComplete,
                    registeredAt = maxOf(pIn.registeredAt, pEx.registeredAt),
                    fieldPrivacy = pEx.fieldPrivacy + pIn.fieldPrivacy,
                )
                val resolvedRole = when {
                    incoming.role == UserRole.ADMIN || existing.role == UserRole.ADMIN -> UserRole.ADMIN
                    incoming.role == UserRole.TEACHER || existing.role == UserRole.TEACHER -> UserRole.TEACHER
                    else -> UserRole.STUDENT
                }
                byEmail[em] = existing.copy(
                    name = incoming.name.ifBlank { existing.name },
                    role = resolvedRole,
                    photoUrl = mergedPhoto,
                    coverPhotoUrl = mergedCover,
                    bio = incoming.bio.ifBlank { existing.bio },
                    profile = mergedProfile,
                    updatedAt = maxOf(existing.updatedAt, incoming.updatedAt),
                )
            }
        }

        fun memberFromDoc(doc: DocumentSnapshot, fallbackRole: UserRole?): DirectoryMember? {
            val d = doc.data ?: return null
            val em = ((d["email"] as? String) ?: doc.id).trim().lowercase()
            if (em.isBlank() || !em.contains("@")) return null
            val roleRaw = (d["role"] as? String) ?: (d["category"] as? String)
            val parsedRole = UserRole.fromRaw(roleRaw) ?: fallbackRole ?: UserRole.STUDENT
            val privacyMap = decodeFieldPrivacyMap(d["field_privacy"])
            val photo = (d["photo_url"] as? String)?.ifBlank { null }
            val cover = (d["cover_photo_url"] as? String)?.ifBlank { null }
            val name = (d["name"] as? String)?.trim() ?: ""
            val bio = (d["bio"] as? String)?.trim() ?: ""
            val sp = StudentProfile(
                email = em,
                name = name,
                bio = bio,
                photoUrl = photo,
                coverPhotoUrl = cover,
                semester = (d["semester"] as? String)?.trim() ?: "",
                shift = (d["shift"] as? String)?.trim() ?: "",
                department = (d["department"] as? String)?.trim() ?: "",
                section = (d["section"] as? String)?.trim() ?: "",
                phone = (d["phone"] as? String)?.trim() ?: "",
                bloodGroup = (d["blood_group"] as? String)?.trim() ?: "",
                address = (d["address"] as? String)?.trim() ?: "",
                rollNumber = (d["roll_number"] as? String)?.trim() ?: "",
                fatherName = (d["father_name"] as? String)?.trim() ?: "",
                motherName = (d["mother_name"] as? String)?.trim() ?: "",
                dob = (d["dob"] as? String)?.trim() ?: "",
                religion = (d["religion"] as? String)?.trim() ?: "",
                gender = (d["gender"] as? String)?.trim() ?: "",
                maritalStatus = (d["marital_status"] as? String)?.trim() ?: "",
                contactEmail = (d["contact_email"] as? String)?.trim() ?: "",
                permanentAddress = (d["permanent_address"] as? String)?.trim() ?: "",
                registrationNumber = (d["registration_number"] as? String)?.trim() ?: "",
                profileSetupComplete = (d["profile_setup_complete"] as? Boolean) ?: false,
                registeredAt = (d["registered_at"] as? Number)?.toLong() ?: 0L,
                fieldPrivacy = privacyMap,
            )
            return DirectoryMember(
                email = em,
                name = name.ifBlank { em.substringBefore("@") },
                role = parsedRole,
                photoUrl = photo,
                coverPhotoUrl = cover,
                bio = bio,
                profile = sp,
                updatedAt = (d["updated_at"] as? Number)?.toLong() ?: 0L,
            )
        }

        withTimeoutOrNull(8.seconds) {
            for (db in getSocialFirestoreInstances()) {
                val sources = listOf(
                    "users" to null,
                    "students" to UserRole.STUDENT,
                    "teachers" to UserRole.TEACHER,
                    "admins" to UserRole.ADMIN,
                )
                for ((col, fallbackRole) in sources) {
                    try {
                        val snap = db.collection(col).get().awaitTask()
                        for (doc in snap.documents) {
                            val m = memberFromDoc(doc, fallbackRole) ?: continue
                            mergeMember(m)
                        }
                    } catch (e: Exception) {
                        Log.w(SOCIAL_TAG, "Directory fetch warning on $col: ${e.message}")
                    }
                }
            }
        }

        byEmail.values
            .map { member ->
                member.copy(name = member.name.ifBlank { member.email.substringBefore("@") })
            }
            .sortedWith(
                compareByDescending<DirectoryMember> { it.updatedAt }
                    .thenBy { it.name.lowercase() }
            )
    }

private fun parseConnectionDoc(doc: DocumentSnapshot): ConnectionRecord? {
    val d = doc.data ?: return null
    val id = (d["id"] as? String)?.ifBlank { doc.id } ?: doc.id
    val req = (d["requester_email"] as? String)?.trim()?.lowercase() ?: return null
    val rec = (d["receiver_email"] as? String)?.trim()?.lowercase() ?: return null
    val status = (d["status"] as? String)?.uppercase() ?: "PENDING"
    return ConnectionRecord(
        id = id,
        requesterEmail = req,
        receiverEmail = rec,
        requesterName = (d["requester_name"] as? String)?.trim() ?: req.substringBefore("@"),
        receiverName = (d["receiver_name"] as? String)?.trim() ?: rec.substringBefore("@"),
        requesterPhotoUrl = (d["requester_photo_url"] as? String)?.ifBlank { null },
        receiverPhotoUrl = (d["receiver_photo_url"] as? String)?.ifBlank { null },
        status = status,
        updatedAt = (d["updated_at"] as? Number)?.toLong() ?: 0L,
    )
}

actual suspend fun fetchConnectionsForUser(userEmail: String): List<ConnectionRecord> =
    withContext(Dispatchers.IO) {
        val cleanEmail = userEmail.trim().lowercase()
        if (cleanEmail.isBlank()) return@withContext emptyList()

        val resultMap = linkedMapOf<String, ConnectionRecord>()
        var anyDbSucceeded = false
        withTimeoutOrNull(10.seconds) {
            for (db in getSocialFirestoreInstances()) {
                try {
                    val snap = db.collection("connections")
                        .whereArrayContains("participants", cleanEmail)
                        .get()
                        .awaitTask()
                    anyDbSucceeded = true
                    for (doc in snap.documents) {
                        val parsed = parseConnectionDoc(doc) ?: continue
                        resultMap[parsed.id] = parsed
                    }
                } catch (e: Exception) {
                    Log.w(SOCIAL_TAG, "Connections fetch warning: ${e.message}")
                }
            }
        }

        val resolved = if (anyDbSucceeded) {
            resultMap.values.sortedByDescending { it.updatedAt }.also {
                saveLocalConnectionsCache(cleanEmail, it)
            }
        } else {
            readLocalConnectionsCache(cleanEmail)
        }
        relayScope.launch(Dispatchers.Main) {
            SocialChatState.publishConnections(cleanEmail, resolved)
        }
        return@withContext resolved
    }

private fun saveLocalConnectionsCache(userEmail: String, list: List<ConnectionRecord>) {
    val prefs = getAppContext()?.getSharedPreferences(SOCIAL_PREFS, Context.MODE_PRIVATE) ?: return
    val clean = userEmail.trim().lowercase()
    val arr = JSONArray()
    list.forEach { c ->
        val obj = JSONObject()
        obj.put("id", c.id)
        obj.put("requester_email", c.requesterEmail)
        obj.put("receiver_email", c.receiverEmail)
        obj.put("requester_name", c.requesterName)
        obj.put("receiver_name", c.receiverName)
        obj.put("requester_photo_url", c.requesterPhotoUrl ?: "")
        obj.put("receiver_photo_url", c.receiverPhotoUrl ?: "")
        obj.put("status", c.status)
        obj.put("updated_at", c.updatedAt)
        arr.put(obj)
    }
    prefs.edit().putString("${clean}_connections", arr.toString()).apply()
}

private fun readLocalConnectionsCache(userEmail: String): List<ConnectionRecord> {
    val prefs = getAppContext()?.getSharedPreferences(SOCIAL_PREFS, Context.MODE_PRIVATE) ?: return emptyList()
    val clean = userEmail.trim().lowercase()
    val raw = prefs.getString("${clean}_connections", null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        val out = mutableListOf<ConnectionRecord>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            out.add(
                ConnectionRecord(
                    id = obj.optString("id"),
                    requesterEmail = obj.optString("requester_email"),
                    receiverEmail = obj.optString("receiver_email"),
                    requesterName = obj.optString("requester_name"),
                    receiverName = obj.optString("receiver_name"),
                    requesterPhotoUrl = obj.optString("requester_photo_url").ifBlank { null },
                    receiverPhotoUrl = obj.optString("receiver_photo_url").ifBlank { null },
                    status = obj.optString("status", "PENDING"),
                    updatedAt = obj.optLong("updated_at", 0L),
                )
            )
        }
        out
    } catch (_: Exception) {
        emptyList()
    }
}

private fun upsertLocalConnectionCache(ownerEmail: String, record: ConnectionRecord) {
    val clean = ownerEmail.trim().lowercase()
    if (clean.isBlank()) return
    val updated = (readLocalConnectionsCache(clean).filterNot { it.id == record.id } + record)
        .sortedByDescending { it.updatedAt }
    saveLocalConnectionsCache(clean, updated)
    if (SocialChatState.connectionsOwnerEmail == clean) {
        relayScope.launch(Dispatchers.Main) {
            SocialChatState.publishConnections(clean, updated)
        }
    }
}

private fun updateLocalConnectionStatusForActiveUsers(connectionId: String, accept: Boolean) {
    val prefs = getAppContext()?.getSharedPreferences(SOCIAL_PREFS, Context.MODE_PRIVATE) ?: return
    val now = System.currentTimeMillis()
    val keys = prefs.all.keys.filter { it.endsWith("_connections") }
    for (k in keys) {
        val owner = k.removeSuffix("_connections")
        val current = readLocalConnectionsCache(owner)
        if (current.any { it.id == connectionId }) {
            val next = if (accept) {
                current.map { if (it.id == connectionId) it.copy(status = "ACCEPTED", updatedAt = now) else it }
            } else {
                current.filterNot { it.id == connectionId }
            }
            saveLocalConnectionsCache(owner, next)
            if (SocialChatState.connectionsOwnerEmail == owner) {
                relayScope.launch(Dispatchers.Main) {
                    SocialChatState.publishConnections(owner, next)
                }
            }
        }
    }
}

private fun formatFirestoreActionError(e: Exception?, fallback: String): String {
    if (FirebaseAuth.getInstance().currentUser == null) {
        return "Cloud session expired. Please sign in with Google to send friend requests."
    }
    val ffe = e as? FirebaseFirestoreException
    return when (ffe?.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Permission denied. Please sign in again to sync."
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
            "Network slow or offline. Please check internet and try again."
        else -> e?.message?.takeIf { it.isNotBlank() } ?: fallback
    }
}

actual suspend fun sendFriendRequest(
    fromEmail: String,
    fromName: String,
    fromPhotoUrl: String?,
    toMember: DirectoryMember,
): Result<ConnectionRecord> = withContext(Dispatchers.IO) {
    val cleanFrom = fromEmail.trim().lowercase()
    val cleanTo = toMember.email.trim().lowercase()
    if (cleanFrom.isBlank() || cleanTo.isBlank() || cleanFrom == cleanTo) {
        return@withContext Result.failure(Exception("Invalid users"))
    }
    if (FirebaseAuth.getInstance().currentUser == null) {
        return@withContext Result.failure(
            Exception("Please sign in with Google to send friend requests.")
        )
    }
    val connId = buildPairConnectionId(cleanFrom, cleanTo)
    val now = System.currentTimeMillis()
    val safeFromPhoto = sanitizeRelayPhotoUrl(fromPhotoUrl)
    val safeToPhoto = sanitizeRelayPhotoUrl(toMember.photoUrl)
    var resolvedRecord = ConnectionRecord(
        id = connId,
        requesterEmail = cleanFrom,
        receiverEmail = cleanTo,
        requesterName = fromName.ifBlank { cleanFrom.substringBefore("@") },
        receiverName = toMember.name.ifBlank { cleanTo.substringBefore("@") },
        requesterPhotoUrl = safeFromPhoto.ifBlank { null },
        receiverPhotoUrl = safeToPhoto.ifBlank { null },
        status = "PENDING",
        updatedAt = now,
    )

    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(12.seconds) {
        for (db in getSocialFirestoreInstances()) {
            try {
                val docRef = db.collection("connections").document(connId)
                val existingSnap = try { docRef.get().awaitTask() } catch (_: Exception) { null }
                val existing = existingSnap?.takeIf { it.exists() }?.let { parseConnectionDoc(it) }
                if (existing != null) {
                    if (existing.status.equals("ACCEPTED", ignoreCase = true)) {
                        resolvedRecord = existing
                        ok = true
                    } else if (existing.receiverEmail == cleanFrom && existing.requesterEmail == cleanTo) {
                        // Peer already sent us a pending request — accept seamlessly!
                        docRef.set(mapOf("status" to "ACCEPTED", "updated_at" to now), SetOptions.merge()).awaitTask()
                        resolvedRecord = existing.copy(status = "ACCEPTED", updatedAt = now)
                        ok = true
                    } else {
                        resolvedRecord = existing
                        ok = true
                    }
                } else {
                    val payload = mapOf<String, Any>(
                        "id" to connId,
                        "participants" to listOf(cleanFrom, cleanTo).sorted(),
                        "requester_email" to cleanFrom,
                        "receiver_email" to cleanTo,
                        "requester_name" to resolvedRecord.requesterName.take(120),
                        "receiver_name" to resolvedRecord.receiverName.take(120),
                        "requester_photo_url" to safeFromPhoto,
                        "receiver_photo_url" to safeToPhoto,
                        "status" to "PENDING",
                        "updated_at" to now,
                    )
                    docRef.set(payload).awaitTask()
                    ok = true
                }
            } catch (e: Exception) {
                err = e
                Log.e(SOCIAL_TAG, "sendFriendRequest failed ($connId): ${e.message}")
            }
        }
    }
    if (ok) {
        upsertLocalConnectionCache(cleanFrom, resolvedRecord)
        Result.success(resolvedRecord)
    } else {
        Result.failure(Exception(formatFirestoreActionError(err, "Failed sending request")))
    }
}

actual suspend fun respondToFriendRequest(
    connectionId: String,
    accept: Boolean,
): Result<Unit> = withContext(Dispatchers.IO) {
    if (connectionId.isBlank()) return@withContext Result.failure(Exception("Invalid request"))
    if (FirebaseAuth.getInstance().currentUser == null) {
        return@withContext Result.failure(
            Exception("Please sign in with Google to update requests.")
        )
    }
    var ok = false
    var err: Exception? = null
    val now = System.currentTimeMillis()
    withTimeoutOrNull(12.seconds) {
        for (db in getSocialFirestoreInstances()) {
            try {
                if (accept) {
                    db.collection("connections").document(connectionId)
                        .set(mapOf("status" to "ACCEPTED", "updated_at" to now), SetOptions.merge())
                        .awaitTask()
                } else {
                    db.collection("connections").document(connectionId).delete().awaitTask()
                }
                ok = true
            } catch (e: Exception) {
                err = e
                Log.e(SOCIAL_TAG, "respondToFriendRequest failed ($connectionId): ${e.message}")
            }
        }
    }
    if (ok) {
        updateLocalConnectionStatusForActiveUsers(connectionId, accept)
        Result.success(Unit)
    } else {
        Result.failure(Exception(formatFirestoreActionError(err, "Failed updating request")))
    }
}

actual suspend fun removeConnection(connectionId: String): Result<Unit> =
    respondToFriendRequest(connectionId = connectionId, accept = false)

actual suspend fun loadLocalChatMessages(ownerEmail: String, peerEmail: String): List<ChatMessageItem> =
    withContext(Dispatchers.IO) {
        val cleanOwner = ownerEmail.trim().lowercase()
        val cleanPeer = peerEmail.trim().lowercase()
        if (cleanOwner.isBlank() || cleanPeer.isBlank()) return@withContext emptyList()
        synchronized(chatIoLock) {
            val file = chatMessagesFile(cleanOwner, cleanPeer) ?: return@withContext emptyList()
            readJsonMessagesLocked(file, cleanOwner)
        }
    }

actual suspend fun loadLocalChatThreads(ownerEmail: String): List<ChatThreadSummary> =
    withContext(Dispatchers.IO) {
        val cleanOwner = ownerEmail.trim().lowercase()
        if (cleanOwner.isBlank()) return@withContext emptyList()
        synchronized(chatIoLock) {
            val file = chatThreadsIndexFile(cleanOwner) ?: return@withContext emptyList()
            readThreadsIndexLocked(file).values.sortedByDescending { it.lastMessageAt }
        }
    }

actual suspend fun markChatThreadRead(ownerEmail: String, peerEmail: String) {
    withContext(Dispatchers.IO) {
        val cleanOwner = ownerEmail.trim().lowercase()
        val cleanPeer = peerEmail.trim().lowercase()
        if (cleanOwner.isBlank() || cleanPeer.isBlank()) return@withContext
        synchronized(chatIoLock) {
            val indexFile = chatThreadsIndexFile(cleanOwner) ?: return@withContext
            val threads = readThreadsIndexLocked(indexFile)
            val existing = threads[cleanPeer]
            if (existing != null && existing.unreadCount > 0) {
                threads[cleanPeer] = existing.copy(unreadCount = 0)
                writeThreadsIndexLocked(indexFile, threads)
                publishUnreadSummaryLocked(threads)
            }
        }
    }
}

actual suspend fun clearLocalChatHistory(ownerEmail: String, peerEmail: String) {
    withContext(Dispatchers.IO) {
        val cleanOwner = ownerEmail.trim().lowercase()
        val cleanPeer = peerEmail.trim().lowercase()
        if (cleanOwner.isBlank() || cleanPeer.isBlank()) return@withContext
        synchronized(chatIoLock) {
            chatMessagesFile(cleanOwner, cleanPeer)?.delete()
            val indexFile = chatThreadsIndexFile(cleanOwner)
            if (indexFile != null) {
                val threads = readThreadsIndexLocked(indexFile)
                threads.remove(cleanPeer)
                writeThreadsIndexLocked(indexFile, threads)
                publishUnreadSummaryLocked(threads)
            }
        }
    }
}

actual suspend fun sendEphemeralMessage(
    senderEmail: String,
    senderName: String,
    senderPhotoUrl: String?,
    receiverEmail: String,
    receiverName: String,
    receiverPhotoUrl: String?,
    text: String,
): Result<ChatMessageItem> = withContext(Dispatchers.IO) {
    val cleanSender = senderEmail.trim().lowercase()
    val cleanReceiver = receiverEmail.trim().lowercase()
    val cleanText = text.trim()
    if (cleanSender.isBlank() || cleanReceiver.isBlank() || cleanText.isBlank()) {
        return@withContext Result.failure(Exception("Empty message"))
    }
    val now = System.currentTimeMillis()
    val msgId = "msg_${now}_${UUID.randomUUID().toString().take(8)}"
    val item = ChatMessageItem(
        id = msgId,
        senderEmail = cleanSender,
        receiverEmail = cleanReceiver,
        text = cleanText,
        sentAt = now,
        isMine = true,
    )

    // 1. Always save permanently on sender's device
    appendMessageLocally(
        ownerEmail = cleanSender,
        peerEmail = cleanReceiver,
        peerName = receiverName,
        peerPhotoUrl = receiverPhotoUrl,
        msg = item,
        incrementUnreadIfIncoming = false,
    )

    // 2. Write transient relay doc to Firestore for real-time recipient delivery (deleted upon receipt)
    val relayPayload = mapOf<String, Any>(
        "id" to msgId,
        "sender_email" to cleanSender,
        "sender_name" to senderName.ifBlank { cleanSender.substringBefore("@") }.take(120),
        "sender_photo_url" to sanitizeRelayPhotoUrl(senderPhotoUrl),
        "receiver_email" to cleanReceiver,
        "text" to cleanText.take(2000),
        "sent_at" to now,
    )

    var deliveredToRelay = false
    var relayError: Exception? = null
    withTimeoutOrNull(10.seconds) {
        for (db in getSocialFirestoreInstances()) {
            try {
                db.collection("chat_relay").document(msgId).set(relayPayload).awaitTask()
                deliveredToRelay = true
            } catch (e: Exception) {
                relayError = e
                Log.w(SOCIAL_TAG, "Chat relay send failed: ${e.message}")
            }
        }
    }

    if (deliveredToRelay) {
        Result.success(item)
    } else {
        Result.failure(Exception(formatFirestoreActionError(relayError, "Message saved offline; cloud delivery failed")))
    }
}

actual fun startChatRelayListener(userEmail: String) {
    val cleanEmail = userEmail.trim().lowercase()
    if (cleanEmail.isBlank()) return
    if (activeRelayEmail == cleanEmail && activeRelayListeners.isNotEmpty()) return

    stopChatRelayListener()
    activeRelayEmail = cleanEmail

    // Refresh initial unread state + cached connections immediately
    relayScope.launch {
        synchronized(chatIoLock) {
            val file = chatThreadsIndexFile(cleanEmail)
            if (file != null) {
                publishUnreadSummaryLocked(readThreadsIndexLocked(file))
            }
        }
        val cachedConnections = readLocalConnectionsCache(cleanEmail)
        relayScope.launch(Dispatchers.Main) {
            SocialChatState.publishConnections(cleanEmail, cachedConnections)
        }
    }

    for (db in getSocialFirestoreInstances()) {
        try {
            val msgReg = db.collection("chat_relay")
                .whereEqualTo("receiver_email", cleanEmail)
                .addSnapshotListener { snap, error ->
                    if (error != null || snap == null) return@addSnapshotListener
                    val docs = snap.documents
                    if (docs.isEmpty()) return@addSnapshotListener

                    relayScope.launch {
                        for (doc in docs) {
                            val d = doc.data ?: continue
                            val msgId = (d["id"] as? String)?.ifBlank { doc.id } ?: doc.id
                            val sender = (d["sender_email"] as? String)?.trim()?.lowercase() ?: ""
                            val senderName = (d["sender_name"] as? String)?.trim() ?: sender.substringBefore("@")
                            val senderPhoto = (d["sender_photo_url"] as? String)?.ifBlank { null }
                            val receiver = (d["receiver_email"] as? String)?.trim()?.lowercase() ?: cleanEmail
                            val body = (d["text"] as? String) ?: ""
                            val sentAt = (d["sent_at"] as? Number)?.toLong() ?: System.currentTimeMillis()

                            if (sender.isNotBlank() && body.isNotBlank()) {
                                appendMessageLocally(
                                    ownerEmail = cleanEmail,
                                    peerEmail = sender,
                                    peerName = senderName,
                                    peerPhotoUrl = senderPhoto,
                                    msg = ChatMessageItem(
                                        id = msgId,
                                        senderEmail = sender,
                                        receiverEmail = receiver,
                                        text = body,
                                        sentAt = sentAt,
                                        isMine = false,
                                    ),
                                    incrementUnreadIfIncoming = true,
                                )
                            }

                            // Zero-retention Spark Free Plan deletion immediately upon local persistence
                            try {
                                doc.reference.delete().awaitTask()
                            } catch (e: Exception) {
                                Log.w(SOCIAL_TAG, "Relay doc cleanup warning (${doc.id}): ${e.message}")
                            }
                        }
                    }
                }
            activeRelayListeners.add(msgReg)
        } catch (e: Exception) {
            Log.w(SOCIAL_TAG, "Failed attaching chat_relay listener: ${e.message}")
        }

        try {
            val connReg = db.collection("connections")
                .whereArrayContains("participants", cleanEmail)
                .addSnapshotListener { snap, error ->
                    if (error != null || snap == null) return@addSnapshotListener
                    val parsedList = snap.documents
                        .mapNotNull { parseConnectionDoc(it) }
                        .sortedByDescending { it.updatedAt }
                    saveLocalConnectionsCache(cleanEmail, parsedList)
                    relayScope.launch(Dispatchers.Main) {
                        SocialChatState.publishConnections(cleanEmail, parsedList)
                    }
                }
            activeRelayListeners.add(connReg)
        } catch (e: Exception) {
            Log.w(SOCIAL_TAG, "Failed attaching connections listener: ${e.message}")
        }
    }
}

actual fun stopChatRelayListener() {
    activeRelayListeners.forEach {
        try {
            it.remove()
        } catch (_: Exception) {}
    }
    activeRelayListeners.clear()
    activeRelayEmail = null
    SocialChatState.clearConnections()
}
