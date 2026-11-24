package com.mpi.klass

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

enum class ProfilePrivacy(
    val key: String,
    val labelBn: String,
) {
    PUBLIC("PUBLIC", "পাবলিক"),
    PRIVATE("PRIVATE", "প্রাইভেট");

    companion object {
        fun fromKey(raw: String?): ProfilePrivacy = when (raw?.trim()?.uppercase()) {
            "CONNECTIONS", "FRIENDS", "PRIVATE", "ONLY_ME" -> PRIVATE
            else -> PUBLIC
        }
    }
}

const val PRIMARY_ADMIN_EMAIL = "mdrofiqmpi@gmail.com"

// Student / User Profile model supporting all Personal and Academic fields
data class StudentProfile(
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val coverPhotoUrl: String? = null,
    val semester: String = "",
    val shift: String = "",
    val department: String = "",
    val section: String = "",
    val phone: String = "",
    val bloodGroup: String = "",
    val address: String = "",
    val rollNumber: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val dob: String = "",
    val religion: String = "",
    val gender: String = "",
    val maritalStatus: String = "",
    val contactEmail: String = "",
    val permanentAddress: String = "",
    val registrationNumber: String = "",
    val profileSetupComplete: Boolean = true,
    val registeredAt: Long = 0L,
    val fieldPrivacy: Map<String, ProfilePrivacy> = emptyMap(),
) {
    fun privacyFor(fieldKey: String, defaultPrivacy: ProfilePrivacy = ProfilePrivacy.PUBLIC): ProfilePrivacy =
        fieldPrivacy[fieldKey] ?: defaultPrivacy
}

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

data class TeacherDirectoryItem(
    val email: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val phone: String = "",
    val contactEmail: String = "",
    val department: String = "",
    val shift: String = "",
    val bloodGroup: String = "",
    val gender: String = "",
    val address: String = "",
    val permanentAddress: String = "",
    val registeredAt: Long = 0L,
    val updatedAt: Long = 0L,
)

data class LibraryBook(
    val id: String = "",
    val title: String = "",
    val subjectCode: String = "",
    val departmentSemesters: Map<String, List<String>> = emptyMap(),
    val theoryClassesPerWeek: Int = 0,
    val practicalClassesPerWeek: Int = 0,
    val credit: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

sealed class EmailPasswordLoginResult {
    data class Success(
        val role: UserRole,
        val email: String,
        val name: String,
        val photoUrl: String? = null,
    ) : EmailPasswordLoginResult()

    data class Error(
        val errorType: ErrorImageType,
        val detail: String? = null,
    ) : EmailPasswordLoginResult()
}

enum class PasswordStrengthLevel {
    EMPTY,
    WEAK,
    FAIR,
    GOOD,
    STRONG,
}

data class PasswordSecurityEvaluation(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialChar: Boolean = false,
    val hasNoWhitespace: Boolean = false,
    val notCommonOrEmailDerived: Boolean = true,
    val score: Int = 0, // 0..4 segments
    val progress: Float = 0f,
    val level: PasswordStrengthLevel = PasswordStrengthLevel.EMPTY,
    val isAcceptable: Boolean = false,
)

private val COMMON_WEAK_PASSWORDS = setOf(
    "password", "password1", "password123", "12345678", "123456789", "1234567890",
    "qwerty123", "qwertyui", "admin123", "letmein1", "welcome1", "abc12345",
    "11111111", "00000000", "iloveyou", "pass@123", "admin@123",
)

fun evaluatePasswordStrength(password: String, email: String = ""): PasswordSecurityEvaluation {
    if (password.isEmpty()) return PasswordSecurityEvaluation()

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it in 'A'..'Z' }
    val hasLowercase = password.any { it in 'a'..'z' }
    val hasDigit = password.any { it in '0'..'9' }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
    val hasNoWhitespace = password.none { it.isWhitespace() }

    val lowerPw = password.lowercase()
    val emailLocal = email.trim().lowercase().substringBefore("@")
    val containsEmailPart = emailLocal.length >= 3 && lowerPw.contains(emailLocal)
    val isCommonWeak = lowerPw in COMMON_WEAK_PASSWORDS || password.toSet().size <= 2
    val notCommonOrEmailDerived = !containsEmailPart && !isCommonWeak

    val criteriaMetCount = listOf(
        hasMinLength,
        hasUppercase && hasLowercase,
        hasDigit,
        hasSpecialChar,
    ).count { it }

    val isAcceptable = hasMinLength &&
        hasUppercase &&
        hasLowercase &&
        hasDigit &&
        hasSpecialChar &&
        hasNoWhitespace &&
        notCommonOrEmailDerived

    val rawScore = when {
        !hasNoWhitespace || !notCommonOrEmailDerived -> 1
        criteriaMetCount == 0 -> 1
        criteriaMetCount == 1 -> 1
        criteriaMetCount == 2 -> 2
        criteriaMetCount == 3 -> 3
        isAcceptable && password.length >= 10 -> 4
        isAcceptable -> 4
        else -> 3
    }.coerceIn(1, 4)

    val level = when {
        !hasNoWhitespace || !notCommonOrEmailDerived -> PasswordStrengthLevel.WEAK
        rawScore <= 1 -> PasswordStrengthLevel.WEAK
        rawScore == 2 -> PasswordStrengthLevel.FAIR
        rawScore == 3 -> PasswordStrengthLevel.GOOD
        else -> PasswordStrengthLevel.STRONG
    }

    return PasswordSecurityEvaluation(
        hasMinLength = hasMinLength,
        hasUppercase = hasUppercase,
        hasLowercase = hasLowercase,
        hasDigit = hasDigit,
        hasSpecialChar = hasSpecialChar,
        hasNoWhitespace = hasNoWhitespace,
        notCommonOrEmailDerived = notCommonOrEmailDerived,
        score = rawScore,
        progress = rawScore / 4f,
        level = level,
        isAcceptable = isAcceptable,
    )
}

