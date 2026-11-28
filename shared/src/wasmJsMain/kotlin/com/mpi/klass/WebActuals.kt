package com.mpi.klass

// Web (Kotlin/Wasm) platform layer.
// Real implementations: session, time and storage helpers (browser localStorage).
// Firebase-backed data functions return empty results or Result.failure(WebBackendUnavailable)
// until the web backend (Firestore REST / Firebase JS) is connected.

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

actual fun isGestureNavigation(): Boolean = false

actual fun getDisplayBottomCornerRadiusDp(): Float = 0f

actual fun saveAppLanguage(language: AppLanguage) {
}

actual fun loadAppLanguage(): AppLanguage = AppLanguage.ENGLISH

@Composable
actual fun rememberLocationPermissionLauncher(onLocationFetched: (String) -> Unit): () -> Unit = { }

actual suspend fun isNetworkAvailable(): Boolean = true

actual fun loadCachedTranslations(): Map<String, String> = emptyMap()

actual fun saveCachedTranslations(entries: Map<String, String>) {
}

actual suspend fun fetchOnlineDictionaryFromCloud(): Map<String, String> = emptyMap()

actual suspend fun fetchOnlineTranslations(texts: List<String>, sourceLanguage: AppLanguage): Map<String, String> = emptyMap()

actual fun openPhoneDialer(phone: String) {
}

actual fun getTodayWorkingDayName(): String = dayNameFromJs()

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (String) -> Unit): () -> Unit = { }

actual fun resolveProfileImageModel(photoUrl: String?): Any? = null

@Composable
actual fun rememberDatePickerLauncher(initialDate: String, onDatePicked: (String) -> Unit): () -> Unit = { }

actual fun hasNotificationPermission(): Boolean = false

@Composable
actual fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit = { }

actual fun getNotificationPrefs(): NotificationPrefs = NotificationPrefs()

actual fun saveNotificationPrefs(prefs: NotificationPrefs) {
}

actual fun getSeenNotificationIds(): Set<String> = emptySet()

actual fun saveSeenNotificationIds(ids: Set<String>) {
}

actual fun sendWelcomePushNotification(userName: String, roleLabel: String) {
}

actual fun sendInstantTestPushNotification(title: String, body: String) {
}

actual fun syncAutomaticClassPushNotifications(
    entries: List<RoutineEntry>,
    config: RoutineConfig,
    role: UserRole?,
    prefs: NotificationPrefs,
): List<ScheduledClassAlert> = emptyList()

actual fun cancelAllClassPushNotifications() {
}

actual fun saveLocalProfile(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
    category: String,
) {
}

actual fun fetchLocalProfile(email: String): UserProfile? = null

actual fun fetchLocalStudentProfile(email: String): StudentProfile? = null

actual fun deleteLocalProfile(email: String) {
}

actual fun updateLocalCategory(email: String, category: String) {
}

actual suspend fun setOrUpdateLoginPassword(
    email: String,
    newPassword: String,
    currentPassword: String?,
): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun checkHasLoginPassword(email: String): Boolean = false

actual suspend fun signInWithEmailAndPasswordAccount(email: String, password: String): EmailPasswordLoginResult = EmailPasswordLoginResult.Error(ErrorImageType.entries.first(), "Sign-in is not available on the web version yet.")

actual suspend fun checkIsAuthorizedAdminEmail(email: String): Boolean = false

actual suspend fun saveAdminInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun saveTeacherInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun saveStudentInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun saveStudentProfileDataInFirestore(profile: StudentProfile): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchStudentProfileDataFromFirestore(email: String): StudentProfile? = null

actual suspend fun updateStudentAddressInFirestore(email: String, address: String): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun saveUserInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
    category: String,
): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchUserFromFirestore(email: String): UserProfile? = null

actual suspend fun deleteUserFromFirestore(email: String, role: String): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchSignInControlFromFirestore(): SignInControlConfig = SignInControlConfig()

actual suspend fun saveSignInControlInFirestore(config: SignInControlConfig): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchAppUpdateConfigFromFirestore(): AppUpdateConfig = AppUpdateConfig(enabled = false)

actual suspend fun saveAppUpdateConfigInFirestore(config: AppUpdateConfig): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun getAdminStatusFromFirestore(email: String): AdminStatus = AdminStatus.NOT_REQUESTED

