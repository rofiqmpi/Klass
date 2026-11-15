package com.mpi.klass

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mpi.klass.theme.getAppContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "AdminAuth"

private fun getCustomDatabaseId(): String? {
    val ctx = getAppContext() ?: return null
    val resId = ctx.resources.getIdentifier("firestore_database_id", "string", ctx.packageName)
    if (resId == 0) return null
    val dbName = ctx.getString(resId).trim()
    return if (dbName.isEmpty() || dbName == "(default)") null else dbName
}

private fun getFirestoreInstances(): List<Pair<FirebaseFirestore, String>> {
    val list = mutableListOf<Pair<FirebaseFirestore, String>>()
    val customDbId = getCustomDatabaseId()
    if (customDbId != null) {
        try {
            list.add(FirebaseFirestore.getInstance(customDbId) to customDbId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get custom db instance ($customDbId): ${e.message}")
        }
    }
    try {
        list.add(FirebaseFirestore.getInstance() to "(default)")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get default db instance: ${e.message}")
    }
    return list
}

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
}

actual fun saveLocalProfile(email: String, name: String, photoUrl: String?, provider: String, category: String) {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return
    val cleanEmail = email.trim().lowercase()
    val editor = prefs.edit()
        .putString("${cleanEmail}_provider", provider)
        .putString("${cleanEmail}_category", category)
        .putLong("${cleanEmail}_time", System.currentTimeMillis())
    if (name.isNotBlank()) {
        editor.putString("${cleanEmail}_name", name)
    }
    if (!photoUrl.isNullOrBlank()) {
        editor.putString("${cleanEmail}_photo", photoUrl)
    }
    editor.apply()
}

actual fun fetchLocalProfile(email: String): UserProfile? {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return null
    val cleanEmail = email.trim().lowercase()
    val name = prefs.getString("${cleanEmail}_name", null) ?: return null
    val photoUrl = prefs.getString("${cleanEmail}_photo", "")?.ifBlank { null }
    val provider = prefs.getString("${cleanEmail}_provider", "") ?: ""
    val category = prefs.getString("${cleanEmail}_category", "STUDENT") ?: "STUDENT"
    val time = prefs.getLong("${cleanEmail}_time", 0L)
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
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return null
    val cleanEmail = email.trim().lowercase()
    val localName = prefs.getString("${cleanEmail}_student_name", "")?.ifBlank { prefs.getString("${cleanEmail}_name", "") } ?: ""
    val localPhoto = prefs.getString("${cleanEmail}_student_photo", "")?.ifBlank { prefs.getString("${cleanEmail}_photo", "")?.ifBlank { null } }
    val localComplete = prefs.getBoolean("${cleanEmail}_student_complete", false)
    val semester = prefs.getString("${cleanEmail}_student_semester", "") ?: ""
    val shift = prefs.getString("${cleanEmail}_student_shift", "") ?: ""
    val dept = prefs.getString("${cleanEmail}_student_department", "") ?: ""
    val section = prefs.getString("${cleanEmail}_student_section", "") ?: ""
    val phone = prefs.getString("${cleanEmail}_student_phone", "") ?: ""
    val blood = prefs.getString("${cleanEmail}_student_blood", "") ?: ""
    val address = prefs.getString("${cleanEmail}_student_address", "") ?: ""
    val roll = prefs.getString("${cleanEmail}_student_roll", "") ?: ""
    val father = prefs.getString("${cleanEmail}_student_father", "") ?: ""
    val mother = prefs.getString("${cleanEmail}_student_mother", "") ?: ""
    val dob = prefs.getString("${cleanEmail}_student_dob", "") ?: ""
    val religion = prefs.getString("${cleanEmail}_student_religion", "") ?: ""
    val gender = prefs.getString("${cleanEmail}_student_gender", "") ?: ""
    val marital = prefs.getString("${cleanEmail}_student_marital", "") ?: ""
    val contactEmail = prefs.getString("${cleanEmail}_student_contact_email", "") ?: ""
    val permanentAddress = prefs.getString("${cleanEmail}_student_permanent_address", "") ?: ""
    val reg = prefs.getString("${cleanEmail}_student_registration", "") ?: ""

    if (!localComplete && localName.isBlank() && localPhoto.isNullOrBlank() && phone.isBlank() && dept.isBlank() && roll.isBlank()) {
        return null
    }
    return StudentProfile(
        email = cleanEmail,
        name = localName,
        photoUrl = localPhoto,
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
        permanentAddress = permanentAddress,
        registrationNumber = reg,
        profileSetupComplete = localComplete,
    )
}

actual fun deleteLocalProfile(email: String) {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return
    val cleanEmail = email.trim().lowercase()
    prefs.edit()
        .remove("${cleanEmail}_name")
        .remove("${cleanEmail}_photo")
        .remove("${cleanEmail}_provider")
        .remove("${cleanEmail}_category")
        .remove("${cleanEmail}_time")
        .remove("${cleanEmail}_student_name")
        .remove("${cleanEmail}_student_photo")
        .remove("${cleanEmail}_student_semester")
        .remove("${cleanEmail}_student_shift")
        .remove("${cleanEmail}_student_department")
        .remove("${cleanEmail}_student_section")
        .remove("${cleanEmail}_student_phone")
        .remove("${cleanEmail}_student_blood")
        .remove("${cleanEmail}_student_address")
        .remove("${cleanEmail}_student_roll")
        .remove("${cleanEmail}_student_father")
        .remove("${cleanEmail}_student_mother")
        .remove("${cleanEmail}_student_dob")
        .remove("${cleanEmail}_student_religion")
        .remove("${cleanEmail}_student_gender")
        .remove("${cleanEmail}_student_marital")
        .remove("${cleanEmail}_student_contact_email")
        .remove("${cleanEmail}_student_permanent_address")
        .remove("${cleanEmail}_student_registration")
        .remove("${cleanEmail}_student_complete")
        .remove("${cleanEmail}_student_time")
        .apply()
}

actual fun updateLocalCategory(email: String, category: String) {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return
    val cleanEmail = email.trim().lowercase()
    prefs.edit()
        .putString("${cleanEmail}_category", category)
        .apply()
}

private fun isDocMatchingEmail(doc: DocumentSnapshot, cleanEmail: String): Boolean {
    val docId = doc.id.trim().lowercase()
    val enabled = doc.getBoolean("enabled") ?: true
    if (!enabled) {
        Log.w(TAG, "Document ${doc.id} has enabled == false, skipping.")
        return false
    }

    if (docId == cleanEmail) {
        return true
    }

    val data = doc.data ?: return false
    val emailFields = listOf("email", "Email", "mail", "admin_email", "adminEmail", "user_email", "userEmail")
    for (f in emailFields) {
        val v = (data[f] as? String)?.trim()?.lowercase()
        if (v == cleanEmail) {
            return true
        }
    }

    for ((key, value) in data) {
        if (value is String && value.trim().lowercase() == cleanEmail) {
            return true
        }
        if (value is List<*>) {
            for (item in value) {
                if (item is String && item.trim().lowercase() == cleanEmail) {
                    return true
                }
            }
        }
    }

    return false
}

