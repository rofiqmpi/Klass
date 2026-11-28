package com.mpi.klass

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PW_ALGO_PBKDF2 = "pbkdf2_hmac_sha256_120k"
private const val PW_MAX_FAILED_ATTEMPTS = 5
private const val PW_LOCKOUT_DURATION_MS = 60_000L

private fun localDbKey(cleanEmail: String, field: String): String =
    "klass_local_${cleanEmail}_$field"

private fun encodeFieldPrivacyJson(map: Map<String, ProfilePrivacy>): String {
    if (map.isEmpty()) return ""
    return WebJson.stringify(
        jObj {
            map.forEach { (k, v) ->
                if (k.isNotBlank()) put(k.trim(), v.key)
            }
        }
    )
}

private fun decodeFieldPrivacyJson(raw: String?): Map<String, ProfilePrivacy> {
    val obj = WebJson.parseObj(raw) ?: return emptyMap()
    val out = linkedMapOf<String, ProfilePrivacy>()
    for ((k, v) in obj.entries) {
        val cleanKey = k.trim()
        if (cleanKey.isNotBlank()) {
            out[cleanKey] = ProfilePrivacy.fromKey(v.asStr())
        }
    }
    return out
}

private fun decodeFieldPrivacyMap(obj: JValue.JObj?): Map<String, ProfilePrivacy> {
    if (obj == null) return emptyMap()
    val out = linkedMapOf<String, ProfilePrivacy>()
    for ((k, v) in obj.entries) {
        val cleanKey = k.trim()
        val valStr = v.asStr()
        if (cleanKey.isNotBlank() && valStr.isNotBlank()) {
            out[cleanKey] = ProfilePrivacy.fromKey(valStr)
        }
    }
    return out
}

private fun encodeFieldPrivacyToJObj(map: Map<String, ProfilePrivacy>): JValue.JObj =
    jObj {
        map.forEach { (k, v) ->
            if (k.isNotBlank()) put(k.trim(), v.key)
        }
    }

actual fun saveLocalProfile(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
    category: String,
) {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return
    jsStorageSet(localDbKey(cleanEmail, "provider"), provider)
    jsStorageSet(localDbKey(cleanEmail, "category"), category)
    jsStorageSet(localDbKey(cleanEmail, "time"), jsNowMillis().toLong().toString())
    if (name.isNotBlank()) {
        jsStorageSet(localDbKey(cleanEmail, "name"), name)
    }
    if (!photoUrl.isNullOrBlank()) {
        jsStorageSet(localDbKey(cleanEmail, "photo"), photoUrl)
    }
}

actual fun fetchLocalProfile(email: String): UserProfile? {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return null
    val name = jsStorageGet(localDbKey(cleanEmail, "name")) ?: return null
    val photoUrl = jsStorageGet(localDbKey(cleanEmail, "photo"))?.ifBlank { null }
    val provider = jsStorageGet(localDbKey(cleanEmail, "provider")) ?: ""
    val category = jsStorageGet(localDbKey(cleanEmail, "category")) ?: "STUDENT"
    val time = jsStorageGet(localDbKey(cleanEmail, "time"))?.toLongOrNull() ?: 0L
    return UserProfile(
        email = cleanEmail,
        name = name,
        photoUrl = photoUrl,
        provider = provider,
        category = category,
        registeredAt = time,
        updatedAt = time,
    )
}

actual fun fetchLocalStudentProfile(email: String): StudentProfile? {
    if (email.isBlank()) return null
    val cleanEmail = email.trim().lowercase()
    val localName = jsStorageGet(localDbKey(cleanEmail, "student_name"))?.ifBlank { null }
        ?: jsStorageGet(localDbKey(cleanEmail, "name"))
        ?: ""
    val localBio = jsStorageGet(localDbKey(cleanEmail, "student_bio")) ?: ""
    val localPhoto = jsStorageGet(localDbKey(cleanEmail, "student_photo"))?.ifBlank { null }
        ?: jsStorageGet(localDbKey(cleanEmail, "photo"))?.ifBlank { null }
    val localCoverPhoto = jsStorageGet(localDbKey(cleanEmail, "student_cover_photo"))?.ifBlank { null }
    val localComplete = jsStorageGet(localDbKey(cleanEmail, "student_complete")) == "true"
    val semester = jsStorageGet(localDbKey(cleanEmail, "student_semester")) ?: ""
    val shift = jsStorageGet(localDbKey(cleanEmail, "student_shift")) ?: ""
    val dept = jsStorageGet(localDbKey(cleanEmail, "student_department")) ?: ""
    val section = jsStorageGet(localDbKey(cleanEmail, "student_section")) ?: ""
    val phone = jsStorageGet(localDbKey(cleanEmail, "student_phone")) ?: ""
    val blood = jsStorageGet(localDbKey(cleanEmail, "student_blood")) ?: ""
    val address = jsStorageGet(localDbKey(cleanEmail, "student_address")) ?: ""
    val roll = jsStorageGet(localDbKey(cleanEmail, "student_roll")) ?: ""
    val father = jsStorageGet(localDbKey(cleanEmail, "student_father")) ?: ""
    val mother = jsStorageGet(localDbKey(cleanEmail, "student_mother")) ?: ""
    val dob = jsStorageGet(localDbKey(cleanEmail, "student_dob")) ?: ""
    val religion = jsStorageGet(localDbKey(cleanEmail, "student_religion")) ?: ""
    val gender = jsStorageGet(localDbKey(cleanEmail, "student_gender")) ?: ""
    val marital = jsStorageGet(localDbKey(cleanEmail, "student_marital")) ?: ""
    val contactEmail = jsStorageGet(localDbKey(cleanEmail, "student_contact_email")) ?: ""
    val permAddr = jsStorageGet(localDbKey(cleanEmail, "student_permanent_address")) ?: ""
    val reg = jsStorageGet(localDbKey(cleanEmail, "student_registration")) ?: ""
    val privacyMap = decodeFieldPrivacyJson(jsStorageGet(localDbKey(cleanEmail, "student_field_privacy")))

    if (!localComplete && localName.isBlank() && localBio.isBlank() && localPhoto.isNullOrBlank() &&
        localCoverPhoto.isNullOrBlank() && phone.isBlank() && dept.isBlank() && roll.isBlank()
    ) {
        return null
    }

    return StudentProfile(
        email = cleanEmail,
        name = localName,
        bio = localBio,
        photoUrl = localPhoto,
        coverPhotoUrl = localCoverPhoto,
        semester = semester,
        shift = shift,
        department = dept,
        section = section,
        phone = phone,
        bloodGroup = blood,
        address = address,
        rollNumber = roll,
        fatherName = father,
        motherName = mother,
        dob = dob,
        religion = religion,
        gender = gender,
        maritalStatus = marital,
        contactEmail = contactEmail,
        permanentAddress = permAddr,
        registrationNumber = reg,
        profileSetupComplete = localComplete,
        fieldPrivacy = privacyMap,
    )
}

actual fun deleteLocalProfile(email: String) {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return
    val fields = listOf(
        "name", "photo", "provider", "category", "time",
        "student_name", "student_bio", "student_photo", "student_cover_photo",
        "student_semester", "student_shift", "student_department", "student_section",
        "student_phone", "student_blood", "student_address", "student_roll",
        "student_father", "student_mother", "student_dob", "student_religion",
        "student_gender", "student_marital", "student_contact_email",
        "student_permanent_address", "student_registration", "student_field_privacy",
        "student_complete", "student_time",
        "pw_salt", "pw_hash", "pw_algo", "has_pw", "pw_fail_count", "pw_lock_until",
    )
    fields.forEach { jsStorageRemove(localDbKey(cleanEmail, it)) }
}

actual fun updateLocalCategory(email: String, category: String) {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return
    jsStorageSet(localDbKey(cleanEmail, "category"), category)
}

// ─── Password Hashing & Auth Helpers ─────────────────────────────────────────

private suspend fun saveLocalPasswordHash(cleanEmail: String, password: String): Pair<String, String> {
    val salt = jsRandomSaltHex()
    val hash = WebFirebaseBridge.hashPbkdf2(salt, password) ?: ""
    if (hash.isNotBlank()) {
        jsStorageSet(localDbKey(cleanEmail, "pw_salt"), salt)
        jsStorageSet(localDbKey(cleanEmail, "pw_hash"), hash)
        jsStorageSet(localDbKey(cleanEmail, "pw_algo"), PW_ALGO_PBKDF2)
        jsStorageSet(localDbKey(cleanEmail, "has_pw"), "true")
        jsStorageRemove(localDbKey(cleanEmail, "pw_fail_count"))
        jsStorageRemove(localDbKey(cleanEmail, "pw_lock_until"))
    }
    return salt to hash
}

