package com.mpi.klass

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
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

// Login request status
enum class RequestStatus { PENDING, APPROVED, REJECTED }

data class LoginRequest(
    val email: String = "",
    val name: String = "",
    val role: String = "",
    val status: String = RequestStatus.PENDING.name,
    val requestedAt: Long = 0L,
    val approvedAt: Long? = null,
    val approvedBy: String? = null,
    val allowedFeatures: List<String> = emptyList(),
)

data class StudentProfile(
    val email: String = "",
    val name: String = "",
    val semester: String = "",
    val shift: String = "",
    val department: String = "",
    val section: String = "",
    val phone: String = "",
    val bloodGroup: String = "",
    val address: String = "",
    val rollNumber: String = "",
    val profileSetupComplete: Boolean = false,
    val registeredAt: Long = 0L,
)

data class SignInControlConfig(
    val adminSignInEnabled: Boolean = true,
    val teacherSignInEnabled: Boolean = true,
    val studentSignInEnabled: Boolean = true,
)

object RoleConfig {

    private const val LOGIN_REQUESTS_COLLECTION = "login_requests"
    private const val APP_CONFIG_COLLECTION = "app_config"
    private const val SIGN_IN_CONTROL_DOC = "sign_in_control"

    /**
     * Checks whether [email] is registered with [role] in Firebase
     * (Firestore collection + Realtime Database node + login_requests).
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
     * Self-registers [email] into [role]'s Firestore collection on sign-in.
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
                        "role" to role.name,
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

    // ─── Sign-in Control ─────────────────────────────────────────────────────

    /** Fetch sign-in control config from Firestore. */
    suspend fun fetchSignInControl(): SignInControlConfig {
        return try {
            val doc = withTimeoutOrNull(10.seconds) {
                Firebase.firestore
                    .collection(APP_CONFIG_COLLECTION)
                    .document(SIGN_IN_CONTROL_DOC)
                    .get()
            } ?: return SignInControlConfig()

            if (!doc.exists) return SignInControlConfig()

            val data = doc.data<Map<String, Any>>()
            SignInControlConfig(
                adminSignInEnabled = (data["admin_sign_in_enabled"] as? Boolean) ?: true,
                teacherSignInEnabled = (data["teacher_sign_in_enabled"] as? Boolean) ?: true,
                studentSignInEnabled = (data["student_sign_in_enabled"] as? Boolean) ?: true,
            )
        } catch (_: Exception) {
            SignInControlConfig() // Default: all enabled on error
        }
    }

