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
    val photoUrl: String? = null,
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

enum class AdminStatus {
    APPROVED,
    PENDING,
    DECLINED,
    NOT_REQUESTED,
}

data class AdminRequestItem(
    val email: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val status: String = "PENDING",
    val requestedAt: Long = 0L,
)

expect fun saveLocalProfile(email: String, name: String, photoUrl: String?, provider: String, category: String)
expect fun fetchLocalProfile(email: String): UserProfile?
expect fun deleteLocalProfile(email: String)
expect fun updateLocalCategory(email: String, category: String)
expect suspend fun checkIsAuthorizedAdminEmail(email: String): Boolean
expect suspend fun saveAdminInFirestore(email: String, name: String, photoUrl: String?): Result<Unit>
expect suspend fun saveTeacherInFirestore(email: String, name: String, photoUrl: String?, provider: String): Result<Unit>
expect suspend fun saveStudentInFirestore(email: String, name: String, photoUrl: String?, provider: String): Result<Unit>
expect suspend fun saveStudentProfileDataInFirestore(profile: StudentProfile): Result<Unit>
expect suspend fun fetchStudentProfileDataFromFirestore(email: String): StudentProfile?
expect suspend fun saveUserInFirestore(email: String, name: String, photoUrl: String?, provider: String, category: String): Result<Unit>
expect suspend fun fetchUserFromFirestore(email: String): UserProfile?
expect suspend fun deleteUserFromFirestore(email: String, role: String): Result<Unit>
expect suspend fun fetchSignInControlFromFirestore(): SignInControlConfig
expect suspend fun saveSignInControlInFirestore(config: SignInControlConfig): Result<Unit>
expect suspend fun getAdminStatusFromFirestore(email: String): AdminStatus
expect suspend fun submitAdminRequestToFirestore(email: String, name: String, photoUrl: String?): Result<Unit>
expect suspend fun fetchPendingAdminRequestsFromFirestore(): List<AdminRequestItem>
expect suspend fun updateAdminRequestStatusInFirestore(email: String, newStatus: String): Result<Unit>

object RoleConfig {
    private const val USERS_COLLECTION = "users"
    private const val ADMINS_COLLECTION = "admins"
    private const val TEACHERS_COLLECTION = "teachers"
    private const val STUDENTS_COLLECTION = "students"

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

        // 1. Save locally on device
        saveLocalProfile(cleanEmail, name, photoUrl, provider, UserRole.TEACHER.name)