private suspend fun checkDatabaseForAdmin(db: FirebaseFirestore, dbLabel: String, cleanEmail: String): Boolean {
    val collectionsToCheck = listOf("admins", "admin")
    for (colName in collectionsToCheck) {
        try {
            Log.d(TAG, "[$dbLabel] Checking collection '$colName' for email: $cleanEmail")
            val colRef = db.collection(colName)

            // 1. Direct document lookup by email
            try {
                val directDoc = colRef.document(cleanEmail).get().awaitTask()
                if (directDoc.exists() && isDocMatchingEmail(directDoc, cleanEmail)) {
                    Log.d(TAG, "[$dbLabel] SUCCESS: Authorized admin via direct document $colName/$cleanEmail")
                    return true
                }
            } catch (e: Exception) {
                Log.d(TAG, "[$dbLabel] Direct lookup exception for $cleanEmail: ${e.message}")
            }

            // 2. Query where email == cleanEmail
            try {
                val querySnap = colRef.whereEqualTo("email", cleanEmail).get().awaitTask()
                for (doc in querySnap.documents) {
                    if (isDocMatchingEmail(doc, cleanEmail)) {
                        Log.d(TAG, "[$dbLabel] SUCCESS: Authorized admin via query where email == $cleanEmail in $colName: ${doc.id}")
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "[$dbLabel] whereEqualTo query exception: ${e.message}")
            }

            // 3. Collection scan
            try {
                val allDocs = colRef.get().awaitTask()
                Log.d(TAG, "[$dbLabel] Collection '$colName' contains ${allDocs.size()} docs")
                for (doc in allDocs.documents) {
                    if (isDocMatchingEmail(doc, cleanEmail)) {
                        Log.d(TAG, "[$dbLabel] SUCCESS: Authorized admin via collection scan in $colName: ${doc.id}")
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "[$dbLabel] Collection scan on '$colName' failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$dbLabel] Error on collection '$colName': ${e.message}")
        }
    }
    return false
}

actual suspend fun checkIsAuthorizedAdminEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val cleanEmail = email.trim().lowercase()
    val result = withTimeoutOrNull(8.seconds) {
        val instances = getFirestoreInstances()
        for ((db, dbName) in instances) {
            try {
                if (checkDatabaseForAdmin(db, dbName, cleanEmail)) {
                    return@withTimeoutOrNull true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking db $dbName: ${e.message}")
            }
        }
        false
    } ?: false
    return result
}

actual suspend fun saveAdminInFirestore(email: String, name: String, photoUrl: String?): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = System.currentTimeMillis()
    val data = mutableMapOf<String, Any>(
        "email" to cleanEmail,
        "name" to name,
        "role" to "ADMIN",
        "status" to "APPROVED",
        "last_login_at" to now,
        "updated_at" to now,
    )
    if (!photoUrl.isNullOrBlank()) {
        data["photo_url"] = photoUrl
    }

    withTimeoutOrNull(4.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                db.collection("admins").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
    }

    return Result.success(Unit)
}

actual suspend fun saveTeacherInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = System.currentTimeMillis()
    val data = mutableMapOf<String, Any>(
        "email" to cleanEmail,
        "name" to name,
        "role" to "TEACHER",
        "provider" to provider,
        "registered_at" to now,
        "updated_at" to now,
    )
    if (!photoUrl.isNullOrBlank()) {
        data["photo_url"] = photoUrl
    }

    withTimeoutOrNull(4.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                db.collection("teachers").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
    }

    return Result.success(Unit)
}

actual suspend fun saveStudentInFirestore(
    email: String,
    name: String,
    photoUrl: String?,
    provider: String,
): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = System.currentTimeMillis()
    val data = mutableMapOf<String, Any>(
        "email" to cleanEmail,
        "name" to name,
        "role" to "STUDENT",
        "provider" to provider,
        "registered_at" to now,
        "updated_at" to now,
    )
    if (!photoUrl.isNullOrBlank()) {
        data["photo_url"] = photoUrl
    }

    withTimeoutOrNull(4.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                db.collection("students").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
    }

    return Result.success(Unit)
}