    /** Save sign-in control config to Firestore (admin only). */
    suspend fun saveSignInControl(config: SignInControlConfig): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(Exception("NO_INTERNET"))
        return try {
            Firebase.firestore
                .collection(APP_CONFIG_COLLECTION)
                .document(SIGN_IN_CONTROL_DOC)
                .set(
                    mapOf(
                        "admin_sign_in_enabled" to config.adminSignInEnabled,
                        "teacher_sign_in_enabled" to config.teacherSignInEnabled,
                        "student_sign_in_enabled" to config.studentSignInEnabled,
                        "updated_at" to Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Login Request / Approval System ────────────────────────────────────

    /**
     * Check if a login request exists and return its status.
     * Returns null if no request found.
     */
    suspend fun getLoginRequestStatus(email: String): LoginRequest? {
        if (email.isBlank()) return null
        val cleanEmail = email.trim().lowercase()
        return try {
            val doc = withTimeoutOrNull(10.seconds) {
                Firebase.firestore
                    .collection(LOGIN_REQUESTS_COLLECTION)
                    .document(cleanEmail)
                    .get()
            } ?: return null

            if (!doc.exists) return null
            val data = doc.data<Map<String, Any>>()
            val allowed = (data["allowed_features"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            LoginRequest(
                email = (data["email"] as? String) ?: cleanEmail,
                name = (data["name"] as? String) ?: "",
                role = (data["role"] as? String) ?: "",
                status = (data["status"] as? String) ?: RequestStatus.PENDING.name,
                requestedAt = (data["requested_at"] as? Long) ?: 0L,
                approvedAt = data["approved_at"] as? Long,
                approvedBy = data["approved_by"] as? String,
                allowedFeatures = allowed,
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks if the admins collection in Firestore is empty.
     */
    suspend fun isAdminsCollectionEmpty(): Boolean {
        return try {
            val snapshot = withTimeoutOrNull(10.seconds) {
                Firebase.firestore.collection(UserRole.ADMIN.collection).get()
            }
            snapshot?.documents?.isEmpty() ?: true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Creates or updates a login request for [email] with [role].
     * If already APPROVED, does not overwrite.
     */
    suspend fun createLoginRequest(email: String, role: UserRole, name: String?): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        if (!isNetworkAvailable()) return Result.failure(Exception("NO_INTERNET"))

        val cleanEmail = email.trim().lowercase()
        return try {
            // Check if already approved — don't overwrite
            val existing = getLoginRequestStatus(cleanEmail)
            if (existing?.status == RequestStatus.APPROVED.name) return Result.success(Unit)

            Firebase.firestore
                .collection(LOGIN_REQUESTS_COLLECTION)
                .document(cleanEmail)
                .set(
                    mapOf(
                        "email" to cleanEmail,
                        "name" to (name ?: ""),
                        "role" to role.name,
                        "status" to RequestStatus.PENDING.name,
                        "requested_at" to Clock.System.now().toEpochMilliseconds(),
                        "allowed_features" to emptyList<String>(),
                    ),
                )
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all pending login requests (for admin use).
     */
    suspend fun fetchPendingRequests(): List<LoginRequest> {
        return try {
            val snapshot = withTimeoutOrNull(15.seconds) {
                Firebase.firestore
                    .collection(LOGIN_REQUESTS_COLLECTION)
                    .get()
            } ?: return emptyList()

            snapshot.documents.mapNotNull { doc ->
                if (!doc.exists) return@mapNotNull null
                try {
                    val data = doc.data<Map<String, Any>>()
                    val allowed = (data["allowed_features"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    LoginRequest(
                        email = (data["email"] as? String) ?: "",
                        name = (data["name"] as? String) ?: "",
                        role = (data["role"] as? String) ?: "",
                        status = (data["status"] as? String) ?: RequestStatus.PENDING.name,
                        requestedAt = (data["requested_at"] as? Long) ?: 0L,
                        approvedAt = data["approved_at"] as? Long,
                        approvedBy = data["approved_by"] as? String,
                        allowedFeatures = allowed,
                    )
                } catch (_: Exception) {
                    null
                }
            }.filter { it.email.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Admin approves a login request.
     * Also registers the user in their role's Firestore collection.
     */
    suspend fun approveLoginRequest(
        requestEmail: String,
        adminEmail: String,
        allowedFeatures: List<String>,
    ): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(Exception("NO_INTERNET"))
        val cleanEmail = requestEmail.trim().lowercase()
        return try {
            // 1. Get the request to find the role
            val request = getLoginRequestStatus(cleanEmail)
                ?: return Result.failure(Exception("Request not found"))

            val role = UserRole.fromRaw(request.role)
                ?: return Result.failure(Exception("Invalid role in request"))

            // 2. Update request status
            Firebase.firestore
                .collection(LOGIN_REQUESTS_COLLECTION)
                .document(cleanEmail)
                .set(
                    mapOf(
                        "email" to cleanEmail,
                        "name" to request.name,
                        "role" to request.role,
                        "status" to RequestStatus.APPROVED.name,
                        "requested_at" to request.requestedAt,
                        "approved_at" to Clock.System.now().toEpochMilliseconds(),
                        "approved_by" to adminEmail,
                        "allowed_features" to allowedFeatures,
                    ),
                )

            // 3. Register in role collection
            registerRoleMember(cleanEmail, role, request.name)

            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin rejects a login request.
     */
    suspend fun rejectLoginRequest(requestEmail: String, adminEmail: String): Result<Unit> {
        if (!isNetworkAvailable()) return Result.failure(Exception("NO_INTERNET"))
        val cleanEmail = requestEmail.trim().lowercase()
        return try {
            val request = getLoginRequestStatus(cleanEmail) ?: return Result.failure(Exception("Request not found"))
            Firebase.firestore
                .collection(LOGIN_REQUESTS_COLLECTION)
                .document(cleanEmail)
                .set(
                    mapOf(
                        "email" to cleanEmail,
                        "name" to request.name,
                        "role" to request.role,
                        "status" to RequestStatus.REJECTED.name,
                        "requested_at" to request.requestedAt,
                        "approved_at" to Clock.System.now().toEpochMilliseconds(),
                        "approved_by" to adminEmail,
                        "allowed_features" to emptyList<String>(),
                    ),
                )
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Student Profile ─────────────────────────────────────────────────────

    /** Fetch student profile from Firestore. Returns null if not found or setup incomplete. */
    suspend fun fetchStudentProfile(email: String): StudentProfile? {
        if (email.isBlank()) return null
        val cleanEmail = email.trim().lowercase()
        return try {
            val doc = withTimeoutOrNull(15.seconds) {
                Firebase.firestore
                    .collection("students")
                    .document(cleanEmail)
                    .get()
            } ?: return null

            if (!doc.exists) return null
            val data = doc.data<Map<String, Any>>()
            val profileComplete = (data["profile_setup_complete"] as? Boolean) ?: false
            if (!profileComplete) return null

            StudentProfile(
                email = (data["email"] as? String) ?: cleanEmail,
                name = (data["name"] as? String) ?: "",
                semester = (data["semester"] as? String) ?: "",
                shift = (data["shift"] as? String) ?: "",
                department = (data["department"] as? String) ?: "",
                section = (data["section"] as? String) ?: "",
                phone = (data["phone"] as? String) ?: "",
                bloodGroup = (data["blood_group"] as? String) ?: "",
                address = (data["address"] as? String) ?: "",
                rollNumber = (data["roll_number"] as? String) ?: "",
                profileSetupComplete = true,
                registeredAt = (data["registered_at"] as? Long) ?: 0L,
            )
        } catch (_: Exception) {
            null
        }
    }

    /** Save student profile to Firestore. */
    suspend fun saveStudentProfile(profile: StudentProfile): Result<Unit> {
        if (profile.email.isBlank()) return Result.failure(Exception("Invalid email"))
        if (!isNetworkAvailable()) return Result.failure(Exception("NO_INTERNET"))

        val cleanEmail = profile.email.trim().lowercase()
        return try {
            Firebase.firestore
                .collection("students")
                .document(cleanEmail)
                .set(
                    mapOf(
                        "email" to cleanEmail,
                        "name" to profile.name,
                        "semester" to profile.semester,
                        "shift" to profile.shift,
                        "department" to profile.department,
                        "section" to profile.section,
                        "phone" to profile.phone,
                        "blood_group" to profile.bloodGroup,
                        "address" to profile.address,
                        "roll_number" to profile.rollNumber,
                        "profile_setup_complete" to true,
                        "registered_at" to (if (profile.registeredAt == 0L) Clock.System.now().toEpochMilliseconds() else profile.registeredAt),
                    ),
                )
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Account Deletion (Full Firebase Sync) ───────────────────────────────

    /**
     * Fully deletes a user account from Firestore, Realtime Database, login_requests,
     * and Firebase Auth. Also clears local session.
     */
    suspend fun deleteAccountFromFirebase(email: String, role: UserRole): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()
        val errors = mutableListOf<String>()

        // 1. Delete from Firestore role collection
        try {
            Firebase.firestore
                .collection(role.collection)
                .document(cleanEmail)
                .delete()
        } catch (e: Exception) {
            errors.add("Firestore ${role.collection}: ${e.message}")
        }

        // 2. Delete from students collection if student
        if (role == UserRole.STUDENT) {
            try {
                Firebase.firestore
                    .collection("students")
                    .document(cleanEmail)
                    .delete()
            } catch (_: Exception) {}
        }

        // 3. Delete from login_requests
        try {
            Firebase.firestore
                .collection(LOGIN_REQUESTS_COLLECTION)
                .document(cleanEmail)
                .delete()
        } catch (_: Exception) {}

        // 4. Delete from Realtime Database
        val rtdbKeys = listOf(
            cleanEmail,
            cleanEmail.substringBefore("@"),
            cleanEmail.replace(".", ","),
            cleanEmail.replace(".", "_"),
        )
        try {
            val ref = Firebase.database.reference(role.rtdbNode)
            rtdbKeys.forEach { key ->
                try { ref.child(key).removeValue() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            errors.add("RTDB: ${e.message}")
        }

        // 5. Delete Firebase Auth user
        try {
            Firebase.auth.currentUser?.delete()
        } catch (e: Exception) {
            errors.add("FirebaseAuth: ${e.message}")
        }

        // 6. Clear local session
        clearUserSession()

        return if (errors.isEmpty()) Result.success(Unit)
        else Result.failure(Exception("Partial delete errors: ${errors.joinToString()}"))
    }

    /**
     * Deletes student sign-in data (without full account deletion).
     * Used when student doesn't complete profile setup.
     */
    suspend fun deleteStudentSignInData(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        // 1. Delete from students Firestore collection
        try {
            Firebase.firestore
                .collection("students")
                .document(cleanEmail)
                .delete()
        } catch (_: Exception) {}

        // 2. Delete from login_requests
        try {
            Firebase.firestore
                .collection(LOGIN_REQUESTS_COLLECTION)
                .document(cleanEmail)
                .delete()
        } catch (_: Exception) {}

        // 3. Delete from RTDB
        val rtdbKeys = listOf(
            cleanEmail,
            cleanEmail.substringBefore("@"),
            cleanEmail.replace(".", ","),
            cleanEmail.replace(".", "_"),
        )
        try {
            val ref = Firebase.database.reference("students")
            rtdbKeys.forEach { key ->
                try { ref.child(key).removeValue() } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        // 4. Sign out Firebase Auth
        try {
            Firebase.auth.currentUser?.delete()
        } catch (_: Exception) {}

        // 5. Clear local session
        clearUserSession()

        return Result.success(Unit)
    }

    // ─── Private Helpers ────────────────────────────────────────────────────

    private suspend fun verifyInFirestore(role: UserRole, cleanEmail: String, emailUsername: String): Boolean {
        val collection = Firebase.firestore.collection(role.collection)

        // 1. Direct document check by email
        val docByEmail = try {
            collection.document(cleanEmail).get().exists
        } catch (_: Exception) { false }
        if (docByEmail) return true

        // 2. Direct document check by username
        val docByUsername = try {
            collection.document(emailUsername).get().exists
        } catch (_: Exception) { false }
        if (docByUsername) return true

        // 3. Query by email field
        val queryMatches = try {
            collection.where { "email" equalTo cleanEmail }.get().documents.isNotEmpty()
        } catch (_: Exception) { false }
        if (queryMatches) return true

        // 4. For Admin, check approved login requests
        if (role == UserRole.ADMIN) {
            val request = getLoginRequestStatus(cleanEmail)
            if (request?.status == RequestStatus.APPROVED.name) return true
        }

        return false
    }

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