        // 2. Save directly to Firebase 'teachers' collection across databases
        return saveTeacherInFirestore(cleanEmail, name, photoUrl, provider)
    }

    suspend fun saveStudentProfile(
        email: String,
        name: String,
        photoUrl: String?,
        provider: String = "google",
    ): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        // 1. Save locally on device
        saveLocalProfile(cleanEmail, name, photoUrl, provider, UserRole.STUDENT.name)

        // 2. Save directly to Firebase 'students' collection across databases
        return saveStudentInFirestore(cleanEmail, name, photoUrl, provider)
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

        // 1. Save locally on device
        saveLocalProfile(cleanEmail, name, photoUrl, provider, category)

        // 2. Specialized collection saving
        when (category.uppercase()) {
            UserRole.TEACHER.name -> saveTeacherProfile(cleanEmail, name, photoUrl, provider)
            UserRole.STUDENT.name -> saveStudentProfile(cleanEmail, name, photoUrl, provider)
        }

        // 3. Save to live Firebase Firestore 'users' collection across active databases
        saveUserInFirestore(cleanEmail, name, photoUrl, provider, category)

        return Result.success(Unit)
    }

    suspend fun isAuthorizedAdminEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val cleanEmail = email.trim().lowercase()
        val status = getAdminStatusFromFirestore(cleanEmail)
        return status == AdminStatus.APPROVED
    }

    suspend fun saveAdminEmail(email: String, name: String, photoUrl: String?): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid admin email"))
        val cleanEmail = email.trim().lowercase()
        saveLocalProfile(cleanEmail, name, photoUrl, "google", UserRole.ADMIN.name)
        return saveAdminInFirestore(cleanEmail, name, photoUrl)
    }

    suspend fun fetchUserProfile(email: String): UserProfile? {
        if (email.isBlank()) return null
        val cleanEmail = email.trim().lowercase()

        val localProf = fetchLocalProfile(cleanEmail)
        if (localProf != null && !localProf.photoUrl.isNullOrBlank()) return localProf

        val remoteProf = fetchUserFromFirestore(cleanEmail)
        if (remoteProf != null) {
            val mergedName = localProf?.name?.ifBlank { null } ?: remoteProf.name
            val mergedPhoto = localProf?.photoUrl?.ifBlank { null } ?: remoteProf.photoUrl
            val mergedCategory = localProf?.category?.ifBlank { null } ?: remoteProf.category
            val merged = remoteProf.copy(name = mergedName, photoUrl = mergedPhoto, category = mergedCategory)
            saveLocalProfile(cleanEmail, merged.name, merged.photoUrl, merged.provider, merged.category)
            return merged
        }

        return localProf
    }

    suspend fun updateUserCategory(email: String, category: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        // Update local storage
        updateLocalCategory(cleanEmail, category)

        val local = fetchLocalProfile(cleanEmail)
        val name = local?.name ?: cleanEmail.substringBefore("@")
        val photoUrl = local?.photoUrl
        val provider = local?.provider ?: "google"

        // If updated to TEACHER, also record into 'teachers' collection
        if (category.equals(UserRole.TEACHER.name, ignoreCase = true)) {
            saveTeacherProfile(cleanEmail, name, photoUrl, provider)
        }

        saveUserInFirestore(cleanEmail, name, photoUrl, provider, category)

        return Result.success(Unit)
    }

    suspend fun deleteUserAccount(email: String, role: UserRole): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()

        deleteLocalProfile(cleanEmail)
        deleteUserFromFirestore(cleanEmail, role.name)
        try {
            Firebase.auth.currentUser?.delete()
        } catch (_: Exception) {}

        clearUserSession()
        return Result.success(Unit)
    }

    suspend fun isUserRegisteredForRole(email: String, role: UserRole): Boolean {
        if (email.isBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return when (role) {
            UserRole.ADMIN -> isAuthorizedAdminEmail(cleanEmail)
            UserRole.TEACHER -> {
                val profile = fetchUserProfile(cleanEmail)
                profile != null && profile.category.trim().uppercase() == UserRole.TEACHER.name
            }
            UserRole.STUDENT -> {
                val profile = fetchUserProfile(cleanEmail)
                profile != null
            }
        }
    }

    // Student profile persistence
    suspend fun fetchStudentProfile(email: String): StudentProfile? {
        if (email.isBlank()) return null
        return fetchStudentProfileDataFromFirestore(email.trim().lowercase())
    }

    suspend fun saveStudentProfile(profile: StudentProfile): Result<Unit> {
        if (profile.email.isBlank()) return Result.failure(Exception("Invalid email"))
        saveStudentProfile(profile.email, profile.name, profile.photoUrl)
        return saveStudentProfileDataInFirestore(profile)
    }
    suspend fun deleteStudentSignInData(email: String): Result<Unit> = deleteUserAccount(email, UserRole.STUDENT)
    suspend fun fetchSignInControl(): SignInControlConfig = fetchSignInControlFromFirestore()
    suspend fun saveSignInControl(config: SignInControlConfig): Result<Unit> = saveSignInControlInFirestore(config)
    suspend fun deleteAccountFromFirebase(email: String, role: UserRole): Result<Unit> = deleteUserAccount(email, role)
    suspend fun getAdminStatusFromFirestore(email: String): AdminStatus = com.mpi.klass.getAdminStatusFromFirestore(email)
    suspend fun submitAdminRequestToFirestore(email: String, name: String, photoUrl: String?): Result<Unit> = com.mpi.klass.submitAdminRequestToFirestore(email, name, photoUrl)
    suspend fun fetchPendingAdminRequestsFromFirestore(): List<AdminRequestItem> = com.mpi.klass.fetchPendingAdminRequestsFromFirestore()
    suspend fun updateAdminRequestStatusInFirestore(email: String, newStatus: String): Result<Unit> = com.mpi.klass.updateAdminRequestStatusInFirestore(email, newStatus)
    fun fetchLocalProfile(email: String): UserProfile? = com.mpi.klass.fetchLocalProfile(email)
    suspend fun fetchRemoteUserProfile(email: String): UserProfile? = fetchUserFromFirestore(email.trim().lowercase())
}