private fun hasLocalPasswordRecord(cleanEmail: String): Boolean {
    val hasPw = jsStorageGet(localDbKey(cleanEmail, "has_pw")) == "true"
    val salt = jsStorageGet(localDbKey(cleanEmail, "pw_salt")).orEmpty()
    val hash = jsStorageGet(localDbKey(cleanEmail, "pw_hash")).orEmpty()
    return hasPw && salt.isNotBlank() && hash.isNotBlank()
}

private fun getLockoutRemainingSeconds(cleanEmail: String): Long {
    val lockUntil = jsStorageGet(localDbKey(cleanEmail, "pw_lock_until"))?.toLongOrNull() ?: 0L
    val remainingMs = lockUntil - jsNowMillis().toLong()
    return if (remainingMs > 0L) ((remainingMs + 999L) / 1000L) else 0L
}

private fun recordFailedPasswordAttempt(cleanEmail: String): Long {
    val nextCount = (jsStorageGet(localDbKey(cleanEmail, "pw_fail_count"))?.toIntOrNull() ?: 0) + 1
    return if (nextCount >= PW_MAX_FAILED_ATTEMPTS) {
        val lockUntil = jsNowMillis().toLong() + PW_LOCKOUT_DURATION_MS
        jsStorageSet(localDbKey(cleanEmail, "pw_fail_count"), "0")
        jsStorageSet(localDbKey(cleanEmail, "pw_lock_until"), lockUntil.toString())
        PW_LOCKOUT_DURATION_MS / 1000L
    } else {
        jsStorageSet(localDbKey(cleanEmail, "pw_fail_count"), nextCount.toString())
        0L
    }
}

private fun resetPasswordAttemptCounter(cleanEmail: String) {
    jsStorageRemove(localDbKey(cleanEmail, "pw_fail_count"))
    jsStorageRemove(localDbKey(cleanEmail, "pw_lock_until"))
}

private suspend fun verifyLocalPasswordHash(cleanEmail: String, password: String): Boolean {
    val salt = jsStorageGet(localDbKey(cleanEmail, "pw_salt"))?.takeIf { it.isNotBlank() } ?: return false
    val expectedHash = jsStorageGet(localDbKey(cleanEmail, "pw_hash"))?.takeIf { it.isNotBlank() } ?: return false
    val actualHash = WebFirebaseBridge.hashPbkdf2(salt, password) ?: return false
    if (actualHash.length != expectedHash.length) return false
    var diff = 0
    for (i in expectedHash.indices) {
        diff = diff or (expectedHash[i].code xor actualHash[i].code)
    }
    return diff == 0
}

actual suspend fun setOrUpdateLoginPassword(
    email: String,
    newPassword: String,
    currentPassword: String?,
): Result<Unit> {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return Result.failure(Exception("সঠিক ইমেইল পাওয়া যায়নি"))

    val lockoutSecs = getLockoutRemainingSeconds(cleanEmail)
    if (lockoutSecs > 0L) {
        return Result.failure(Exception("অনেকবার ভুল পাসওয়ার্ড দেওয়া হয়েছে। $lockoutSecs সেকেন্ড পর আবার চেষ্টা করুন।"))
    }

    val validationErr = validateLoginPassword(newPassword, email = cleanEmail)
    if (validationErr != null) return Result.failure(Exception(validationErr))

    val hasExisting = checkHasLoginPassword(cleanEmail)
    if (hasExisting && !currentPassword.isNullOrEmpty()) {
        if (currentPassword == newPassword) {
            return Result.failure(Exception("নতুন পাসওয়ার্ডটি বর্তমান পাসওয়ার্ড থেকে ভিন্ন হতে হবে"))
        }
        var currentVerified = if (hasLocalPasswordRecord(cleanEmail)) {
            verifyLocalPasswordHash(cleanEmail, currentPassword)
        } else {
            false
        }
        if (!currentVerified) {
            val authRes = WebFirebaseBridge.setOrUpdateAuthPassword(cleanEmail, newPassword, currentPassword)
            if (authRes?.optBool("ok", false) == true) {
                currentVerified = true
            }
        }
        if (!currentVerified) {
            val lockAfterFail = recordFailedPasswordAttempt(cleanEmail)
            return if (lockAfterFail > 0L) {
                Result.failure(Exception("অনেকবার ভুল পাসওয়ার্ড দেওয়া হয়েছে। $lockAfterFail সেকেন্ড পর আবার চেষ্টা করুন।"))
            } else {
                Result.failure(Exception("বর্তমান পাসওয়ার্ডটি সঠিক নয়"))
            }
        }
        resetPasswordAttemptCounter(cleanEmail)
    } else if (hasExisting && currentPassword != null && currentPassword.isBlank()) {
        return Result.failure(Exception("বর্তমান পাসওয়ার্ডটি লিখুন"))
    }

    val (salt, hash) = saveLocalPasswordHash(cleanEmail, newPassword)
    val now = jsNowMillis().toLong()

    // Link / update Firebase Auth Email+Password credential if signed in as this email
    WebFirebaseBridge.setOrUpdateAuthPassword(cleanEmail, newPassword, currentPassword)

    if (hash.isNotBlank()) {
        val pwPatch = jObj {
            put("email", cleanEmail)
            put("password_salt", salt)
            put("password_hash", hash)
            put("password_algo", PW_ALGO_PBKDF2)
            put("has_password", true)
            put("password_updated_at", now)
            put("updated_at", now)
        }
        WebFirebaseBridge.setDoc("users", cleanEmail, pwPatch, merge = true, writeAllDbs = true)
    }

    return Result.success(Unit)
}

actual suspend fun checkHasLoginPassword(email: String): Boolean {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return false
    if (hasLocalPasswordRecord(cleanEmail)) return true

    val doc = WebFirebaseBridge.getDoc("users", cleanEmail, tryAllDbs = true) ?: return false
    val hasPw = doc.optBool("has_password", false)
    val salt = doc.optStr("password_salt").trim()
    val hash = doc.optStr("password_hash").trim()
    if (hasPw || (salt.isNotBlank() && hash.isNotBlank())) {
        if (salt.isNotBlank() && hash.isNotBlank()) {
            jsStorageSet(localDbKey(cleanEmail, "pw_salt"), salt)
            jsStorageSet(localDbKey(cleanEmail, "pw_hash"), hash)
            jsStorageSet(localDbKey(cleanEmail, "has_pw"), "true")
        }
        return true
    }
    return false
}