actual suspend fun saveStudentProfileDataInFirestore(profile: StudentProfile): Result<Unit> {
    if (profile.email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = profile.email.trim().lowercase()
    val ctx = getAppContext()
    val prefs = ctx?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE)
    val now = System.currentTimeMillis()
    val category = prefs?.getString("${cleanEmail}_category", "STUDENT")?.trim()?.uppercase()?.ifBlank { "STUDENT" } ?: "STUDENT"

    // 1. Save locally in SharedPreferences (never wipe existing photo or category with blank)
    val editor = prefs?.edit()
        ?.putString("${cleanEmail}_student_semester", profile.semester)
        ?.putString("${cleanEmail}_student_shift", profile.shift)
        ?.putString("${cleanEmail}_student_department", profile.department)
        ?.putString("${cleanEmail}_student_section", profile.section)
        ?.putString("${cleanEmail}_student_phone", profile.phone)
        ?.putString("${cleanEmail}_student_blood", profile.bloodGroup)
        ?.putString("${cleanEmail}_student_address", profile.address)
        ?.putString("${cleanEmail}_student_roll", profile.rollNumber)
        ?.putString("${cleanEmail}_student_father", profile.fatherName)
        ?.putString("${cleanEmail}_student_mother", profile.motherName)
        ?.putString("${cleanEmail}_student_dob", profile.dob)
        ?.putString("${cleanEmail}_student_religion", profile.religion)
        ?.putString("${cleanEmail}_student_gender", profile.gender)
        ?.putString("${cleanEmail}_student_marital", profile.maritalStatus)
        ?.putString("${cleanEmail}_student_contact_email", profile.contactEmail)
        ?.putString("${cleanEmail}_student_permanent_address", profile.permanentAddress)
        ?.putString("${cleanEmail}_student_registration", profile.registrationNumber)
        ?.putBoolean("${cleanEmail}_student_complete", profile.profileSetupComplete)
        ?.putLong("${cleanEmail}_student_time", now)
    if (prefs?.contains("${cleanEmail}_category") != true) {
        editor?.putString("${cleanEmail}_category", category)
    }
    if (profile.name.isNotBlank()) {
        editor?.putString("${cleanEmail}_name", profile.name)
        editor?.putString("${cleanEmail}_student_name", profile.name)
    }
    if (!profile.photoUrl.isNullOrBlank()) {
        editor?.putString("${cleanEmail}_photo", profile.photoUrl)
        editor?.putString("${cleanEmail}_student_photo", profile.photoUrl)
    }
    editor?.apply()

    // 2. Save in Firestore collections ('students', 'users', and role collection if TEACHER/ADMIN)
    val data = mutableMapOf<String, Any>(
        "email" to cleanEmail,
        "name" to profile.name,
        "role" to category,
        "semester" to profile.semester,
        "shift" to profile.shift,
        "department" to profile.department,
        "section" to profile.section,
        "phone" to profile.phone,
        "blood_group" to profile.bloodGroup,
        "address" to profile.address,
        "roll_number" to profile.rollNumber,
        "father_name" to profile.fatherName,
        "mother_name" to profile.motherName,
        "dob" to profile.dob,
        "religion" to profile.religion,
        "gender" to profile.gender,
        "marital_status" to profile.maritalStatus,
        "contact_email" to profile.contactEmail,
        "permanent_address" to profile.permanentAddress,
        "registration_number" to profile.registrationNumber,
        "profile_setup_complete" to profile.profileSetupComplete,
        "updated_at" to now,
    )
    if (!profile.photoUrl.isNullOrBlank()) {
        data["photo_url"] = profile.photoUrl
    }

    val userPatch = mutableMapOf<String, Any>(
        "email" to cleanEmail,
        "name" to profile.name,
        "phone" to profile.phone,
        "department" to profile.department,
        "address" to profile.address,
        "updated_at" to now,
    )
    if (!profile.photoUrl.isNullOrBlank()) {
        userPatch["photo_url"] = profile.photoUrl
    }

    val teacherPatch = userPatch.toMutableMap().apply {
        put("role", "TEACHER")
        put("shift", profile.shift)
        put("blood_group", profile.bloodGroup)
        put("dob", profile.dob)
        put("gender", profile.gender)
        put("religion", profile.religion)
        put("marital_status", profile.maritalStatus)
        put("contact_email", profile.contactEmail)
        put("permanent_address", profile.permanentAddress)
    }

    val instances = getFirestoreInstances()
    if (instances.isEmpty()) {
        return Result.failure(Exception("Firestore not initialized"))
    }

    var firestoreSaved = false
    var lastError: Exception? = null

    withTimeoutOrNull(10.seconds) {
        for ((db, dbLabel) in instances) {
            try {
                db.collection("students").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
                db.collection("users").document(cleanEmail).set(userPatch, SetOptions.merge()).awaitTask()
                when (category) {
                    "TEACHER" -> db.collection("teachers").document(cleanEmail).set(teacherPatch, SetOptions.merge()).awaitTask()
                    "ADMIN" -> db.collection("admins").document(cleanEmail).set(userPatch, SetOptions.merge()).awaitTask()
                }
                firestoreSaved = true
            } catch (e: Exception) {
                lastError = e
                Log.e("StudentProfile", "[$dbLabel] Failed saving profile to firestore: ${e.message}")
            }
        }
    }

    if (firestoreSaved) {
        return Result.success(Unit)
    }
    lastError?.let { err ->
        return Result.failure(err)
    }
    // Timed out while waiting for server ack: check if device is offline (queued locally) vs online failure
    return if (isNetworkAvailable()) {
        Result.failure(Exception("Firestore save timed out"))
    } else {
        Result.success(Unit)
    }
}

actual suspend fun updateStudentAddressInFirestore(email: String, address: String): Result<Unit> {
    if (email.isBlank() || address.isBlank()) return Result.failure(Exception("Invalid address"))
    val cleanEmail = email.trim().lowercase()
    val cleanAddress = address.trim()
    val now = System.currentTimeMillis()
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE)
    prefs?.edit()
        ?.putString("${cleanEmail}_student_address", cleanAddress)
        ?.putLong("${cleanEmail}_student_time", now)
        ?.apply()

    val category = prefs?.getString("${cleanEmail}_category", "")?.trim()?.uppercase() ?: ""
    val patch = mapOf<String, Any>(
        "address" to cleanAddress,
        "updated_at" to now,
    )
    withTimeoutOrNull(6.seconds) {
        for ((db, dbLabel) in getFirestoreInstances()) {
            try {
                db.collection("students").document(cleanEmail).set(patch, SetOptions.merge()).awaitTask()
                db.collection("users").document(cleanEmail).set(patch, SetOptions.merge()).awaitTask()
                when (category) {
                    "TEACHER" -> db.collection("teachers").document(cleanEmail).set(patch, SetOptions.merge()).awaitTask()
                    "ADMIN" -> db.collection("admins").document(cleanEmail).set(patch, SetOptions.merge()).awaitTask()
                }
            } catch (e: Exception) {
                Log.w("StudentProfile", "[$dbLabel] Address sync failed: ${e.message}")
            }
        }
    }
    return Result.success(Unit)
}

