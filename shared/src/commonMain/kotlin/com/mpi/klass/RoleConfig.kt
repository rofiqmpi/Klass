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
    private const val ADMINS_COLLECTION = "admins"
    private const val TEACHERS_COLLECTION = "teachers"

    fun configureLocalEmulator(host: String = "10.0.2.2") {
        // No-op: Always connect directly to live Firebase services
    }

    suspend fun saveTeacherProfile(
        email: String,
        name: String,
        photoUrl: String?,
        provider: String = "google",
    ): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        // 1. Save locally on device
        saveLocalProfile(cleanEmail, name, photoUrl, provider, UserRole.TEACHER.name)

        // 2. Save directly to Firebase 'teachers' collection
        return try {
            withTimeoutOrNull(6.seconds) {
                val docRef = Firebase.firestore.collection(TEACHERS_COLLECTION).document(cleanEmail)
                val data = mapOf(
                    "email" to cleanEmail,
                    "name" to name,
                    "photo_url" to (photoUrl ?: ""),
                    "role" to "TEACHER",
                    "provider" to provider,
                    "registered_at" to now,
                    "updated_at" to now,
                )
                docRef.set(data, merge = true)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(
        email: String,
        name: String,
        photoUrl: String?,
        provider: String,
        category: String = "STUDENT",
    ): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        // 1. Save locally on device
        saveLocalProfile(cleanEmail, name, photoUrl, provider, category)

        // 2. If Teacher, save to 'teachers' collection
        if (category.equals(UserRole.TEACHER.name, ignoreCase = true)) {
            saveTeacherProfile(cleanEmail, name, photoUrl, provider)
        }

        // 3. Save to live Firebase Firestore 'users' collection
        try {
            withTimeoutOrNull(5.seconds) {
                val docRef = Firebase.firestore.collection(USERS_COLLECTION).document(cleanEmail)
                val data = mapOf(
                    "email" to cleanEmail,
                    "name" to name,
                    "photo_url" to (photoUrl ?: ""),
                    "provider" to provider,
                    "category" to category,
                    "updated_at" to now,
                )
                docRef.set(data, merge = true)
            }
        } catch (_: Exception) {}

        return Result.success(Unit)
    }

    suspend fun isAuthorizedAdminEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return try {
            // 1. Check direct document by email ID in 'admins' collection (e.g. admins/user@email.com)
            val doc = withTimeoutOrNull(6.seconds) {
                Firebase.firestore.collection(ADMINS_COLLECTION).document(cleanEmail).get()
            }
            if (doc?.exists == true) {
                val data = try { doc.data<Map<String, Any>>() } catch (_: Exception) { null }
                val isEnabled = data?.get("enabled") as? Boolean ?: true
                return isEnabled
            }

            // 2. Check query where email field == cleanEmail (in case document was saved with auto ID)
            val querySnapshot = withTimeoutOrNull(6.seconds) {
                try {
                    Firebase.firestore.collection(ADMINS_COLLECTION)
                        .where { "email" equalTo cleanEmail }
                        .get()
                } catch (_: Exception) {
                    null
                }
            }
            if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                val firstDoc = querySnapshot.documents.first()
                val data = try { firstDoc.data<Map<String, Any>>() } catch (_: Exception) { null }
                val isEnabled = data?.get("enabled") as? Boolean ?: true
                return isEnabled
            }

            false
        } catch (e: Exception) {
            println("Admin authorization check exception: ${e.message}")
            false
        }
    }

    suspend fun saveAdminEmail(email: String, name: String, photoUrl: String?): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid admin email"))
        val cleanEmail = email.trim().lowercase()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        return try {
            withTimeoutOrNull(6.seconds) {
                Firebase.firestore.collection(ADMINS_COLLECTION).document(cleanEmail).set(
                    mapOf(
                        "email" to cleanEmail,
                        "name" to name,
                        "photo_url" to (photoUrl ?: ""),
                        "role" to "ADMIN",
                        "last_login_at" to now,
                        "updated_at" to now,
                    ),
                    merge = true,
                )
            }
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

        // Try 'teachers' collection first if registered as teacher
        try {
            val teacherDoc = withTimeoutOrNull(4.seconds) {
                Firebase.firestore.collection(TEACHERS_COLLECTION).document(cleanEmail).get()
            }
            if (teacherDoc?.exists == true) {
                val data = teacherDoc.data<Map<String, Any>>()
                val prof = UserProfile(
                    email = (data["email"] as? String) ?: cleanEmail,
                    name = (data["name"] as? String) ?: "",
                    photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
                    provider = (data["provider"] as? String) ?: "google",
                    category = UserRole.TEACHER.name,
                    registeredAt = (data["registered_at"] as? Long) ?: 0L,
                    updatedAt = (data["updated_at"] as? Long) ?: 0L,
                )
                saveLocalProfile(cleanEmail, prof.name, prof.photoUrl, prof.provider, prof.category)
                return prof
            }
        } catch (_: Exception) {}

        // Fallback to 'users' collection
        try {
            val doc = withTimeoutOrNull(4.seconds) {
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
                    updatedAt = (data["updated_at"] as? Long) ?: 0L,
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
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        // Update local storage
        updateLocalCategory(cleanEmail, category)

        // If updated to TEACHER, also record into 'teachers' collection
        if (category.equals(UserRole.TEACHER.name, ignoreCase = true)) {
            val local = fetchLocalProfile(cleanEmail)
            saveTeacherProfile(cleanEmail, local?.name ?: "", local?.photoUrl, local?.provider ?: "google")
        }

        try {
            val docRef = Firebase.firestore.collection(USERS_COLLECTION).document(cleanEmail)
            docRef.set(mapOf("category" to category, "updated_at" to now), merge = true)
        } catch (_: Exception) {}

        return Result.success(Unit)
    }

    suspend fun deleteUserAccount(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        deleteLocalProfile(cleanEmail)
        try {
            Firebase.firestore.collection(USERS_COLLECTION).document(cleanEmail).delete()
            Firebase.firestore.collection(TEACHERS_COLLECTION).document(cleanEmail).delete()
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