actual suspend fun signInWithEmailAndPasswordAccount(
    email: String,
    password: String,
): EmailPasswordLoginResult {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
        return EmailPasswordLoginResult.Error(
            ErrorImageType.GENERAL_ERROR,
            "সঠিক ইমেইল ঠিকানা লিখুন",
        )
    }
    if (password.isBlank()) {
        return EmailPasswordLoginResult.Error(
            ErrorImageType.GENERAL_ERROR,
            "অনুগ্রহ করে আপনার পাসওয়ার্ড লিখুন",
        )
    }

    val lockoutRemaining = getLockoutRemainingSeconds(cleanEmail)
    if (lockoutRemaining > 0L) {
        return EmailPasswordLoginResult.Error(
            ErrorImageType.GENERAL_ERROR,
            "অনেকবার ভুল পাসওয়ার্ড দেওয়া হয়েছে। $lockoutRemaining সেকেন্ড পর আবার চেষ্টা করুন।",
        )
    }

    var firebaseAuthSucceeded = false
    var networkFailed = !jsIsOnline()
    var emailProviderDisabled = false

    if (!networkFailed) {
        val authRes = WebFirebaseBridge.signInWithEmail(cleanEmail, password)
        if (authRes?.optBool("ok", false) == true) {
            firebaseAuthSucceeded = true
            resetPasswordAttemptCounter(cleanEmail)
            saveLocalPasswordHash(cleanEmail, password)
        } else {
            val err = authRes?.optStr("error").orEmpty()
            val code = authRes?.optStr("errorCode").orEmpty()
            if (err == "NO_INTERNET") networkFailed = true
            if (err == "ERROR_OPERATION_NOT_ALLOWED" || code == "auth/operation-not-allowed") {
                emailProviderDisabled = true
            }
            if (err == "WEB_FIREBASE_CONFIG_MISSING") {
                return EmailPasswordLoginResult.Error(ErrorImageType.CONFIGURATION_ERROR, err)
            }
        }
    }

    val hasLocalRecord = hasLocalPasswordRecord(cleanEmail)
    if (!firebaseAuthSucceeded) {
        if (hasLocalRecord) {
            if (!verifyLocalPasswordHash(cleanEmail, password)) {
                val lockedFor = recordFailedPasswordAttempt(cleanEmail)
                val msg = if (lockedFor > 0L) {
                    "অনেকবার ভুল পাসওয়ার্ড দেওয়া হয়েছে। $lockedFor সেকেন্ড পর আবার চেষ্টা করুন।"
                } else {
                    "ইমেইল অথবা পাসওয়ার্ড সঠিক নয়। দয়া করে আবার চেষ্টা করুন।"
                }
                return EmailPasswordLoginResult.Error(ErrorImageType.GENERAL_ERROR, msg)
            }
            resetPasswordAttemptCounter(cleanEmail)
            if (jsIsOnline() && !WebFirebaseBridge.isSignedIn()) {
                val created = WebFirebaseBridge.createUserWithEmail(cleanEmail, password)
                if (created?.optBool("ok", false) == true) {
                    firebaseAuthSucceeded = true
                }
            }
        } else {
            if (!networkFailed && jsIsOnline() && !emailProviderDisabled) {
                recordFailedPasswordAttempt(cleanEmail)
            }
            return when {
                networkFailed || !jsIsOnline() ->
                    EmailPasswordLoginResult.Error(ErrorImageType.NO_INTERNET)
                emailProviderDisabled ->
                    EmailPasswordLoginResult.Error(
                        ErrorImageType.GENERAL_ERROR,
                        "Firebase Console > Authentication > Sign-in method থেকে Email/Password চালু (Enable) করা নেই। প্রথমে Google দিয়ে সাইন-ইন করুন অথবা Firebase Console-এ Email/Password চালু করুন।",
                    )
                else ->
                    EmailPasswordLoginResult.Error(
                        ErrorImageType.GENERAL_ERROR,
                        "ইমেইল অথবা পাসওয়ার্ড সঠিক নয়। নতুন ব্যবহারকারী হলে 'শুরু করুন' থেকে Google দিয়ে সাইন-ইন করে প্রোফাইলে পাসওয়ার্ড সেট করুন।",
                    )
            }
        }
    }

    val localProf = fetchLocalProfile(cleanEmail)
    val remoteRegisteredRole = if (jsIsOnline()) resolveRegisteredRoleFromFirestore(cleanEmail) else null
    val remoteProf = if (firebaseAuthSucceeded && jsIsOnline()) RoleConfig.fetchUserProfile(cleanEmail) else null
    val mergedProf = remoteProf ?: localProf
    val studentProf = RoleConfig.fetchStudentProfile(cleanEmail)

    var resolvedRole = remoteRegisteredRole
        ?: UserRole.fromRaw(mergedProf?.category)
        ?: UserRole.STUDENT

    if (firebaseAuthSucceeded && jsIsOnline()) {
        val adminStatus = getAdminStatusFromFirestore(cleanEmail)
        if (adminStatus == AdminStatus.DECLINED) {
            return EmailPasswordLoginResult.Error(ErrorImageType.ADMIN_REQUEST_DECLINED)
        }
        if (adminStatus == AdminStatus.PENDING && resolvedRole == UserRole.ADMIN) {
            return EmailPasswordLoginResult.Error(ErrorImageType.ADMIN_REQUEST_PENDING)
        }
        if (adminStatus == AdminStatus.APPROVED) {
            if (resolvedRole != UserRole.ADMIN && remoteRegisteredRole != null && remoteRegisteredRole != UserRole.ADMIN) {
                return EmailPasswordLoginResult.Error(ErrorImageType.ROLE_MISMATCH)
            }
            resolvedRole = UserRole.ADMIN
        } else if (resolvedRole == UserRole.ADMIN) {
            return EmailPasswordLoginResult.Error(ErrorImageType.ADMIN_NOT_AUTHORIZED)
        }

        val control = fetchSignInControlFromFirestore()
        val roleEnabled = when (resolvedRole) {
            UserRole.ADMIN -> control.adminSignInEnabled
            UserRole.TEACHER -> control.teacherSignInEnabled
            UserRole.STUDENT -> control.studentSignInEnabled
        }
        if (!roleEnabled && !RoleConfig.isUserRegisteredForRole(cleanEmail, resolvedRole)) {
            return EmailPasswordLoginResult.Error(ErrorImageType.SIGN_IN_DISABLED)
        }
    }

    val finalName = mergedProf?.name?.ifBlank { null }
        ?: studentProf?.name?.ifBlank { null }
        ?: cleanEmail.substringBefore("@")
    val finalPhoto = listOfNotNull(
        mergedProf?.photoUrl?.ifBlank { null },
        studentProf?.photoUrl?.ifBlank { null },
    ).firstOrNull { it.startsWith("data:image") }
        ?: mergedProf?.photoUrl?.ifBlank { null }
        ?: studentProf?.photoUrl?.ifBlank { null }

    saveLocalProfile(cleanEmail, finalName, finalPhoto, mergedProf?.provider ?: "email", resolvedRole.name)
    return EmailPasswordLoginResult.Success(
        role = resolvedRole,
        email = cleanEmail,
        name = finalName,
        photoUrl = finalPhoto,
    )
}

// ─── Admin Authorization & User Collections ──────────────────────────────────

private fun isDocMatchingEmail(docId: String, data: JValue.JObj, cleanEmail: String): Boolean {
    if (!data.optBool("enabled", true)) return false
    if (docId.trim().lowercase() == cleanEmail) return true
    val emailFields = listOf("email", "Email", "mail", "admin_email", "adminEmail", "user_email", "userEmail")
    for (f in emailFields) {
        if (data.optStr(f).trim().lowercase() == cleanEmail) return true
    }
    for ((_, value) in data.entries) {
        if (value is JValue.JStr && value.value.trim().lowercase() == cleanEmail) return true
        if (value is JValue.JArr) {
            if (value.items.any { it is JValue.JStr && it.value.trim().lowercase() == cleanEmail }) return true
        }
    }
    return false
}

actual suspend fun checkIsAuthorizedAdminEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail == PRIMARY_ADMIN_EMAIL) return true

    for (colName in listOf("admins", "admin")) {
        val direct = WebFirebaseBridge.getDoc(colName, cleanEmail, tryAllDbs = true)
        if (direct != null && isDocMatchingEmail(cleanEmail, direct, cleanEmail)) return true

        val queried = WebFirebaseBridge.queryEquals(colName, "email", cleanEmail, mergeAllDbs = true)
        if (queried.any { isDocMatchingEmail(it.id, it.data, cleanEmail) }) return true

        val (_, allDocs) = WebFirebaseBridge.getCollection(colName, mergeAllDbs = true)
        if (allDocs.any { isDocMatchingEmail(it.id, it.data, cleanEmail) }) return true
    }
    return false
}

actual suspend fun saveAdminInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = jsNowMillis().toLong()
    val data = jObj {
        put("email", cleanEmail)
        put("name", name)
        put("role", "ADMIN")
        put("status", "APPROVED")
        put("enabled", true)
        put("last_login_at", now)
        put("updated_at", now)
        if (!photoUrl.isNullOrBlank()) put("photo_url", photoUrl)
    }
    val res = WebFirebaseBridge.setDoc("admins", cleanEmail, data, merge = true, writeAllDbs = false)
    return if (res.isSuccess || !jsIsOnline()) Result.success(Unit) else res
}

actual suspend fun saveTeacherInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = jsNowMillis().toLong()
    val data = jObj {
        put("email", cleanEmail)
        put("name", name)
        put("role", "TEACHER")
        put("provider", provider)
        put("registered_at", now)
        put("updated_at", now)
        if (!photoUrl.isNullOrBlank()) put("photo_url", photoUrl)
    }
    val res = WebFirebaseBridge.setDoc("teachers", cleanEmail, data, merge = true, writeAllDbs = false)
    return if (res.isSuccess || !jsIsOnline()) Result.success(Unit) else res
}