fun validateLoginPassword(
    password: String,
    confirmPassword: String? = null,
    email: String = "",
): String? {
    val eval = evaluatePasswordStrength(password, email)
    if (password.isEmpty()) {
        return "লগইন পাসওয়ার্ড লিখুন"
    }
    if (!eval.hasNoWhitespace) {
        return "পাসওয়ার্ডে কোনো স্পেস ব্যবহার করা যাবে না"
    }
    if (!eval.hasMinLength) {
        return "লগইন পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে"
    }
    if (!eval.notCommonOrEmailDerived) {
        return "ইমেইল বা সহজ পাসওয়ার্ডের পরিবর্তে শক্তিশালী পাসওয়ার্ড দিন"
    }
    if (!eval.hasUppercase || !eval.hasLowercase) {
        return "পাসওয়ার্ডে অন্তত একটি বড় (A-Z) ও একটি ছোট (a-z) অক্ষর থাকতে হবে"
    }
    if (!eval.hasDigit) {
        return "পাসওয়ার্ডে অন্তত একটি সংখ্যা (0-9) থাকতে হবে"
    }
    if (!eval.hasSpecialChar) {
        return "পাসওয়ার্ডে অন্তত একটি বিশেষ চিহ্ন (@, #, $, ! ইত্যাদি) থাকতে হবে"
    }
    if (confirmPassword != null && password != confirmPassword) {
        return "উভয় পাসওয়ার্ড একরকম হতে হবে"
    }
    return null
}