actual suspend fun fetchStudentProfileDataFromFirestore(email: String): StudentProfile? {
    if (email.isBlank()) return null
    val cleanEmail = email.trim().lowercase()
    val ctx = getAppContext()
    val prefs = ctx?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE)

    // 1. Read local cache (never return early before checking Firestore, or returning users miss cloud sync)
    val localTime = prefs?.getLong("${cleanEmail}_student_time", 0L) ?: 0L
    val localComplete = prefs?.getBoolean("${cleanEmail}_student_complete", false) ?: false
    val localName = prefs?.getString("${cleanEmail}_student_name", "")?.ifBlank { prefs.getString("${cleanEmail}_name", "") } ?: ""
    val localPhoto = prefs?.getString("${cleanEmail}_student_photo", "")?.ifBlank { prefs.getString("${cleanEmail}_photo", "")?.ifBlank { null } }
    val localProfile = StudentProfile(
        email = cleanEmail,
        name = localName,
        photoUrl = localPhoto,
        semester = prefs?.getString("${cleanEmail}_student_semester", "") ?: "",
        shift = prefs?.getString("${cleanEmail}_student_shift", "") ?: "",
        department = prefs?.getString("${cleanEmail}_student_department", "") ?: "",
        section = prefs?.getString("${cleanEmail}_student_section", "") ?: "",
        phone = prefs?.getString("${cleanEmail}_student_phone", "") ?: "",
        bloodGroup = prefs?.getString("${cleanEmail}_student_blood", "") ?: "",
        address = prefs?.getString("${cleanEmail}_student_address", "") ?: "",
        rollNumber = prefs?.getString("${cleanEmail}_student_roll", "") ?: "",
        fatherName = prefs?.getString("${cleanEmail}_student_father", "") ?: "",
        motherName = prefs?.getString("${cleanEmail}_student_mother", "") ?: "",
        dob = prefs?.getString("${cleanEmail}_student_dob", "") ?: "",
        religion = prefs?.getString("${cleanEmail}_student_religion", "") ?: "",
        gender = prefs?.getString("${cleanEmail}_student_gender", "") ?: "",
        maritalStatus = prefs?.getString("${cleanEmail}_student_marital", "") ?: "",
        contactEmail = prefs?.getString("${cleanEmail}_student_contact_email", "") ?: "",
        permanentAddress = prefs?.getString("${cleanEmail}_student_permanent_address", "") ?: "",
        registrationNumber = prefs?.getString("${cleanEmail}_student_registration", "") ?: "",
        profileSetupComplete = localComplete,
    )

    // 2. Always attempt to fetch latest document from Firestore 'students' collection and merge cleanly
    val remoteProfile = withTimeoutOrNull(6.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                val doc = db.collection("students").document(cleanEmail).get().awaitTask()
                if (doc.exists()) {
                    val d = doc.data ?: continue
                    val remoteUpdatedAt = (d["updated_at"] as? Number)?.toLong() ?: 0L
                    val remoteName = (d["name"] as? String) ?: ""
                    val remotePhoto = (d["photo_url"] as? String)?.ifBlank { null }
                    val remoteSemester = (d["semester"] as? String) ?: ""
                    val remoteShift = (d["shift"] as? String) ?: ""
                    val remoteDepartment = (d["department"] as? String) ?: ""
                    val remoteSection = (d["section"] as? String) ?: ""
                    val remotePhone = (d["phone"] as? String) ?: ""
                    val remoteBlood = (d["blood_group"] as? String) ?: ""
                    val remoteAddress = (d["address"] as? String) ?: ""
                    val remoteRoll = (d["roll_number"] as? String) ?: ""
                    val remoteFather = (d["father_name"] as? String) ?: ""
                    val remoteMother = (d["mother_name"] as? String) ?: ""
                    val remoteDob = (d["dob"] as? String) ?: ""
                    val remoteReligion = (d["religion"] as? String) ?: ""
                    val remoteGender = (d["gender"] as? String) ?: ""
                    val remoteMarital = (d["marital_status"] as? String) ?: ""
                    val remoteContactEmail = (d["contact_email"] as? String) ?: ""
                    val remotePermAddress = (d["permanent_address"] as? String) ?: ""
                    val remoteReg = (d["registration_number"] as? String) ?: ""
                    val remoteComplete = (d["profile_setup_complete"] as? Boolean) ?: false

                    val preferLocal = localTime > remoteUpdatedAt && localComplete
                    fun pick(remoteVal: String, localVal: String): String =
                        if (preferLocal) localVal.ifBlank { remoteVal } else remoteVal.ifBlank { localVal }

                    val mergedPhoto = when {
                        preferLocal && !localPhoto.isNullOrBlank() -> localPhoto
                        remotePhoto != null && remotePhoto.startsWith("data:image", ignoreCase = true) -> remotePhoto
                        localPhoto != null && localPhoto.startsWith("data:image", ignoreCase = true) -> localPhoto
                        else -> remotePhoto ?: localPhoto
                    }

                    val merged = StudentProfile(
                        email = (d["email"] as? String)?.ifBlank { cleanEmail } ?: cleanEmail,
                        name = pick(remoteName, localProfile.name),
                        photoUrl = mergedPhoto,
                        semester = pick(remoteSemester, localProfile.semester),
                        shift = pick(remoteShift, localProfile.shift),
                        department = pick(remoteDepartment, localProfile.department),
                        section = pick(remoteSection, localProfile.section),
                        phone = pick(remotePhone, localProfile.phone),
                        bloodGroup = pick(remoteBlood, localProfile.bloodGroup),
                        address = pick(remoteAddress, localProfile.address),
                        rollNumber = pick(remoteRoll, localProfile.rollNumber),
                        fatherName = pick(remoteFather, localProfile.fatherName),
                        motherName = pick(remoteMother, localProfile.motherName),
                        dob = pick(remoteDob, localProfile.dob),
                        religion = pick(remoteReligion, localProfile.religion),
                        gender = pick(remoteGender, localProfile.gender),
                        maritalStatus = pick(remoteMarital, localProfile.maritalStatus),
                        contactEmail = pick(remoteContactEmail, localProfile.contactEmail),
                        permanentAddress = pick(remotePermAddress, localProfile.permanentAddress),
                        registrationNumber = pick(remoteReg, localProfile.registrationNumber),
                        profileSetupComplete = remoteComplete || localComplete,
                    )

                    // Update local cache with merged authoritative data
                    val cacheEditor = prefs?.edit()
                        ?.putString("${cleanEmail}_student_semester", merged.semester)
                        ?.putString("${cleanEmail}_student_shift", merged.shift)
                        ?.putString("${cleanEmail}_student_department", merged.department)
                        ?.putString("${cleanEmail}_student_section", merged.section)
                        ?.putString("${cleanEmail}_student_phone", merged.phone)
                        ?.putString("${cleanEmail}_student_blood", merged.bloodGroup)
                        ?.putString("${cleanEmail}_student_address", merged.address)
                        ?.putString("${cleanEmail}_student_roll", merged.rollNumber)
                        ?.putString("${cleanEmail}_student_father", merged.fatherName)
                        ?.putString("${cleanEmail}_student_mother", merged.motherName)
                        ?.putString("${cleanEmail}_student_dob", merged.dob)
                        ?.putString("${cleanEmail}_student_religion", merged.religion)
                        ?.putString("${cleanEmail}_student_gender", merged.gender)
                        ?.putString("${cleanEmail}_student_marital", merged.maritalStatus)
                        ?.putString("${cleanEmail}_student_contact_email", merged.contactEmail)
                        ?.putString("${cleanEmail}_student_permanent_address", merged.permanentAddress)
                        ?.putString("${cleanEmail}_student_registration", merged.registrationNumber)
                        ?.putBoolean("${cleanEmail}_student_complete", merged.profileSetupComplete)
                        ?.putLong("${cleanEmail}_student_time", maxOf(localTime, remoteUpdatedAt))
                    if (merged.name.isNotBlank()) {
                        cacheEditor?.putString("${cleanEmail}_name", merged.name)
                        cacheEditor?.putString("${cleanEmail}_student_name", merged.name)
                    }
                    if (!merged.photoUrl.isNullOrBlank()) {
                        cacheEditor?.putString("${cleanEmail}_photo", merged.photoUrl)
                        cacheEditor?.putString("${cleanEmail}_student_photo", merged.photoUrl)
                    }
                    cacheEditor?.apply()
                    return@withTimeoutOrNull merged
                }
            } catch (e: Exception) {
                Log.w("StudentProfile", "Firestore profile fetch exception: ${e.message}")
            }
        }
        null
    }

    if (remoteProfile != null) return remoteProfile

    val hasAnyLocal = localComplete || localProfile.semester.isNotBlank() || localProfile.department.isNotBlank() ||
        localProfile.rollNumber.isNotBlank() || localProfile.fatherName.isNotBlank() ||
        localProfile.registrationNumber.isNotBlank() || localProfile.phone.isNotBlank() ||
        localProfile.address.isNotBlank() || !localPhoto.isNullOrBlank() || localName.isNotBlank()
    if (hasAnyLocal) return localProfile

    val basicUser = fetchLocalProfile(cleanEmail)
    return StudentProfile(email = cleanEmail, name = basicUser?.name ?: "", photoUrl = basicUser?.photoUrl)
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
    val now = System.currentTimeMillis()
    val data = mutableMapOf<String, Any>(
        "email" to cleanEmail,
        "name" to name,
        "provider" to provider,
        "category" to category,
        "updated_at" to now,
    )
    if (!photoUrl.isNullOrBlank()) {
        data["photo_url"] = photoUrl
    }
    withTimeoutOrNull(4.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                db.collection("users").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
    }
    return Result.success(Unit)
}