actual suspend fun saveStudentInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = jsNowMillis().toLong()
    val data = jObj {
        put("email", cleanEmail)
        put("name", name)
        put("role", "STUDENT")
        put("provider", provider)
        put("registered_at", now)
        put("updated_at", now)
        if (!photoUrl.isNullOrBlank()) put("photo_url", photoUrl)
    }
    val res = WebFirebaseBridge.setDoc("students", cleanEmail, data, merge = true, writeAllDbs = false)
    return if (res.isSuccess || !jsIsOnline()) Result.success(Unit) else res
}

actual suspend fun saveStudentProfileDataInFirestore(profile: StudentProfile): Result<Unit> {
    if (profile.email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = profile.email.trim().lowercase()
    val now = jsNowMillis().toLong()
    val category = jsStorageGet(localDbKey(cleanEmail, "category"))
        ?.trim()?.uppercase()?.ifBlank { "STUDENT" } ?: "STUDENT"

    jsStorageSet(localDbKey(cleanEmail, "student_bio"), profile.bio)
    jsStorageSet(localDbKey(cleanEmail, "student_semester"), profile.semester)
    jsStorageSet(localDbKey(cleanEmail, "student_shift"), profile.shift)
    jsStorageSet(localDbKey(cleanEmail, "student_department"), profile.department)
    jsStorageSet(localDbKey(cleanEmail, "student_section"), profile.section)
    jsStorageSet(localDbKey(cleanEmail, "student_phone"), profile.phone)
    jsStorageSet(localDbKey(cleanEmail, "student_blood"), profile.bloodGroup)
    jsStorageSet(localDbKey(cleanEmail, "student_address"), profile.address)
    jsStorageSet(localDbKey(cleanEmail, "student_roll"), profile.rollNumber)
    jsStorageSet(localDbKey(cleanEmail, "student_father"), profile.fatherName)
    jsStorageSet(localDbKey(cleanEmail, "student_mother"), profile.motherName)
    jsStorageSet(localDbKey(cleanEmail, "student_dob"), profile.dob)
    jsStorageSet(localDbKey(cleanEmail, "student_religion"), profile.religion)
    jsStorageSet(localDbKey(cleanEmail, "student_gender"), profile.gender)
    jsStorageSet(localDbKey(cleanEmail, "student_marital"), profile.maritalStatus)
    jsStorageSet(localDbKey(cleanEmail, "student_contact_email"), profile.contactEmail)
    jsStorageSet(localDbKey(cleanEmail, "student_permanent_address"), profile.permanentAddress)
    jsStorageSet(localDbKey(cleanEmail, "student_registration"), profile.registrationNumber)
    jsStorageSet(localDbKey(cleanEmail, "student_field_privacy"), encodeFieldPrivacyJson(profile.fieldPrivacy))
    jsStorageSet(localDbKey(cleanEmail, "student_complete"), profile.profileSetupComplete.toString())
    jsStorageSet(localDbKey(cleanEmail, "student_time"), now.toString())
    if (jsStorageGet(localDbKey(cleanEmail, "category")).isNullOrBlank()) {
        jsStorageSet(localDbKey(cleanEmail, "category"), category)
    }
    if (profile.name.isNotBlank()) {
        jsStorageSet(localDbKey(cleanEmail, "name"), profile.name)
        jsStorageSet(localDbKey(cleanEmail, "student_name"), profile.name)
    }
    if (!profile.photoUrl.isNullOrBlank()) {
        jsStorageSet(localDbKey(cleanEmail, "photo"), profile.photoUrl)
        jsStorageSet(localDbKey(cleanEmail, "student_photo"), profile.photoUrl)
    }
    if (!profile.coverPhotoUrl.isNullOrBlank()) {
        jsStorageSet(localDbKey(cleanEmail, "student_cover_photo"), profile.coverPhotoUrl)
    }

    val resolvedCategory = jsStorageGet(localDbKey(cleanEmail, "category"))
        ?.trim()?.uppercase()?.ifBlank { null }
        ?: resolveRegisteredRoleFromFirestore(cleanEmail)?.name
        ?: category
    val finalRoleCollection = when (resolvedCategory) {
        "TEACHER" -> "teachers"
        "ADMIN" -> "admins"
        else -> "students"
    }

    if (jsIsOnline() && !WebFirebaseBridge.isSignedIn()) {
        return Result.failure(Exception("সেশন মেয়াদ উত্তীর্ণ হয়েছে। দয়া করে Google দিয়ে পুনরায় সাইন-ইন করুন।"))
    }

    val privacyObj = encodeFieldPrivacyToJObj(profile.fieldPrivacy)
    val data = jObj {
        put("email", cleanEmail)
        put("name", profile.name)
        put("bio", profile.bio)
        put("role", resolvedCategory)
        put("semester", profile.semester)
        put("shift", profile.shift)
        put("department", profile.department)
        put("section", profile.section)
        put("phone", profile.phone)
        put("blood_group", profile.bloodGroup)
        put("address", profile.address)
        put("roll_number", profile.rollNumber)
        put("father_name", profile.fatherName)
        put("mother_name", profile.motherName)
        put("dob", profile.dob)
        put("religion", profile.religion)
        put("gender", profile.gender)
        put("marital_status", profile.maritalStatus)
        put("contact_email", profile.contactEmail)
        put("permanent_address", profile.permanentAddress)
        put("registration_number", profile.registrationNumber)
        put("field_privacy", privacyObj)
        put("profile_setup_complete", profile.profileSetupComplete)
        put("updated_at", now)
        if (!profile.photoUrl.isNullOrBlank()) put("photo_url", profile.photoUrl)
        if (!profile.coverPhotoUrl.isNullOrBlank()) put("cover_photo_url", profile.coverPhotoUrl)
    }

    val userPatch = jObj {
        put("email", cleanEmail)
        put("name", profile.name)
        put("bio", profile.bio)
        put("category", resolvedCategory)
        put("phone", profile.phone)
        put("department", profile.department)
        put("address", profile.address)
        put("field_privacy", privacyObj)
        put("updated_at", now)
        if (!profile.photoUrl.isNullOrBlank()) put("photo_url", profile.photoUrl)
        if (!profile.coverPhotoUrl.isNullOrBlank()) put("cover_photo_url", profile.coverPhotoUrl)
    }

    val roleRes = WebFirebaseBridge.setDoc(finalRoleCollection, cleanEmail, data, merge = true, writeAllDbs = false)
    val userRes = WebFirebaseBridge.setDoc("users", cleanEmail, userPatch, merge = true, writeAllDbs = false)
    if (roleRes.isSuccess) return Result.success(Unit)
    return if (!jsIsOnline()) Result.success(Unit) else roleRes.exceptionOrNull()?.let { Result.failure(it) } ?: userRes
}

actual suspend fun fetchStudentProfileDataFromFirestore(email: String): StudentProfile? {
    if (email.isBlank()) return null
    val cleanEmail = email.trim().lowercase()

    val localTime = jsStorageGet(localDbKey(cleanEmail, "student_time"))?.toLongOrNull() ?: 0L
    val localComplete = jsStorageGet(localDbKey(cleanEmail, "student_complete")) == "true"
    val localPhoto = jsStorageGet(localDbKey(cleanEmail, "student_photo"))?.ifBlank { null }
        ?: jsStorageGet(localDbKey(cleanEmail, "photo"))?.ifBlank { null }
    val localCoverPhoto = jsStorageGet(localDbKey(cleanEmail, "student_cover_photo"))?.ifBlank { null }
    val localProfile = fetchLocalStudentProfile(cleanEmail) ?: StudentProfile(
        email = cleanEmail,
        name = fetchLocalProfile(cleanEmail)?.name ?: "",
        photoUrl = localPhoto,
        coverPhotoUrl = localCoverPhoto,
        profileSetupComplete = false,
    )

    val localCategory = jsStorageGet(localDbKey(cleanEmail, "category"))?.trim()?.uppercase()
    val resolvedCategory = localCategory?.takeIf { it.isNotBlank() }
        ?: resolveRegisteredRoleFromFirestore(cleanEmail)?.name
        ?: "STUDENT"
    val primaryCol = when (resolvedCategory) {
        "TEACHER" -> "teachers"
        "ADMIN" -> "admins"
        else -> "students"
    }

    var doc = WebFirebaseBridge.getDoc(primaryCol, cleanEmail, tryAllDbs = true)
    if (doc == null && primaryCol != "students") {
        doc = WebFirebaseBridge.getDoc("students", cleanEmail, tryAllDbs = true)
    }

    if (doc != null) {
        val remoteUpdatedAt = doc.optLong("updated_at", 0L)
        val remoteComplete = doc.optBool("profile_setup_complete", false)
        val remotePhoto = doc.optNullableStr("photo_url")
        val remoteCoverPhoto = doc.optNullableStr("cover_photo_url")
        val remotePrivacy = decodeFieldPrivacyMap(doc.optObj("field_privacy"))

        val preferLocal = localTime > remoteUpdatedAt && localComplete
        fun pick(remoteKey: String, localVal: String): String {
            val remoteVal = doc.optStr(remoteKey)
            return if (preferLocal) localVal.ifBlank { remoteVal } else remoteVal.ifBlank { localVal }
        }

        val mergedPrivacy = if (preferLocal) {
            remotePrivacy + localProfile.fieldPrivacy
        } else {
            localProfile.fieldPrivacy + remotePrivacy
        }
        val mergedPhoto = when {
            preferLocal && !localPhoto.isNullOrBlank() -> localPhoto
            remotePhoto != null && remotePhoto.startsWith("data:image", ignoreCase = true) -> remotePhoto
            localPhoto != null && localPhoto.startsWith("data:image", ignoreCase = true) -> localPhoto
            else -> remotePhoto ?: localPhoto
        }
        val mergedCoverPhoto = when {
            preferLocal && !localCoverPhoto.isNullOrBlank() -> localCoverPhoto
            remoteCoverPhoto != null && remoteCoverPhoto.startsWith("data:image", ignoreCase = true) -> remoteCoverPhoto
            localCoverPhoto != null && localCoverPhoto.startsWith("data:image", ignoreCase = true) -> localCoverPhoto
            else -> remoteCoverPhoto ?: localCoverPhoto
        }

        val merged = StudentProfile(
            email = doc.optStr("email").ifBlank { cleanEmail },
            name = pick("name", localProfile.name),
            bio = pick("bio", localProfile.bio),
            photoUrl = mergedPhoto,
            coverPhotoUrl = mergedCoverPhoto,
            semester = pick("semester", localProfile.semester),
            shift = pick("shift", localProfile.shift),
            department = pick("department", localProfile.department),
            section = pick("section", localProfile.section),
            phone = pick("phone", localProfile.phone),
            bloodGroup = pick("blood_group", localProfile.bloodGroup),
            address = pick("address", localProfile.address),
            rollNumber = pick("roll_number", localProfile.rollNumber),
            fatherName = pick("father_name", localProfile.fatherName),
            motherName = pick("mother_name", localProfile.motherName),
            dob = pick("dob", localProfile.dob),
            religion = pick("religion", localProfile.religion),
            gender = pick("gender", localProfile.gender),
            maritalStatus = pick("marital_status", localProfile.maritalStatus),
            contactEmail = pick("contact_email", localProfile.contactEmail),
            permanentAddress = pick("permanent_address", localProfile.permanentAddress),
            registrationNumber = pick("registration_number", localProfile.registrationNumber),
            profileSetupComplete = remoteComplete || localComplete,
            fieldPrivacy = mergedPrivacy,
        )

        jsStorageSet(localDbKey(cleanEmail, "student_bio"), merged.bio)
        jsStorageSet(localDbKey(cleanEmail, "student_semester"), merged.semester)
        jsStorageSet(localDbKey(cleanEmail, "student_shift"), merged.shift)
        jsStorageSet(localDbKey(cleanEmail, "student_department"), merged.department)
        jsStorageSet(localDbKey(cleanEmail, "student_section"), merged.section)
        jsStorageSet(localDbKey(cleanEmail, "student_phone"), merged.phone)
        jsStorageSet(localDbKey(cleanEmail, "student_blood"), merged.bloodGroup)
        jsStorageSet(localDbKey(cleanEmail, "student_address"), merged.address)
        jsStorageSet(localDbKey(cleanEmail, "student_roll"), merged.rollNumber)
        jsStorageSet(localDbKey(cleanEmail, "student_father"), merged.fatherName)
        jsStorageSet(localDbKey(cleanEmail, "student_mother"), merged.motherName)
        jsStorageSet(localDbKey(cleanEmail, "student_dob"), merged.dob)
        jsStorageSet(localDbKey(cleanEmail, "student_religion"), merged.religion)
        jsStorageSet(localDbKey(cleanEmail, "student_gender"), merged.gender)
        jsStorageSet(localDbKey(cleanEmail, "student_marital"), merged.maritalStatus)
        jsStorageSet(localDbKey(cleanEmail, "student_contact_email"), merged.contactEmail)
        jsStorageSet(localDbKey(cleanEmail, "student_permanent_address"), merged.permanentAddress)
        jsStorageSet(localDbKey(cleanEmail, "student_registration"), merged.registrationNumber)
        jsStorageSet(localDbKey(cleanEmail, "student_field_privacy"), encodeFieldPrivacyJson(merged.fieldPrivacy))
        jsStorageSet(localDbKey(cleanEmail, "student_complete"), merged.profileSetupComplete.toString())
        jsStorageSet(localDbKey(cleanEmail, "student_time"), maxOf(localTime, remoteUpdatedAt).toString())
        if (merged.name.isNotBlank()) {
            jsStorageSet(localDbKey(cleanEmail, "name"), merged.name)
            jsStorageSet(localDbKey(cleanEmail, "student_name"), merged.name)
        }
        if (!merged.photoUrl.isNullOrBlank()) {
            jsStorageSet(localDbKey(cleanEmail, "photo"), merged.photoUrl)
            jsStorageSet(localDbKey(cleanEmail, "student_photo"), merged.photoUrl)
        }
        if (!merged.coverPhotoUrl.isNullOrBlank()) {
            jsStorageSet(localDbKey(cleanEmail, "student_cover_photo"), merged.coverPhotoUrl)
        }
        return merged
    }

    val hasAnyLocal = localComplete || localProfile.semester.isNotBlank() || localProfile.department.isNotBlank() ||
        localProfile.rollNumber.isNotBlank() || localProfile.fatherName.isNotBlank() ||
        localProfile.registrationNumber.isNotBlank() || localProfile.phone.isNotBlank() ||
        localProfile.address.isNotBlank() || !localPhoto.isNullOrBlank() || !localCoverPhoto.isNullOrBlank() ||
        localProfile.name.isNotBlank()
    if (hasAnyLocal) return localProfile

    val basicUser = fetchLocalProfile(cleanEmail)
    return StudentProfile(email = cleanEmail, name = basicUser?.name ?: "", photoUrl = basicUser?.photoUrl)
}

actual suspend fun updateStudentAddressInFirestore(email: String, address: String): Result<Unit> {
    if (email.isBlank() || address.isBlank()) return Result.failure(Exception("Invalid address"))
    val cleanEmail = email.trim().lowercase()
    val cleanAddress = address.trim()
    val now = jsNowMillis().toLong()
    jsStorageSet(localDbKey(cleanEmail, "student_address"), cleanAddress)
    jsStorageSet(localDbKey(cleanEmail, "student_time"), now.toString())

    val category = jsStorageGet(localDbKey(cleanEmail, "category"))?.trim()?.uppercase() ?: ""
    val roleCollection = when (category) {
        "TEACHER" -> "teachers"
        "ADMIN" -> "admins"
        else -> "students"
    }
    val patch = jObj {
        put("address", cleanAddress)
        put("updated_at", now)
    }
    WebFirebaseBridge.setDoc(roleCollection, cleanEmail, patch, merge = true, writeAllDbs = true)
    WebFirebaseBridge.setDoc("users", cleanEmail, patch, merge = true, writeAllDbs = true)
    return Result.success(Unit)
}

actual suspend fun saveUserInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
    category: String,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = jsNowMillis().toLong()
    val data = jObj {
        put("email", cleanEmail)
        put("name", name)
        put("provider", provider)
        put("category", category)
        put("updated_at", now)
        if (!photoUrl.isNullOrBlank()) put("photo_url", photoUrl)
    }
    val res = WebFirebaseBridge.setDoc("users", cleanEmail, data, merge = true, writeAllDbs = false)
    return if (res.isSuccess || !jsIsOnline()) Result.success(Unit) else res
}