actual suspend fun submitAdminRequestToFirestore(
    email: String,
    name: String,
    photoUrl: String?,
): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchPendingAdminRequestsFromFirestore(): List<AdminRequestItem> = emptyList()

actual suspend fun updateAdminRequestStatusInFirestore(email: String, newStatus: String): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchLoggedInTeachersFromFirestore(): List<TeacherDirectoryItem> = emptyList()

actual suspend fun deleteTeacherFromDatabaseInFirestore(email: String): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchLibraryBooksFromFirestore(): List<LibraryBook> = emptyList()

actual suspend fun saveLibraryBookInFirestore(book: LibraryBook): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun deleteLibraryBookFromFirestore(bookId: String): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun seedBtebLibraryBooksInFirestore(forceReseed: Boolean): Result<Int> = Result.failure(WebBackendUnavailable)

actual suspend fun resolveRegisteredRoleFromFirestore(email: String): UserRole? = null

actual suspend fun downloadRoutinePdf(
    label: String,
    shift: String,
    config: RoutineConfig,
    entries: List<RoutineEntry>,
): Result<String> = Result.failure(WebBackendUnavailable)

actual suspend fun saveTeacherSchedulingPrefInFirestore(pref: TeacherSchedulingPref): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchTeacherSchedulingPrefFromFirestore(email: String): TeacherSchedulingPref? = null

actual suspend fun fetchAllTeacherSchedulingPrefsFromFirestore(): List<TeacherSchedulingPref> = emptyList()

actual suspend fun fetchClassroomsFromFirestore(): List<ClassroomItem> = emptyList()

actual suspend fun saveClassroomInFirestore(room: ClassroomItem): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun deleteClassroomFromFirestore(roomId: String): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun deleteAllClassroomsFromFirestore(): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun saveRoutinePlanInFirestore(plan: RoutinePlan): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchRoutinePlanFromFirestore(docId: String): RoutinePlan? = null

actual suspend fun submitRoutineEditRequestInFirestore(request: RoutineEditRequest): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchRoutineEditRequestsFromFirestore(): List<RoutineEditRequest> = emptyList()

actual suspend fun resolveRoutineEditRequestInFirestore(
    requestId: String,
    newStatus: EditRequestStatus,
    adminNote: String,
): Result<Unit> = Result.failure(WebBackendUnavailable)

actual suspend fun fetchBloodDonorsFromFirestore(): List<BloodDonorItem> = emptyList()

actual fun showToast(message: String) {
    jsLog(message)
}

actual fun getCurrentTimeMillis(): Long = jsNowMillis().toLong()

actual fun saveUserSession(session: UserSession) {
    jsStorageSet("klass_session_role", session.role)
    jsStorageSet("klass_session_email", session.email)
    jsStorageSet("klass_session_name", session.name)
    val photo = session.photoUrl
    if (photo != null) jsStorageSet("klass_session_photo", photo) else jsStorageRemove("klass_session_photo")
}

actual fun loadUserSession(): UserSession? {
    val role = jsStorageGet("klass_session_role") ?: return null
    val email = jsStorageGet("klass_session_email") ?: return null
    return UserSession(
        role = role,
        email = email,
        name = jsStorageGet("klass_session_name") ?: "",
        photoUrl = jsStorageGet("klass_session_photo"),
    )
}

actual fun clearUserSession() {
    jsStorageRemove("klass_session_role")
    jsStorageRemove("klass_session_email")
    jsStorageRemove("klass_session_name")
    jsStorageRemove("klass_session_photo")
}

actual suspend fun askZedWithGemini(
    prompt: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
    history: List<AiChatMessage>,
    onPartialText: (String) -> Unit,
): ZedReply = ZedReply(answerText = "Zed AI is not available on the web version yet.", sourceBadge = "Web")

actual fun currentDayNameEnglish(): String = dayNameFromJs()

actual fun currentYearMonthDay(): Triple<Int, Int, Int> = Triple(jsYear(), jsMonth(), jsDayOfMonth())

actual suspend fun fetchZedKnowledgeFromFirestore(): List<ZedKnowledgeEntry> = emptyList()

actual suspend fun saveZedKnowledgeInFirestore(entry: ZedKnowledgeEntry): Result<Unit> = Result.failure(WebBackendUnavailable)
