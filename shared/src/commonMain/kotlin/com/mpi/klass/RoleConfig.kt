package com.mpi.klass

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull

enum class UserRole(
    val dashboardTitle: String,
    val defaultName: String,
) {
    ADMIN("এডমিন ড্যাশবোর্ড", "এডমিনিস্ট্রেটর"),
    TEACHER("শিক্ষক ড্যাশবোর্ড", "শিক্ষক"),
    STUDENT("শিক্ষার্থী ড্যাশবোর্ড", "শিক্ষার্থী");

    companion object {
        fun fromRaw(role: String?): UserRole? {
            val cleaned = role?.trim()?.uppercase() ?: return null
            return when (cleaned) {
                "ADMIN", "এডমিন" -> ADMIN
                "TEACHER", "শিক্ষক" -> TEACHER
                "STUDENT", "শিক্ষার্থী" -> STUDENT
                else -> null
            }
        }
    }
}

data class UserProfile(
    val email: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val provider: String = "", // "google" or "email"
    val category: String = "STUDENT", // selected in Profile screen
    val registeredAt: Long = 0L,
    val updatedAt: Long = 0L,
)

// Backward compatibility stubs for existing screens
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
    val profileSetupComplete: Boolean = true,
    val registeredAt: Long = 0L,
)

data class SignInControlConfig(
    val adminSignInEnabled: Boolean = true,
    val teacherSignInEnabled: Boolean = true,
    val studentSignInEnabled: Boolean = true,
)

expect fun saveLocalProfile(email: String, name: String, photoUrl: String?, provider: String, category: String)
expect fun fetchLocalProfile(email: String): UserProfile?
expect fun deleteLocalProfile(email: String)
expect fun updateLocalCategory(email: String, category: String)

object RoleConfig {
    private const val USERS_COLLECTION = "users"
    private const val ADMIN_EMAILS_COLLECTION = "admin_emails"
    private const val ADMINS_COLLECTION = "admins"
    private var isEmulatorInitialized = false

    /**
     * Configures Firebase to connect to Local Emulator on PC (10.0.2.2).
     */
    fun configureLocalEmulator(host: String = "10.0.2.2") {
        if (isEmulatorInitialized) return
        try {
            Firebase.firestore.setSettings(host = "$host:8080", sslEnabled = false)
            Firebase.auth.useEmulator(host, 9099)
            Firebase.database.useEmulator(host, 9000)
            isEmulatorInitialized = true
        } catch (_: Exception) {}
    }

    suspend fun saveUserProfile(
        email: String,
        name: String,
        photoUrl: String?,
        provider: String,
        category: String = "STUDENT"
    ): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        // 1. Save locally on device
        saveLocalProfile(cleanEmail, name, photoUrl, provider, category)

        // 2. Save to Local Firebase Emulator with timeout
        try {
            withTimeoutOrNull(2.seconds) {
                val docRef = Firebase.firestore.collection(USERS_COLLECTION).document(cleanEmail)
                val data = mapOf(
                    "email" to cleanEmail,
                    "name" to name,
                    "photo_url" to (photoUrl ?: ""),
                    "provider" to provider,
                    "category" to category,
                    "updated_at" to kotlin.time.Clock.System.now().toEpochMilliseconds()
                )
                docRef.set(data)
            }
        } catch (_: Exception) {}

        return Result.success(Unit)
    }

    suspend fun isAuthorizedAdminEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return try {
            val doc = withTimeoutOrNull(3.seconds) {
                Firebase.firestore.collection(ADMIN_EMAILS_COLLECTION).document(cleanEmail).get()
            }
            doc?.exists == true && (doc.data<Map<String, Any>>()["enabled"] as? Boolean ?: true)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun saveAdminEmail(email: String, name: String, photoUrl: String?): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid admin email"))
        val cleanEmail = email.trim().lowercase()
        return try {
            Firebase.firestore.collection(ADMINS_COLLECTION).document(cleanEmail).set(
                mapOf(
                    "email" to cleanEmail,
                    "name" to name,
                    "photo_url" to (photoUrl ?: ""),
                    "updated_at" to kotlin.time.Clock.System.now().toEpochMilliseconds(),
                ),
                merge = true,
            )
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun fetchUserProfile(email: String): UserProfile? {
        if (email.isBlank()) return null
        val cleanEmail = email.trim().lowercase()

        val localProf = fetchLocalProfile(cleanEmail)
        if (localProf != null) return localProf

        try {
            val doc = withTimeoutOrNull(2.seconds) {
                Firebase.firestore.collection(USERS_COLLECTION).document(cleanEmail).get()
            }
            if (doc?.exists == true) {
                val data = doc.data<Map<String, Any>>()
                val prof = UserProfile(
                    email = (data["email"] as? String) ?: cleanEmail,
                    name = (data["name"] as? String) ?: "",
                    photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
                    provider = (data["provider"] as? String) ?: "",
                    category = (data["category"] as? String) ?: "STUDENT",
                    registeredAt = (data["registered_at"] as? Long) ?: 0L,
                    updatedAt = (data["updated_at"] as? Long) ?: 0L
                )
                saveLocalProfile(cleanEmail, prof.name, prof.photoUrl, prof.provider, prof.category)
                return prof
            }
        } catch (_: Exception) {}

        return null
    }

    suspend fun updateUserCategory(email: String, category: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        // Update local storage & local emulator
        updateLocalCategory(cleanEmail, category)
        try {
            val docRef = Firebase.firestore.collection(USERS_COLLECTION).document(cleanEmail)
            docRef.set(mapOf("category" to category, "updated_at" to kotlin.time.Clock.System.now().toEpochMilliseconds()), merge = true)
        } catch (_: Exception) {}

        return Result.success(Unit)
    }

    suspend fun deleteUserAccount(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        deleteLocalProfile(cleanEmail)
        try {
            Firebase.firestore.collection(USERS_COLLECTION).document(cleanEmail).delete()
            Firebase.auth.currentUser?.delete()
        } catch (_: Exception) {}

        clearUserSession()
        return Result.success(Unit)
    }

    // Stubs for backward compatibility
    suspend fun fetchStudentProfile(email: String): StudentProfile? = StudentProfile(email = email)
    suspend fun saveStudentProfile(profile: StudentProfile): Result<Unit> = Result.success(Unit)
    suspend fun deleteStudentSignInData(email: String): Result<Unit> = Result.success(Unit)
    suspend fun fetchSignInControl(): SignInControlConfig = SignInControlConfig()
    suspend fun saveSignInControl(config: SignInControlConfig): Result<Unit> = Result.success(Unit)
    suspend fun deleteAccountFromFirebase(email: String, role: UserRole): Result<Unit> = deleteUserAccount(email)
}