actual suspend fun fetchUserFromFirestore(email: String): UserProfile? {
    if (email.isBlank()) return null
    val cleanEmail = email.trim().lowercase()

    WebFirebaseBridge.getDoc("admins", cleanEmail, tryAllDbs = true)?.let { d ->
        val st = d.optStr("status").trim().uppercase()
        if (st != "DECLINED") {
            return UserProfile(
                email = d.optStr("email").ifBlank { cleanEmail },
                name = d.optStr("name").ifBlank { cleanEmail.substringBefore("@") },
                photoUrl = d.optNullableStr("photo_url"),
                provider = d.optStr("provider").ifBlank { "google" },
                category = "ADMIN",
                registeredAt = d.optLong("registered_at", 0L),
                updatedAt = d.optLong("updated_at", 0L),
            )
        }
    }

    WebFirebaseBridge.getDoc("teachers", cleanEmail, tryAllDbs = true)?.let { d ->
        return UserProfile(
            email = d.optStr("email").ifBlank { cleanEmail },
            name = d.optStr("name"),
            photoUrl = d.optNullableStr("photo_url"),
            provider = d.optStr("provider").ifBlank { "google" },
            category = "TEACHER",
            registeredAt = d.optLong("registered_at", 0L),
            updatedAt = d.optLong("updated_at", 0L),
        )
    }

    WebFirebaseBridge.getDoc("students", cleanEmail, tryAllDbs = true)?.let { d ->
        return UserProfile(
            email = d.optStr("email").ifBlank { cleanEmail },
            name = d.optStr("name"),
            photoUrl = d.optNullableStr("photo_url"),
            provider = d.optStr("provider").ifBlank { "google" },
            category = "STUDENT",
            registeredAt = d.optLong("registered_at", 0L),
            updatedAt = d.optLong("updated_at", 0L),
        )
    }

    WebFirebaseBridge.getDoc("users", cleanEmail, tryAllDbs = true)?.let { d ->
        return UserProfile(
            email = d.optStr("email").ifBlank { cleanEmail },
            name = d.optStr("name"),
            photoUrl = d.optNullableStr("photo_url"),
            provider = d.optStr("provider"),
            category = d.optStr("category").ifBlank { "STUDENT" },
            registeredAt = d.optLong("registered_at", 0L),
            updatedAt = d.optLong("updated_at", 0L),
        )
    }
    return null
}

