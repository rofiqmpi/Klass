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

            // 3. Collection scan
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

    val instances = getFirestoreInstances()
    for ((db, _) in instances) {
        try {
            db.collection("admins").document(cleanEmail).set(data, SetOptions.merge())
        } catch (_: Exception) {}
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
    val data = mapOf(
        "email" to cleanEmail,
        "name" to name,
        "photo_url" to (photoUrl ?: ""),
        "role" to "TEACHER",
        "provider" to provider,
        "registered_at" to now,
        "updated_at" to now,
    )

    val instances = getFirestoreInstances()
    for ((db, _) in instances) {
        try {
            db.collection("teachers").document(cleanEmail).set(data, SetOptions.merge())
        } catch (_: Exception) {}
    }

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
    val now = System.currentTimeMillis()
    val data = mapOf(
        "email" to cleanEmail,
        "name" to name,
        "photo_url" to (photoUrl ?: ""),
        "provider" to provider,
        "category" to category,
        "updated_at" to now,
    )
    val instances = getFirestoreInstances()
    for ((db, _) in instances) {
        try {
            db.collection("users").document(cleanEmail).set(data, SetOptions.merge())
        } catch (_: Exception) {}
    }
    return Result.success(Unit)
}

actual suspend fun fetchUserFromFirestore(email: String): UserProfile? {
    if (email.isBlank()) return null
    val cleanEmail = email.trim().lowercase()
    val instances = getFirestoreInstances()

    for ((db, _) in instances) {
        // Try 'teachers' collection
        try {
            val teacherDoc = db.collection("teachers").document(cleanEmail).get().awaitTask()
            if (teacherDoc.exists()) {
                val data = teacherDoc.data
                if (data != null) {
                    val prof = UserProfile(
                        email = (data["email"] as? String) ?: cleanEmail,
                        name = (data["name"] as? String) ?: "",
                        photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
                        provider = (data["provider"] as? String) ?: "google",
                        category = UserRole.TEACHER.name,
                        registeredAt = (data["registered_at"] as? Long) ?: (data["registeredAt"] as? Long) ?: 0L,
                        updatedAt = (data["updated_at"] as? Long) ?: (data["updatedAt"] as? Long) ?: 0L,
                    )
                    saveLocalProfile(cleanEmail, prof.name, prof.photoUrl, prof.provider, prof.category)
                    return prof
                }
            }
        } catch (_: Exception) {}

        // Try 'users' collection
        try {
            val userDoc = db.collection("users").document(cleanEmail).get().awaitTask()
            if (userDoc.exists()) {
                val data = userDoc.data
                if (data != null) {
                    val prof = UserProfile(
                        email = (data["email"] as? String) ?: cleanEmail,
                        name = (data["name"] as? String) ?: "",
                        photoUrl = (data["photo_url"] as? String)?.ifBlank { null },
                        provider = (data["provider"] as? String) ?: "",
                        category = (data["category"] as? String) ?: "STUDENT",
                        registeredAt = (data["registered_at"] as? Long) ?: (data["registeredAt"] as? Long) ?: 0L,
                        updatedAt = (data["updated_at"] as? Long) ?: (data["updatedAt"] as? Long) ?: 0L,
                    )
                    saveLocalProfile(cleanEmail, prof.name, prof.photoUrl, prof.provider, prof.category)
                    return prof
                }
            }
        } catch (_: Exception) {}
    }

    return null
}

actual suspend fun deleteUserFromFirestore(email: String): Result<Unit> {
    if (email.isBlank()) return Result.failure(Exception("Invalid email"))
    val cleanEmail = email.trim().lowercase()
    val instances = getFirestoreInstances()
    for ((db, _) in instances) {
        try {
            db.collection("users").document(cleanEmail).delete()
            db.collection("teachers").document(cleanEmail).delete()
        } catch (_: Exception) {}
    }
    return Result.success(Unit)
}
