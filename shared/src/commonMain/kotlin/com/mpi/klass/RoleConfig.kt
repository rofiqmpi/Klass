package com.mpi.klass

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull

enum class UserRole(
    val collection: String,
    val rtdbNode: String,
    val dashboardTitle: String,
    val defaultName: String,
) {
    ADMIN("admins", "admins", "এডমিন ড্যাশবোর্ড", "এডমিনিস্ট্রেটর"),
    TEACHER("teachers", "teachers", "শিক্ষক ড্যাশবোর্ড", "শিক্ষক"),
    STUDENT("students", "students", "শিক্ষার্থী ড্যাশবোর্ড", "শিক্ষার্থী");

    companion object {
        fun fromRaw(role: String?): UserRole? {
            val cleaned = role?.trim()?.uppercase() ?: return null
            return when (cleaned) {
                "ADMIN" -> ADMIN
                "TEACHER" -> TEACHER
                "STUDENT" -> STUDENT
                else -> null
            }
        }
    }
}

object RoleConfig {

    /**
     * Checks whether [email] is registered with [role] in Firebase
     * (Firestore collection + Realtime Database node).
     *
     * Matching is EXACT document-ID / key equality only — never substring —
     * so one registered email can never authorize a similar-looking one.
     *
     * Returns:
     *  - Result.success(true)  -> registered
     *  - Result.success(false) -> definitively not registered
     *  - Result.failure(Exception("NO_INTERNET") / "NETWORK_TIMEOUT") on connection failure
     */
    suspend fun verifyRoleInFirebase(email: String, role: UserRole): Result<Boolean> {
        if (email.isBlank()) return Result.success(value = false)
        if (!isNetworkAvailable()) return Result.failure(Exception("NO_INTERNET"))

        val cleanEmail = email.trim().lowercase()
        val emailUsername = cleanEmail.substringBefore("@")
        val rtdbKeys = listOf(
            cleanEmail,
            emailUsername,
            cleanEmail.replace(".", ","),
            cleanEmail.replace(".", "_"),
        )

        return try {
            val isVerified = withTimeoutOrNull(20.seconds) {
                verifyInFirestore(role, cleanEmail, emailUsername) ||
                    verifyInRealtimeDatabase(role, rtdbKeys)
            }

            if (isVerified != null) {
                Result.success(isVerified)
            } else {
                Result.failure(Exception(networkErrorMessage()))
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            error.printStackTrace()
            Result.failure(error)
        }
    }

    /**
     * Self-registers [email] into [role]'s Firestore collection on first
     * sign-in (document ID = the email itself). Used for teachers/students —
     * there are too many to pre-register. Admins are never auto-created;
     * they must already exist in the console.
     */
    suspend fun registerRoleMember(email: String, role: UserRole, name: String?): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("NETWORK_TIMEOUT"))
        if (!isNetworkAvailable()) return Result.failure(Exception("NO_INTERNET"))

        val cleanEmail = email.trim().lowercase()
        return try {
            Firebase.firestore.collection(role.collection)
                .document(cleanEmail)
                .set(
                    mapOf(
                        "email" to cleanEmail,
                        "name" to (name ?: ""),
                        "registered_at" to Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            error.printStackTrace()
            Result.failure(error)
        }
    }

    /**
     * Firestore: only direct, exact document lookups are performed.
     * The whole collection is never downloaded, and document data is never
     * string-matched, so the role registry cannot leak to clients.
     */
    private suspend fun verifyInFirestore(role: UserRole, cleanEmail: String, emailUsername: String): Boolean {
        val collection = Firebase.firestore.collection(role.collection)
        return collection.document(cleanEmail).get().exists ||
            collection.document(emailUsername).get().exists
    }

    /**
     * Realtime Database: exact key lookups under the role node.
     * Errors here are best-effort swallowed (Firestore is the source of truth).
     */
    private suspend fun verifyInRealtimeDatabase(role: UserRole, keys: List<String>): Boolean {
        return try {
            val ref = Firebase.database.reference(role.rtdbNode)
            keys.any { key ->
                try {
                    val snap = ref.child(key).get()
                    snap.exists && (snap.value != null)
                } catch (_: Exception) {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun networkErrorMessage(): String =
        if (!isNetworkAvailable()) "NO_INTERNET" else "NETWORK_TIMEOUT"
}