actual suspend fun deleteUserFromFirestore(email: String, role: String): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    WebFirebaseBridge.deleteDoc("users", cleanEmail, writeAllDbs = true)
    when (role.trim().uppercase()) {
        "ADMIN" -> WebFirebaseBridge.deleteDoc("admins", cleanEmail, writeAllDbs = true)
        "TEACHER" -> WebFirebaseBridge.deleteDoc("teachers", cleanEmail, writeAllDbs = true)
        "STUDENT" -> WebFirebaseBridge.deleteDoc("students", cleanEmail, writeAllDbs = true)
    }
    WebFirebaseBridge.deleteCurrentAuthUser()
    return Result.success(Unit)
}

// ─── Config & Admin Controls ─────────────────────────────────────────────────

actual suspend fun fetchSignInControlFromFirestore(): SignInControlConfig {
    val doc = WebFirebaseBridge.getDoc("config", "signin_control", tryAllDbs = true) ?: return SignInControlConfig()
    return SignInControlConfig(
        adminSignInEnabled = doc.optBool("adminSignInEnabled", true),
        teacherSignInEnabled = doc.optBool("teacherSignInEnabled", true),
        studentSignInEnabled = doc.optBool("studentSignInEnabled", true),
    )
}

actual suspend fun saveSignInControlInFirestore(config: SignInControlConfig): Result<Unit> {
    val data = jObj {
        put("adminSignInEnabled", config.adminSignInEnabled)
        put("teacherSignInEnabled", config.teacherSignInEnabled)
        put("studentSignInEnabled", config.studentSignInEnabled)
        put("updatedAt", jsNowMillis().toLong())
    }
    return WebFirebaseBridge.setDoc("config", "signin_control", data, merge = true, writeAllDbs = true)
}

actual suspend fun fetchAppUpdateConfigFromFirestore(): AppUpdateConfig {
    val doc = WebFirebaseBridge.getDoc("config", "app_update", tryAllDbs = true) ?: return AppUpdateConfig()
    val version = doc.optStr("latest_version")
        .ifBlank { doc.optStr("version") }
        .ifBlank { doc.optStr("versionName") }
        .ifBlank { doc.optStr("tag") }
        .ifBlank { doc.optStr("tag_name") }
        .trim()
    val apkUrl = doc.optStr("apk_url")
        .ifBlank { doc.optStr("download_url") }
        .ifBlank { doc.optStr("apkUrl") }
        .ifBlank { doc.optStr("downloadUrl") }
        .trim()
    return AppUpdateConfig(
        enabled = doc.optBool("enabled", true),
        latestVersion = version,
        apkUrl = apkUrl,
        notes = doc.optStr("notes").trim(),
        force = doc.optBool("force", false),
        updatedAt = doc.optLong("updated_at", 0L),
    )
}

actual suspend fun saveAppUpdateConfigInFirestore(config: AppUpdateConfig): Result<Unit> {
    val normalizedVersion = config.latestVersion.trim().removePrefix("v").removePrefix("V").trim()
    val data = jObj {
        put("enabled", config.enabled)
        put("latest_version", normalizedVersion)
        put("version", normalizedVersion)
        put("apk_url", config.apkUrl.trim())
        put("notes", config.notes.trim())
        put("force", config.force)
        put("updated_at", jsNowMillis().toLong())
    }
    return WebFirebaseBridge.setDoc("config", "app_update", data, merge = true, writeAllDbs = true)
}

actual suspend fun getAdminStatusFromFirestore(email: String): AdminStatus {
    if (email.isBlank()) return AdminStatus.NOT_REQUESTED
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail == PRIMARY_ADMIN_EMAIL) return AdminStatus.APPROVED

    fun parseStatus(d: JValue.JObj): AdminStatus {
        val st = d.optStr("status").trim().uppercase()
        val enabled = d.optBool("enabled", true)
        return when {
            st == "APPROVED" || (st.isBlank() && enabled) -> AdminStatus.APPROVED
            st == "DECLINED" -> AdminStatus.DECLINED
            st == "PENDING" -> AdminStatus.PENDING
            else -> AdminStatus.APPROVED
        }
    }

    WebFirebaseBridge.getDoc("admins", cleanEmail, tryAllDbs = true)?.let { return parseStatus(it) }
    val queried = WebFirebaseBridge.queryEquals("admins", "email", cleanEmail, mergeAllDbs = true)
    queried.firstOrNull()?.let { return parseStatus(it.data) }
    return AdminStatus.NOT_REQUESTED
}

actual suspend fun submitAdminRequestToFirestore(
    email: String,
    name: String,
    photoUrl: String?,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail == PRIMARY_ADMIN_EMAIL) {
        return saveAdminInFirestore(cleanEmail, name, photoUrl)
    }
    val now = jsNowMillis().toLong()
    val data = jObj {
        put("email", cleanEmail)
        put("name", name)
        put("photo_url", photoUrl ?: "")
        put("role", "ADMIN")
        put("status", "PENDING")
        put("requested_at", now)
        put("updated_at", now)
    }
    WebFirebaseBridge.setDoc("admins", cleanEmail, data, merge = true, writeAllDbs = true)
    return Result.success(Unit)
}

actual suspend fun fetchPendingAdminRequestsFromFirestore(): List<AdminRequestItem> {
    val currentAuthEmail = WebFirebaseBridge.currentEmail()
    val (_, docs) = WebFirebaseBridge.getCollection("admins", mergeAllDbs = true)
    val list = mutableListOf<AdminRequestItem>()
    for (doc in docs) {
        val em = doc.data.optStr("email").ifBlank { doc.id }.trim().lowercase()
        val st = doc.data.optStr("status").trim().uppercase()
        if (em.isBlank() || em == currentAuthEmail || em == PRIMARY_ADMIN_EMAIL || st == "APPROVED" || st == "DECLINED") {
            continue
        }
        if (st == "PENDING" && list.none { it.email == em }) {
            list.add(
                AdminRequestItem(
                    email = em,
                    name = doc.data.optStr("name").ifBlank { em.substringBefore("@") },
                    photoUrl = doc.data.optNullableStr("photo_url"),
                    status = "PENDING",
                    requestedAt = doc.data.optLong("requested_at", jsNowMillis().toLong()),
                )
            )
        }
    }
    return list
}