actual suspend fun fetchUserFromFirestore(email: String): UserProfile? {
    if (email.isBlank()) return null
    val cleanEmail = email.trim().lowercase()
    return withTimeoutOrNull(6.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            // Try 'admins'
            try {
                val doc = db.collection("admins").document(cleanEmail).get().awaitTask()
                if (doc.exists()) {
                    val data = doc.data
                    if (data != null) {
                        val photo = (data["photo_url"] as? String)?.ifBlank { null }
                        val name = (data["name"] as? String) ?: ""
                        if (name.isNotBlank() || photo != null) {
                            return@withTimeoutOrNull UserProfile(
                                email = (data["email"] as? String) ?: cleanEmail,
                                name = name,
                                photoUrl = photo,
                                provider = (data["provider"] as? String) ?: "google",
                                category = "ADMIN",
                                registeredAt = (data["registered_at"] as? Long) ?: 0L,
                                updatedAt = (data["updated_at"] as? Long) ?: 0L,
                            )
                        }
                    }
                }
            } catch (_: Exception) {}

            // Try 'teachers'
            try {
                val doc = db.collection("teachers").document(cleanEmail).get().awaitTask()
                if (doc.exists()) {
                    val data = doc.data
                    if (data != null) {
                        return@withTimeoutOrNull UserProfile(
                            email = (data["email"] as? String) ?: cleanEmail,
                            name = (data["name"] as? String) ?: "",
                            photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
                            provider = (data["provider"] as? String) ?: "google",
                            category = "TEACHER",
                            registeredAt = (data["registered_at"] as? Long) ?: 0L,
                            updatedAt = (data["updated_at"] as? Long) ?: 0L,
                        )
                    }
                }
            } catch (_: Exception) {}

            // Try 'students'
            try {
                val doc = db.collection("students").document(cleanEmail).get().awaitTask()
                if (doc.exists()) {
                    val data = doc.data
                    if (data != null) {
                        return@withTimeoutOrNull UserProfile(
                            email = (data["email"] as? String) ?: cleanEmail,
                            name = (data["name"] as? String) ?: "",
                            photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
                            provider = (data["provider"] as? String) ?: "google",
                            category = "STUDENT",
                            registeredAt = (data["registered_at"] as? Long) ?: 0L,
                            updatedAt = (data["updated_at"] as? Long) ?: 0L,
                        )
                    }
                }
            } catch (_: Exception) {}

            // Try 'users'
            try {
                val doc = db.collection("users").document(cleanEmail).get().awaitTask()
                if (doc.exists()) {
                    val data = doc.data
                    if (data != null) {
                        return@withTimeoutOrNull UserProfile(
                            email = (data["email"] as? String) ?: cleanEmail,
                            name = (data["name"] as? String) ?: "",
                            photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
                            provider = (data["provider"] as? String) ?: "",
                            category = (data["category"] as? String) ?: "STUDENT",
                            registeredAt = (data["registered_at"] as? Long) ?: 0L,
                            updatedAt = (data["updated_at"] as? Long) ?: 0L,
                        )
                    }
                }
            } catch (_: Exception) {}
        }
        null
    }
}

actual suspend fun deleteUserFromFirestore(email: String, role: String): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    withTimeoutOrNull(6.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                db.collection("users").document(cleanEmail).delete().awaitTask()
                when (role.uppercase()) {
                    "ADMIN" -> db.collection("admins").document(cleanEmail).delete().awaitTask()
                    "TEACHER" -> db.collection("teachers").document(cleanEmail).delete().awaitTask()
                    "STUDENT" -> db.collection("students").document(cleanEmail).delete().awaitTask()
                }
            } catch (_: Exception) {}
        }
    }
    return Result.success(Unit)
}

actual suspend fun fetchSignInControlFromFirestore(): SignInControlConfig {
    val instances = getFirestoreInstances()
    for ((db, _) in instances) {
        try {
            val doc = db.collection("config").document("signin_control").get().awaitTask()
            if (doc.exists()) {
                val data = doc.data
                if (data != null) {
                    return SignInControlConfig(
                        adminSignInEnabled = data["adminSignInEnabled"] as? Boolean ?: true,
                        teacherSignInEnabled = data["teacherSignInEnabled"] as? Boolean ?: true,
                        studentSignInEnabled = data["studentSignInEnabled"] as? Boolean ?: true,
                    )
                }
            }
        } catch (_: Exception) {}
    }
    return SignInControlConfig()
}

actual suspend fun saveSignInControlInFirestore(config: SignInControlConfig): Result<Unit> {
    val instances = getFirestoreInstances()
    val data = mapOf(
        "adminSignInEnabled" to config.adminSignInEnabled,
        "teacherSignInEnabled" to config.teacherSignInEnabled,
        "studentSignInEnabled" to config.studentSignInEnabled,
        "updatedAt" to System.currentTimeMillis()
    )
    withTimeoutOrNull(4.seconds) {
        for ((db, _) in instances) {
            try {
                db.collection("config").document("signin_control").set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
    }
    return Result.success(Unit)
}

actual suspend fun getAdminStatusFromFirestore(email: String): AdminStatus {
    if (email.isBlank()) return AdminStatus.NOT_REQUESTED
    val cleanEmail = email.trim().lowercase()

    val statusResult = withTimeoutOrNull(6.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                val doc = db.collection("admins").document(cleanEmail).get().awaitTask()
                if (doc.exists()) {
                    val statusStr = doc.getString("status")?.uppercase()
                    val enabled = doc.getBoolean("enabled") ?: true
                    return@withTimeoutOrNull when {
                        statusStr == "APPROVED" || (statusStr == null && enabled) -> AdminStatus.APPROVED
                        statusStr == "DECLINED" -> AdminStatus.DECLINED
                        statusStr == "PENDING" -> AdminStatus.PENDING
                        else -> AdminStatus.APPROVED
                    }
                }

                val qSnap = db.collection("admins").whereEqualTo("email", cleanEmail).get().awaitTask()
                if (!qSnap.isEmpty) {
                    val firstDoc = qSnap.documents.first()
                    val statusStr = firstDoc.getString("status")?.uppercase()
                    val enabled = firstDoc.getBoolean("enabled") ?: true
                    return@withTimeoutOrNull when {
                        statusStr == "APPROVED" || (statusStr == null && enabled) -> AdminStatus.APPROVED
                        statusStr == "DECLINED" -> AdminStatus.DECLINED
                        statusStr == "PENDING" -> AdminStatus.PENDING
                        else -> AdminStatus.APPROVED
                    }
                }
            } catch (_: Exception) {}
        }
        AdminStatus.NOT_REQUESTED
    }

    return statusResult ?: AdminStatus.NOT_REQUESTED
}

