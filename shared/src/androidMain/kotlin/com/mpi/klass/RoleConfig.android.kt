package com.mpi.klass

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mpi.klass.theme.getAppContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "AdminAuth"
private const val CUSTOM_DB_ID = "ai-studio-android-klass-12f1bead-bdd0-4fa6-8971-0f8fe8db4bd5"

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
    prefs.edit()
        .putString("${cleanEmail}_name", name)
        .putString("${cleanEmail}_photo", photoUrl ?: "")
        .putString("${cleanEmail}_provider", provider)
        .putString("${cleanEmail}_category", category)
        .putLong("${cleanEmail}_time", System.currentTimeMillis())
        .apply()
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

actual fun deleteLocalProfile(email: String) {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return
    val cleanEmail = email.trim().lowercase()
    prefs.edit()
        .remove("${cleanEmail}_name")
        .remove("${cleanEmail}_photo")
        .remove("${cleanEmail}_provider")
        .remove("${cleanEmail}_category")
        .remove("${cleanEmail}_time")
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
        Log.d(TAG, "Matched admin by doc.id: '$docId'")
        return true
    }

    val data = doc.data ?: return false
    val emailFields = listOf("email", "Email", "mail", "admin_email", "adminEmail", "user_email", "userEmail")
    for (f in emailFields) {
        val v = (data[f] as? String)?.trim()?.lowercase()
        if (v == cleanEmail) {
            Log.d(TAG, "Matched admin by field '$f': '$v' in doc ${doc.id}")
            return true
        }
    }

    for ((key, value) in data) {
        if (value is String && value.trim().lowercase() == cleanEmail) {
            Log.d(TAG, "Matched admin by string field '$key' == '$value' in doc ${doc.id}")
            return true
        }
        if (value is List<*>) {
            for (item in value) {
                if (item is String && item.trim().lowercase() == cleanEmail) {
                    Log.d(TAG, "Matched admin in list '$key' containing '$item' in doc ${doc.id}")
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

            // 3. Collection scan (for small collections like admins, guarantees finding custom doc IDs or mixed casing)
            try {
                val allDocs = colRef.get().awaitTask()
                Log.d(TAG, "[$dbLabel] Collection '$colName' contains ${allDocs.size()} docs: ${allDocs.documents.map { it.id }}")
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
    Log.d(TAG, "Starting admin authorization check for '$cleanEmail'")

    val result = withTimeoutOrNull(8.seconds) {
        // 1. Check default database
        try {
            val defaultDb = FirebaseFirestore.getInstance()
            if (checkDatabaseForAdmin(defaultDb, "default_db", cleanEmail)) {
                return@withTimeoutOrNull true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing default Firestore database: ${e.message}")
        }

        // 2. Check custom named database
        try {
            val customDb = FirebaseFirestore.getInstance(CUSTOM_DB_ID)
            if (checkDatabaseForAdmin(customDb, "custom_db", cleanEmail)) {
                return@withTimeoutOrNull true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing custom Firestore database ($CUSTOM_DB_ID): ${e.message}")
        }

        false
    } ?: false

    Log.d(TAG, "Admin authorization result for '$cleanEmail': $result")
    return result
}

actual suspend fun saveAdminInFirestore(email: String, name: String, photoUrl: String?): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val now = System.currentTimeMillis()
    val data = mapOf(
        "email" to cleanEmail,
        "name" to name,
        "photo_url" to (photoUrl ?: ""),
        "role" to "ADMIN",
        "last_login_at" to now,
        "updated_at" to now,
    )

    try {
        FirebaseFirestore.getInstance().collection("admins").document(cleanEmail)
            .set(data, SetOptions.merge())
    } catch (_: Exception) {}

    try {
        FirebaseFirestore.getInstance(CUSTOM_DB_ID).collection("admins").document(cleanEmail)
            .set(data, SetOptions.merge())
    } catch (_: Exception) {}

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
    val data = mapOf(
        "email" to cleanEmail,
        "name" to name,
        "photo_url" to (photoUrl ?: ""),
        "role" to "TEACHER",
        "provider" to provider,
        "registered_at" to now,
        "updated_at" to now,
    )

    try {
        FirebaseFirestore.getInstance().collection("teachers").document(cleanEmail)
            .set(data, SetOptions.merge())
    } catch (_: Exception) {}

    try {
        FirebaseFirestore.getInstance(CUSTOM_DB_ID).collection("teachers").document(cleanEmail)
            .set(data, SetOptions.merge())
    } catch (_: Exception) {}

    return Result.success(Unit)
}