actual suspend fun updateAdminRequestStatusInFirestore(
    email: String,
    newStatus: String,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val data = jObj {
        put("status", newStatus.trim().uppercase())
        put("updated_at", jsNowMillis().toLong())
    }
    WebFirebaseBridge.setDoc("admins", cleanEmail, data, merge = true, writeAllDbs = true)
    return Result.success(Unit)
}

// ─── Teachers Directory ──────────────────────────────────────────────────────

actual suspend fun fetchLoggedInTeachersFromFirestore(): List<TeacherDirectoryItem> {
    val teacherMap = linkedMapOf<String, TeacherDirectoryItem>()

    fun mergeIntoMap(email: String, incoming: TeacherDirectoryItem) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return
        val existing = teacherMap[cleanEmail]
        if (existing == null) {
            teacherMap[cleanEmail] = incoming.copy(email = cleanEmail)
        } else {
            val mergedPhoto = when {
                !incoming.photoUrl.isNullOrBlank() && incoming.photoUrl.startsWith("data:image", ignoreCase = true) -> incoming.photoUrl
                !existing.photoUrl.isNullOrBlank() && existing.photoUrl.startsWith("data:image", ignoreCase = true) -> existing.photoUrl
                else -> incoming.photoUrl?.ifBlank { null } ?: existing.photoUrl
            }
            teacherMap[cleanEmail] = existing.copy(
                name = incoming.name.ifBlank { existing.name },
                photoUrl = mergedPhoto,
                phone = incoming.phone.ifBlank { existing.phone },
                contactEmail = incoming.contactEmail.ifBlank { existing.contactEmail },
                department = incoming.department.ifBlank { existing.department },
                shift = incoming.shift.ifBlank { existing.shift },
                bloodGroup = incoming.bloodGroup.ifBlank { existing.bloodGroup },
                gender = incoming.gender.ifBlank { existing.gender },
                address = incoming.address.ifBlank { existing.address },
                permanentAddress = incoming.permanentAddress.ifBlank { existing.permanentAddress },
                registeredAt = maxOf(existing.registeredAt, incoming.registeredAt),
                updatedAt = maxOf(existing.updatedAt, incoming.updatedAt),
            )
        }
    }

    fun itemFromDoc(doc: WebFirestoreDoc): TeacherDirectoryItem? {
        val d = doc.data
        val em = d.optStr("email").ifBlank { doc.id }.trim().lowercase()
        if (em.isBlank()) return null
        return TeacherDirectoryItem(
            email = em,
            name = d.optStr("name").trim(),
            photoUrl = d.optNullableStr("photo_url"),
            phone = d.optStr("phone").trim(),
            contactEmail = d.optStr("contact_email").trim(),
            department = d.optStr("department").trim(),
            shift = d.optStr("shift").trim(),
            bloodGroup = d.optStr("blood_group").trim(),
            gender = d.optStr("gender").trim(),
            address = d.optStr("address").trim(),
            permanentAddress = d.optStr("permanent_address").trim(),
            registeredAt = d.optLong("registered_at", 0L),
            updatedAt = d.optLong("updated_at", 0L),
        )
    }

    val (_, teacherDocs) = WebFirebaseBridge.getCollection("teachers", mergeAllDbs = true)
    teacherDocs.forEach { itemFromDoc(it)?.let { item -> mergeIntoMap(item.email, item) } }

    val userTeacherDocs = WebFirebaseBridge.queryEquals("users", "category", "TEACHER", mergeAllDbs = true)
    userTeacherDocs.forEach { itemFromDoc(it)?.let { item -> mergeIntoMap(item.email, item) } }

    for (teacherEmail in teacherMap.keys.toList()) {
        val profileDoc = WebFirebaseBridge.getDoc("students", teacherEmail, tryAllDbs = true)
        if (profileDoc != null) {
            val roleInProfile = profileDoc.optStr("role").trim().uppercase()
            if (roleInProfile.isBlank() || roleInProfile == "TEACHER") {
                itemFromDoc(WebFirestoreDoc(teacherEmail, profileDoc))?.let { mergeIntoMap(teacherEmail, it) }
            }
        }
    }

    return teacherMap.values
        .map { item ->
            item.copy(
                name = item.name.ifBlank { item.email.substringBefore("@") },
                contactEmail = item.contactEmail.ifBlank { item.email },
            )
        }
        .sortedWith(
            compareByDescending<TeacherDirectoryItem> { maxOf(it.updatedAt, it.registeredAt) }
                .thenBy { it.name.lowercase() }
        )
}

actual suspend fun deleteTeacherFromDatabaseInFirestore(email: String): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    WebFirebaseBridge.deleteDoc("teachers", cleanEmail, writeAllDbs = true)
    val matching = WebFirebaseBridge.queryEquals("teachers", "email", cleanEmail, mergeAllDbs = true)
    matching.forEach { WebFirebaseBridge.deleteDoc("teachers", it.id, writeAllDbs = true) }

    val userDoc = WebFirebaseBridge.getDoc("users", cleanEmail, tryAllDbs = true)
    if (userDoc != null) {
        val cat = userDoc.optStr("category").trim().uppercase()
        if (cat.isBlank() || cat == "TEACHER") {
            WebFirebaseBridge.deleteDoc("users", cleanEmail, writeAllDbs = true)
        }
    }

    val studentDoc = WebFirebaseBridge.getDoc("students", cleanEmail, tryAllDbs = true)
    if (studentDoc != null) {
        val roleStr = studentDoc.optStr("role").trim().uppercase()
        if (roleStr.isBlank() || roleStr == "TEACHER") {
            WebFirebaseBridge.deleteDoc("students", cleanEmail, writeAllDbs = true)
        }
    }
    return Result.success(Unit)
}

// ─── BTEB Books Library ──────────────────────────────────────────────────────

private const val BOOKS_CACHE_KEY = "klass_books_cache_json"
private const val BTEB_SEED_FLAG_KEY = "klass_bteb_2022_v1_seeded"
private val booksSeedMutex = Mutex()
private var booksSeedVerifiedThisSession = false

private val booksSortComparator = compareByDescending<LibraryBook> {
    maxOf(it.updatedAt, it.createdAt)
}.thenBy {
    it.subjectCode.trim()
}.thenBy {
    it.title.lowercase()
}

private fun bookToJObj(book: LibraryBook): JValue.JObj = jObj {
    put("id", book.id)
    put("title", book.title)
    put("subject_code", book.subjectCode)
    put(
        "department_semesters",
        jObj {
            for ((dept, sems) in book.departmentSemesters) {
                put(dept, jStrArr(sems))
            }
        }
    )
    put("theory_classes_per_week", book.theoryClassesPerWeek)
    put("practical_classes_per_week", book.practicalClassesPerWeek)
    put("credit", book.credit)
    put("created_at", book.createdAt)
    put("updated_at", book.updatedAt)
}

private fun bookFromJObj(idFallback: String, obj: JValue.JObj): LibraryBook? {
    val id = obj.optStr("id").ifBlank { idFallback }.trim()
    if (id.isBlank()) return null
    val deptSemMap = linkedMapOf<String, List<String>>()
    val deptObj = obj.optObj("department_semesters")
    if (deptObj != null) {
        for ((deptKey, rawArr) in deptObj.entries) {
            val sems = rawArr.asArr()?.items
                ?.mapNotNull { it.asStr().trim().takeIf { s -> s.isNotBlank() } }
                ?: emptyList()
            if (sems.isNotEmpty()) {
                deptSemMap[deptKey.trim()] = sems
            }
        }
    }
    return LibraryBook(
        id = id,
        title = obj.optStr("title").trim(),
        subjectCode = obj.optStr("subject_code").trim(),
        departmentSemesters = deptSemMap,
        theoryClassesPerWeek = obj.optInt("theory_classes_per_week", 0),
        practicalClassesPerWeek = obj.optInt("practical_classes_per_week", 0),
        credit = obj.optStr("credit").trim(),
        createdAt = obj.optLong("created_at", 0L),
        updatedAt = obj.optLong("updated_at", 0L),
    )
}