actual suspend fun submitAdminRequestToFirestore(email: String, name: String, photoUrl: String?): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = System.currentTimeMillis()
    val data = mapOf(
        "email" to cleanEmail,
        "name" to name,
        "photo_url" to (photoUrl ?: ""),
        "role" to "ADMIN",
        "status" to "PENDING",
        "requested_at" to now,
        "updated_at" to now,
    )

    withTimeoutOrNull(4.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                db.collection("admins").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
    }
    return Result.success(Unit)
}

actual suspend fun fetchPendingAdminRequestsFromFirestore(): List<AdminRequestItem> {
    val list = mutableListOf<AdminRequestItem>()
    val currentAuthEmail = try {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()
    } catch (_: Exception) { null }

    withTimeoutOrNull(5.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                val snap = db.collection("admins").get().awaitTask()
                for (doc in snap.documents) {
                    val em = (doc.getString("email") ?: doc.id).trim().lowercase()
                    val st = doc.getString("status")?.uppercase()

                    // Skip current admin user and primary admin and non-pending accounts
                    if (em.isBlank() || em == currentAuthEmail || em == "mdrofiqmpi@gmail.com" || st == "APPROVED" || st == "DECLINED") {
                        continue
                    }

                    if (st == "PENDING") {
                        if (list.none { it.email == em }) {
                            list.add(
                                AdminRequestItem(
                                    email = em,
                                    name = doc.getString("name") ?: em.substringBefore("@"),
                                    photoUrl = doc.getString("photo_url")?.ifBlank { null },
                                    status = "PENDING",
                                    requestedAt = doc.getLong("requested_at") ?: System.currentTimeMillis(),
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }
    return list
}

actual suspend fun updateAdminRequestStatusInFirestore(email: String, newStatus: String): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = System.currentTimeMillis()
    val data = mapOf(
        "status" to newStatus.uppercase(),
        "updated_at" to now,
    )

    withTimeoutOrNull(4.seconds) {
        val instances = getFirestoreInstances()
        for ((db, _) in instances) {
            try {
                db.collection("admins").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
    }
    return Result.success(Unit)
}

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

    fun itemFromDoc(doc: DocumentSnapshot): TeacherDirectoryItem? {
        val data = doc.data ?: return null
        val em = ((data["email"] as? String) ?: doc.id).trim().lowercase()
        if (em.isBlank()) return null
        return TeacherDirectoryItem(
            email = em,
            name = (data["name"] as? String)?.trim() ?: "",
            photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
            phone = (data["phone"] as? String)?.trim() ?: "",
            contactEmail = (data["contact_email"] as? String)?.trim() ?: "",
            department = (data["department"] as? String)?.trim() ?: "",
            shift = (data["shift"] as? String)?.trim() ?: "",
            bloodGroup = (data["blood_group"] as? String)?.trim() ?: "",
            gender = (data["gender"] as? String)?.trim() ?: "",
            address = (data["address"] as? String)?.trim() ?: "",
            permanentAddress = (data["permanent_address"] as? String)?.trim() ?: "",
            registeredAt = (data["registered_at"] as? Number)?.toLong() ?: 0L,
            updatedAt = (data["updated_at"] as? Number)?.toLong() ?: 0L,
        )
    }

    withTimeoutOrNull(8.seconds) {
        val instances = getFirestoreInstances()
        for ((db, dbLabel) in instances) {
            try {
                // 1. Primary source: 'teachers' collection (written whenever a user logs in or saves as TEACHER)
                val teachersSnap = db.collection("teachers").get().awaitTask()
                for (doc in teachersSnap.documents) {
                    val item = itemFromDoc(doc) ?: continue
                    mergeIntoMap(item.email, item)
                }

                // 2. Fallback: 'users' documents explicitly tagged with category == "TEACHER"
                try {
                    val usersTeacherSnap = db.collection("users").whereEqualTo("category", "TEACHER").get().awaitTask()
                    for (doc in usersTeacherSnap.documents) {
                        val item = itemFromDoc(doc) ?: continue
                        mergeIntoMap(item.email, item)
                    }
                } catch (_: Exception) {}

                // 3. Enrich known teachers with extended profile fields from 'students' (profile edits store all fields there)
                val knownEmails = teacherMap.keys.toList()
                for (teacherEmail in knownEmails) {
                    try {
                        val profileDoc = db.collection("students").document(teacherEmail).get().awaitTask()
                        if (profileDoc.exists()) {
                            val roleInProfile = (profileDoc.getString("role") ?: "").trim().uppercase()
                            if (roleInProfile.isBlank() || roleInProfile == "TEACHER") {
                                val enriched = itemFromDoc(profileDoc)
                                if (enriched != null) {
                                    mergeIntoMap(teacherEmail, enriched)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                Log.w(TAG, "[$dbLabel] Failed fetching teachers directory: ${e.message}")
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
        .sortedWith(compareByDescending<TeacherDirectoryItem> { maxOf(it.updatedAt, it.registeredAt) }.thenBy { it.name.lowercase() })
}

actual suspend fun deleteTeacherFromDatabaseInFirestore(email: String): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    var anyAttemptSucceeded = false
    var lastError: Exception? = null

    val completed = withTimeoutOrNull(8.seconds) {
        val instances = getFirestoreInstances()
        for ((db, dbLabel) in instances) {
            try {
                // 1. Delete from 'teachers' collection (direct doc + email-matched docs)
                db.collection("teachers").document(cleanEmail).delete().awaitTask()
                try {
                    val matchingTeachers = db.collection("teachers").whereEqualTo("email", cleanEmail).get().awaitTask()
                    for (doc in matchingTeachers.documents) {
                        doc.reference.delete().awaitTask()
                    }
                } catch (_: Exception) {}

                // 2. Delete from 'users' collection if tagged as TEACHER (or no conflicting non-teacher role)
                try {
                    val userDoc = db.collection("users").document(cleanEmail).get().awaitTask()
                    if (userDoc.exists()) {
                        val cat = (userDoc.getString("category") ?: "").trim().uppercase()
                        if (cat.isBlank() || cat == "TEACHER") {
                            userDoc.reference.delete().awaitTask()
                        }
                    }
                } catch (_: Exception) {}

                // 3. Delete extended profile from 'students' collection if stored for this teacher
                try {
                    val profileDoc = db.collection("students").document(cleanEmail).get().awaitTask()
                    if (profileDoc.exists()) {
                        val roleStr = (profileDoc.getString("role") ?: "").trim().uppercase()
                        if (roleStr.isBlank() || roleStr == "TEACHER") {
                            profileDoc.reference.delete().awaitTask()
                        }
                    }
                } catch (_: Exception) {}

                anyAttemptSucceeded = true
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "[$dbLabel] Failed deleting teacher $cleanEmail: ${e.message}")
            }
        }
        anyAttemptSucceeded
    }

    return if (completed == true || anyAttemptSucceeded) {
        Result.success(Unit)
    } else {
        Result.failure(lastError ?: Exception("Failed to delete teacher from Firestore"))
    }
}

private const val BOOKS_CACHE_PREFS = "klass_books_cache"
private const val BOOKS_CACHE_KEY = "books_json"
private const val BTEB_SEED_FLAG_KEY = "bteb_2022_v1_seeded"
private val booksSeedMutex = Mutex()
private var booksSeedVerifiedThisSession = false

private val booksSortComparator = compareByDescending<LibraryBook> {
    maxOf(it.updatedAt, it.createdAt)
}.thenBy {
    it.subjectCode.trim()
}.thenBy {
    it.title.lowercase()
}

private fun bookToFirestoreMap(book: LibraryBook): Map<String, Any> = mapOf(
    "id" to book.id,
    "title" to book.title,
    "subject_code" to book.subjectCode,
    "department_semesters" to book.departmentSemesters,
    "theory_classes_per_week" to book.theoryClassesPerWeek,
    "practical_classes_per_week" to book.practicalClassesPerWeek,
    "credit" to book.credit,
    "created_at" to book.createdAt,
    "updated_at" to book.updatedAt,
)

private fun readLocalBooksCache(): List<LibraryBook> {
    val prefs = getAppContext()?.getSharedPreferences(BOOKS_CACHE_PREFS, Context.MODE_PRIVATE) ?: return emptyList()
    val raw = prefs.getString(BOOKS_CACHE_KEY, null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        val result = mutableListOf<LibraryBook>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id").trim()
            if (id.isBlank()) continue
            val deptSemMap = linkedMapOf<String, List<String>>()
            val deptObj = obj.optJSONObject("department_semesters")
            if (deptObj != null) {
                val keys = deptObj.keys()
                while (keys.hasNext()) {
                    val deptKey = keys.next()
                    val semArr = deptObj.optJSONArray(deptKey) ?: continue
                    val sems = mutableListOf<String>()
                    for (j in 0 until semArr.length()) {
                        val s = semArr.optString(j).trim()
                        if (s.isNotBlank()) sems.add(s)
                    }
                    if (sems.isNotEmpty()) {
                        deptSemMap[deptKey] = sems
                    }
                }
            }
            result.add(
                LibraryBook(
                    id = id,
                    title = obj.optString("title"),
                    subjectCode = obj.optString("subject_code"),
                    departmentSemesters = deptSemMap,
                    theoryClassesPerWeek = obj.optInt("theory_classes_per_week", 0),
                    practicalClassesPerWeek = obj.optInt("practical_classes_per_week", 0),
                    credit = obj.optString("credit"),
                    createdAt = obj.optLong("created_at", 0L),
                    updatedAt = obj.optLong("updated_at", 0L),
                )
            )
        }
        result.sortedWith(booksSortComparator)
    } catch (_: Exception) {
        emptyList()
    }
}

private fun writeLocalBooksCache(books: List<LibraryBook>) {
    val prefs = getAppContext()?.getSharedPreferences(BOOKS_CACHE_PREFS, Context.MODE_PRIVATE) ?: return
    try {
        val arr = JSONArray()
        for (book in books) {
            val obj = JSONObject()
            obj.put("id", book.id)
            obj.put("title", book.title)
            obj.put("subject_code", book.subjectCode)
            val deptObj = JSONObject()
            for ((dept, sems) in book.departmentSemesters) {
                val semArr = JSONArray()
                for (sem in sems) {
                    semArr.put(sem)
                }
                deptObj.put(dept, semArr)
            }
            obj.put("department_semesters", deptObj)
            obj.put("theory_classes_per_week", book.theoryClassesPerWeek)
            obj.put("practical_classes_per_week", book.practicalClassesPerWeek)
            obj.put("credit", book.credit)
            obj.put("created_at", book.createdAt)
            obj.put("updated_at", book.updatedAt)
            arr.put(obj)
        }
        prefs.edit().putString(BOOKS_CACHE_KEY, arr.toString()).apply()
    } catch (_: Exception) {}
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
        val merged = existing.values.sortedWith(booksSortComparator)
        writeLocalBooksCache(merged)
    }
}

actual suspend fun seedBtebLibraryBooksInFirestore(forceReseed: Boolean): Result<Int> {
    return booksSeedMutex.withLock {
        val prefs = getAppContext()?.getSharedPreferences(BOOKS_CACHE_PREFS, Context.MODE_PRIVATE)
        val isAuthenticated = try {
            FirebaseAuth.getInstance().currentUser != null
        } catch (_: Exception) {
            false
        }

        if (!isAuthenticated) {
            mergeBtebCatalogIntoLocalCache()
            return@withLock Result.failure(Exception("Firebase user not signed in"))
        }

        val catalogBooks = BtebBooksCatalog.books
        val instances = getFirestoreInstances()
        if (instances.isEmpty()) {
            mergeBtebCatalogIntoLocalCache()
            return@withLock Result.failure(Exception("No Firestore instances available"))
        }

        var seededOrVerifiedDbs = 0
        var lastError: Exception? = null

        for ((db, dbLabel) in instances) {
            try {
                val markerRef = db.collection("config").document(BtebBooksCatalog.SEED_VERSION_DOC)
                val markerDoc = withTimeoutOrNull(6.seconds) { markerRef.get().awaitTask() }
                val alreadySeeded = markerDoc != null && markerDoc.exists() && markerDoc.getBoolean("seeded") == true
                if (!forceReseed && alreadySeeded) {
                    seededOrVerifiedDbs++
                    continue
                }

                // Batch-write all 179 BTEB Probidhan-2022 books + idempotency marker atomically
                val chunks = catalogBooks.chunked(450)
                var batchWriteOk = true
                for ((chunkIdx, chunk) in chunks.withIndex()) {
                    val batch = db.batch()
                    for (book in chunk) {
                        batch.set(
                            db.collection("books").document(book.id),
                            bookToFirestoreMap(book),
                            SetOptions.merge(),
                        )
                    }
                    if (chunkIdx == chunks.lastIndex) {
                        val markerData = mapOf(
                            "seeded" to true,
                            "count" to catalogBooks.size,
                            "source" to "bteb.gov.bd",
                            "probidhan" to "2022",
                            "seeded_at" to System.currentTimeMillis(),
                        )
                        batch.set(markerRef, markerData, SetOptions.merge())
                    }
                    val committed = withTimeoutOrNull(12.seconds) {
                        batch.commit().awaitTask()
                        true
                    } ?: false
                    if (!committed) {
                        batchWriteOk = false
                        break
                    }
                }
                if (batchWriteOk) {
                    seededOrVerifiedDbs++
                }
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "[$dbLabel] BTEB books seeding failed: ${e.message}")
            }
        }

        if (seededOrVerifiedDbs > 0) {
            val localWasSeeded = prefs?.getBoolean(BTEB_SEED_FLAG_KEY, false) ?: false
            if (!localWasSeeded || forceReseed) {
                mergeBtebCatalogIntoLocalCache()
            }
            prefs?.edit()?.putBoolean(BTEB_SEED_FLAG_KEY, true)?.apply()
            booksSeedVerifiedThisSession = true
            Result.success(catalogBooks.size)
        } else {
            mergeBtebCatalogIntoLocalCache()
            Result.failure(lastError ?: Exception("Failed to seed BTEB books in Firestore"))
        }
    }
}

actual suspend fun fetchLibraryBooksFromFirestore(): List<LibraryBook> {
    val prefs = getAppContext()?.getSharedPreferences(BOOKS_CACHE_PREFS, Context.MODE_PRIVATE)
    val localSeedFlag = prefs?.getBoolean(BTEB_SEED_FLAG_KEY, false) ?: false
    val isAuthenticated = try {
        FirebaseAuth.getInstance().currentUser != null
    } catch (_: Exception) {
        false
    }

    if (isAuthenticated && (!localSeedFlag || !booksSeedVerifiedThisSession)) {
        seedBtebLibraryBooksInFirestore(forceReseed = false)
    }

    val bookMap = linkedMapOf<String, LibraryBook>()
    var fetchedAnyDbSuccessfully = false

    withTimeoutOrNull(12.seconds) {
        val instances = getFirestoreInstances()
        for ((db, dbLabel) in instances) {
            try {
                val snap = db.collection("books").get().awaitTask()
                fetchedAnyDbSuccessfully = true
                for (doc in snap.documents) {
                    val data = doc.data ?: continue
                    val id = ((data["id"] as? String)?.takeIf { it.isNotBlank() } ?: doc.id).trim()
                    if (id.isBlank()) continue

                    val deptSemMap = linkedMapOf<String, List<String>>()
                    val rawMap = data["department_semesters"] as? Map<*, *>
                    if (rawMap != null) {
                        for ((k, v) in rawMap) {
                            val deptKey = (k as? String)?.trim() ?: continue
                            val rawList = v as? List<*> ?: continue
                            val sems = rawList.mapNotNull { (it as? String)?.trim()?.takeIf { s -> s.isNotBlank() } }
                            if (sems.isNotEmpty()) {
                                deptSemMap[deptKey] = sems
                            }
                        }
                    }

                    val incoming = LibraryBook(
                        id = id,
                        title = (data["title"] as? String)?.trim() ?: "",
                        subjectCode = (data["subject_code"] as? String)?.trim() ?: "",
                        departmentSemesters = deptSemMap,
                        theoryClassesPerWeek = (data["theory_classes_per_week"] as? Number)?.toInt() ?: 0,
                        practicalClassesPerWeek = (data["practical_classes_per_week"] as? Number)?.toInt() ?: 0,
                        credit = (data["credit"] as? String)?.trim() ?: ((data["credit"] as? Number)?.toString() ?: ""),
                        createdAt = (data["created_at"] as? Number)?.toLong() ?: 0L,
                        updatedAt = (data["updated_at"] as? Number)?.toLong() ?: 0L,
                    )

                    val existing = bookMap[id]
                    if (existing == null || incoming.updatedAt >= existing.updatedAt) {
                        bookMap[id] = incoming
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "[$dbLabel] Failed fetching books library: ${e.message}")
            }
        }
    }

    if (fetchedAnyDbSuccessfully) {
        if (bookMap.isEmpty() && !localSeedFlag) {
            mergeBtebCatalogIntoLocalCache()
            return readLocalBooksCache()
        }
        val sorted = bookMap.values.sortedWith(booksSortComparator)
        writeLocalBooksCache(sorted)
        return sorted
    }

    val cached = readLocalBooksCache()
    if (cached.isNotEmpty()) return cached
    mergeBtebCatalogIntoLocalCache()
    return readLocalBooksCache()
}

actual suspend fun saveLibraryBookInFirestore(book: LibraryBook): Result<Unit> {
    val now = System.currentTimeMillis()
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
    if (existingIdx >= 0) {
        currentCache[existingIdx] = cleanBook
    } else {
        currentCache.add(0, cleanBook)
    }
    writeLocalBooksCache(currentCache.sortedWith(booksSortComparator))

    val data = bookToFirestoreMap(cleanBook)

    var anySucceeded = false
    var lastError: Exception? = null
    withTimeoutOrNull(6.seconds) {
        val instances = getFirestoreInstances()
        for ((db, dbLabel) in instances) {
            try {
                db.collection("books").document(bookId).set(data, SetOptions.merge()).awaitTask()
                anySucceeded = true
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "[$dbLabel] Failed saving book $bookId: ${e.message}")
            }
        }
    }

    return if (anySucceeded) {
        Result.success(Unit)
    } else {
        Result.failure(lastError ?: Exception("Failed to save book to Firestore"))
    }
}

actual suspend fun deleteLibraryBookFromFirestore(bookId: String): Result<Unit> {
    val cleanId = bookId.trim()
    if (cleanId.isBlank()) return Result.failure(Exception("Invalid book ID"))

    val updatedCache = readLocalBooksCache().filterNot { it.id == cleanId }
    writeLocalBooksCache(updatedCache)

    var anySucceeded = false
    var lastError: Exception? = null
    withTimeoutOrNull(6.seconds) {
        val instances = getFirestoreInstances()
        for ((db, dbLabel) in instances) {
            try {
                db.collection("books").document(cleanId).delete().awaitTask()
                anySucceeded = true
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "[$dbLabel] Failed deleting book $cleanId: ${e.message}")
            }
        }
    }

    return if (anySucceeded) {
        Result.success(Unit)
    } else {
        Result.failure(lastError ?: Exception("Failed to delete book from Firestore"))
    }
}