expect fun saveLocalProfile(email: String, name: String, photoUrl: String?, provider: String, category: String)
expect fun fetchLocalProfile(email: String): UserProfile?
expect fun fetchLocalStudentProfile(email: String): StudentProfile?
expect fun deleteLocalProfile(email: String)
expect fun updateLocalCategory(email: String, category: String)
expect suspend fun setOrUpdateLoginPassword(
    email: String,
    newPassword: String,
    currentPassword: String? = null,
): Result<Unit>
expect suspend fun checkHasLoginPassword(email: String): Boolean
expect suspend fun signInWithEmailAndPasswordAccount(email: String, password: String): EmailPasswordLoginResult
expect suspend fun checkIsAuthorizedAdminEmail(email: String): Boolean
expect suspend fun saveAdminInFirestore(email: String, name: String, photoUrl: String?): Result<Unit>
expect suspend fun saveTeacherInFirestore(email: String, name: String, photoUrl: String?, provider: String): Result<Unit>
expect suspend fun saveStudentInFirestore(email: String, name: String, photoUrl: String?, provider: String): Result<Unit>
expect suspend fun saveStudentProfileDataInFirestore(profile: StudentProfile): Result<Unit>
expect suspend fun fetchStudentProfileDataFromFirestore(email: String): StudentProfile?
expect suspend fun updateStudentAddressInFirestore(email: String, address: String): Result<Unit>
expect suspend fun saveUserInFirestore(email: String, name: String, photoUrl: String?, provider: String, category: String): Result<Unit>
expect suspend fun fetchUserFromFirestore(email: String): UserProfile?
expect suspend fun deleteUserFromFirestore(email: String, role: String): Result<Unit>
expect suspend fun fetchSignInControlFromFirestore(): SignInControlConfig
expect suspend fun saveSignInControlInFirestore(config: SignInControlConfig): Result<Unit>
expect suspend fun getAdminStatusFromFirestore(email: String): AdminStatus
expect suspend fun submitAdminRequestToFirestore(email: String, name: String, photoUrl: String?): Result<Unit>
expect suspend fun fetchPendingAdminRequestsFromFirestore(): List<AdminRequestItem>
expect suspend fun updateAdminRequestStatusInFirestore(email: String, newStatus: String): Result<Unit>
expect suspend fun fetchLoggedInTeachersFromFirestore(): List<TeacherDirectoryItem>
expect suspend fun deleteTeacherFromDatabaseInFirestore(email: String): Result<Unit>
expect suspend fun fetchLibraryBooksFromFirestore(): List<LibraryBook>
expect suspend fun saveLibraryBookInFirestore(book: LibraryBook): Result<Unit>
expect suspend fun deleteLibraryBookFromFirestore(bookId: String): Result<Unit>
expect suspend fun seedBtebLibraryBooksInFirestore(forceReseed: Boolean = false): Result<Int>
expect suspend fun resolveRegisteredRoleFromFirestore(email: String): UserRole?

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
        val remoteProf = fetchUserFromFirestore(cleanEmail)
        if (remoteProf != null) {
            val mergedName = remoteProf.name.ifBlank { localProf?.name ?: "" }
            val remotePhoto = remoteProf.photoUrl?.ifBlank { null }
            val localPhoto = localProf?.photoUrl?.ifBlank { null }
            val mergedPhoto = when {
                remotePhoto != null && remotePhoto.startsWith("data:image", ignoreCase = true) -> remotePhoto
                localPhoto != null && localPhoto.startsWith("data:image", ignoreCase = true) -> localPhoto
                else -> remotePhoto ?: localPhoto
            }
            val mergedCategory = remoteProf.category.ifBlank { localProf?.category ?: "STUDENT" }
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

        clearUserSession()
        return Result.success(Unit)
    }

    suspend fun resolveRegisteredRole(email: String): UserRole? {
        if (email.isBlank()) return null
        val cleanEmail = email.trim().lowercase()
        val remoteRole = resolveRegisteredRoleFromFirestore(cleanEmail)
        if (remoteRole != null) return remoteRole
        val localProf = fetchLocalProfile(cleanEmail)
        return UserRole.fromRaw(localProf?.category)
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
                profile != null && profile.category.trim().uppercase() == UserRole.STUDENT.name
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
        return saveStudentProfileDataInFirestore(profile)
    }

    suspend fun saveStudentAddress(email: String, address: String): Result<Unit> {
        if (email.isBlank() || address.isBlank()) return Result.failure(Exception("Invalid address"))
        return updateStudentAddressInFirestore(email.trim().lowercase(), address.trim())
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
    suspend fun fetchLoggedInTeachers(): List<TeacherDirectoryItem> = fetchLoggedInTeachersFromFirestore()
    suspend fun deleteTeacherFromDatabase(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Invalid email"))
        val cleanEmail = email.trim().lowercase()
        deleteLocalProfile(cleanEmail)
        return deleteTeacherFromDatabaseInFirestore(cleanEmail)
    }
    suspend fun fetchLibraryBooks(): List<LibraryBook> = fetchLibraryBooksFromFirestore()
    suspend fun saveLibraryBook(book: LibraryBook): Result<Unit> = saveLibraryBookInFirestore(book)
    suspend fun deleteLibraryBook(bookId: String): Result<Unit> = deleteLibraryBookFromFirestore(bookId)
    suspend fun seedBtebLibraryBooks(forceReseed: Boolean = false): Result<Int> = seedBtebLibraryBooksInFirestore(forceReseed)
    suspend fun saveLoginPassword(
        email: String,
        newPassword: String,
        currentPassword: String? = null,
    ): Result<Unit> =
        setOrUpdateLoginPassword(email, newPassword, currentPassword)
    suspend fun hasPasswordSet(email: String): Boolean =
        checkHasLoginPassword(email)
    suspend fun loginWithEmailPassword(email: String, password: String): EmailPasswordLoginResult =
        signInWithEmailAndPasswordAccount(email, password)
}