private fun readLocalBooksCache(): List<LibraryBook> {
    val raw = jsStorageGet(BOOKS_CACHE_KEY) ?: return emptyList()
    val arr = WebJson.parseArr(raw) ?: return emptyList()
    return arr.items.mapNotNull { item ->
        val obj = item.asObj() ?: return@mapNotNull null
        bookFromJObj("", obj)
    }.sortedWith(booksSortComparator)
}

private fun writeLocalBooksCache(books: List<LibraryBook>) {
    jsStorageSet(BOOKS_CACHE_KEY, WebJson.stringify(jObjArr(books.map(::bookToJObj))))
}

private fun mergeBtebCatalogIntoLocalCache() {
    val existing = readLocalBooksCache().associateBy { it.id }.toMutableMap()
    var changed = false
    for (catalogBook in BtebBooksCatalog.books) {
        if (!existing.containsKey(catalogBook.id)) {
            existing[catalogBook.id] = catalogBook
            changed = true
        }
    }
    if (changed || existing.isEmpty()) {
        writeLocalBooksCache(existing.values.sortedWith(booksSortComparator))
    }
}

actual suspend fun seedBtebLibraryBooksInFirestore(forceReseed: Boolean): Result<Int> {
    return booksSeedMutex.withLock {
        val isAuthenticated = WebFirebaseBridge.isSignedIn()
        if (!isAuthenticated) {
            mergeBtebCatalogIntoLocalCache()
            return@withLock Result.failure(Exception("Firebase user not signed in"))
        }

        val catalogBooks = BtebBooksCatalog.books
        val markerDoc = WebFirebaseBridge.getDoc("config", BtebBooksCatalog.SEED_VERSION_DOC, tryAllDbs = true)
        val markerCount = markerDoc?.optLong("count", 0L) ?: 0L
        val alreadySeeded = markerDoc != null &&
            markerDoc.optBool("seeded", false) &&
            markerCount >= catalogBooks.size

        if (!forceReseed && alreadySeeded) {
            mergeBtebCatalogIntoLocalCache()
            jsStorageSet(BTEB_SEED_FLAG_KEY, "true")
            booksSeedVerifiedThisSession = true
            return@withLock Result.success(catalogBooks.size)
        }

        val booksToWrite = if (forceReseed) {
            catalogBooks
        } else {
            val (_, existingDocs) = WebFirebaseBridge.getCollection("books", mergeAllDbs = false)
            val existingIds = existingDocs.map { it.id }.toSet()
            catalogBooks.filter { it.id !in existingIds }
        }

        val batchRes = if (booksToWrite.isNotEmpty()) {
            WebFirebaseBridge.batchSetDocs(
                collection = "books",
                docs = booksToWrite.map { it.id to bookToJObj(it) },
                merge = forceReseed,
                writeAllDbs = false,
            )
        } else {
            Result.success(Unit)
        }

        if (batchRes.isSuccess) {
            val markerData = jObj {
                put("seeded", true)
                put("count", catalogBooks.size)
                put("source", "bteb.gov.bd")
                put("probidhan", "2022")
                put("seeded_at", jsNowMillis().toLong())
            }
            if (markerDoc == null || forceReseed || markerCount < catalogBooks.size) {
                WebFirebaseBridge.setDoc(
                    "config",
                    BtebBooksCatalog.SEED_VERSION_DOC,
                    markerData,
                    merge = markerDoc != null,
                    writeAllDbs = false,
                )
            }
            mergeBtebCatalogIntoLocalCache()
            jsStorageSet(BTEB_SEED_FLAG_KEY, "true")
            booksSeedVerifiedThisSession = true
            Result.success(catalogBooks.size)
        } else {
            mergeBtebCatalogIntoLocalCache()
            Result.failure(batchRes.exceptionOrNull() ?: Exception("Failed to seed BTEB books in Firestore"))
        }
    }
}

actual suspend fun fetchLibraryBooksFromFirestore(): List<LibraryBook> {
    val localSeedFlag = jsStorageGet(BTEB_SEED_FLAG_KEY) == "true"
    val isAuthenticated = WebFirebaseBridge.isSignedIn()
    if (isAuthenticated && (!localSeedFlag || !booksSeedVerifiedThisSession)) {
        seedBtebLibraryBooksInFirestore(forceReseed = false)
    }

    val markerDoc = WebFirebaseBridge.getDoc("config", BtebBooksCatalog.SEED_VERSION_DOC, tryAllDbs = true)
    val serverSeedComplete = markerDoc != null &&
        markerDoc.optBool("seeded", false) &&
        markerDoc.optLong("count", 0L) >= BtebBooksCatalog.books.size

    val (fetchedAny, remoteDocs) = WebFirebaseBridge.getCollection("books", mergeAllDbs = true)
    if (fetchedAny) {
        val bookMap = linkedMapOf<String, LibraryBook>()
        for (doc in remoteDocs) {
            val incoming = bookFromJObj(doc.id, doc.data) ?: continue
            val existing = bookMap[incoming.id]
            if (existing == null || incoming.updatedAt >= existing.updatedAt) {
                bookMap[incoming.id] = incoming
            }
        }
        if (!serverSeedComplete || bookMap.isEmpty()) {
            for (catalogBook in BtebBooksCatalog.books) {
                if (!bookMap.containsKey(catalogBook.id)) {
                    bookMap[catalogBook.id] = catalogBook
                }
            }
        }
        val sorted = bookMap.values.sortedWith(booksSortComparator)
        writeLocalBooksCache(sorted)
        return sorted
    }

    val cached = readLocalBooksCache()
    if (cached.size >= BtebBooksCatalog.books.size) return cached
    mergeBtebCatalogIntoLocalCache()
    return readLocalBooksCache()
}

actual suspend fun saveLibraryBookInFirestore(book: LibraryBook): Result<Unit> {
    val now = jsNowMillis().toLong()
    val safeCode = book.subjectCode.trim().replace(Regex("[^A-Za-z0-9_-]"), "")
    val bookId = book.id.trim().ifBlank {
        "book_${now}_${safeCode.ifBlank { (1000..9999).random().toString() }}"
    }
    val createdAt = if (book.createdAt > 0L) book.createdAt else now
    val cleanBook = book.copy(
        id = bookId,
        title = book.title.trim(),
        subjectCode = book.subjectCode.trim(),
        credit = book.credit.trim(),
        createdAt = createdAt,
        updatedAt = now,
    )

    val currentCache = readLocalBooksCache().toMutableList()
    val existingIdx = currentCache.indexOfFirst { it.id == bookId }
    if (existingIdx >= 0) currentCache[existingIdx] = cleanBook else currentCache.add(0, cleanBook)
    writeLocalBooksCache(currentCache.sortedWith(booksSortComparator))

    return WebFirebaseBridge.setDoc("books", bookId, bookToJObj(cleanBook), merge = true, writeAllDbs = true)
}

actual suspend fun deleteLibraryBookFromFirestore(bookId: String): Result<Unit> {
    val cleanId = bookId.trim()
    if (cleanId.isBlank()) return Result.failure(Exception("Invalid book ID"))
    writeLocalBooksCache(readLocalBooksCache().filterNot { it.id == cleanId })
    return WebFirebaseBridge.deleteDoc("books", cleanId, writeAllDbs = true)
}

actual suspend fun resolveRegisteredRoleFromFirestore(email: String): UserRole? {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return null
    if (cleanEmail == PRIMARY_ADMIN_EMAIL) return UserRole.ADMIN

    WebFirebaseBridge.getDoc("admins", cleanEmail, tryAllDbs = true)?.let { d ->
        if (isDocMatchingEmail(cleanEmail, d, cleanEmail) && d.optStr("status").trim().uppercase() != "DECLINED") {
            return UserRole.ADMIN
        }
    }

    WebFirebaseBridge.getDoc("users", cleanEmail, tryAllDbs = true)?.let { d ->
        UserRole.fromRaw(d.optStr("category"))?.let { return it }
    }

    WebFirebaseBridge.getDoc("teachers", cleanEmail, tryAllDbs = true)?.let {
        return UserRole.TEACHER
    }

    WebFirebaseBridge.getDoc("students", cleanEmail, tryAllDbs = true)?.let { d ->
        val docRole = d.optStr("role").trim().uppercase()
        if (docRole.isBlank() || docRole == "STUDENT") {
            return UserRole.STUDENT
        }
    }

    return null
}
