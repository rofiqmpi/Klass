package com.mpi.klass

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull

object AdminConfig {

    /**
     * Checks whether [email] is configured as an admin in Firebase (Firestore or Realtime Database).
     * Returns Result.success(true) if admin is found, Result.success(false) if unauthorized,
     * or Result.failure(Exception("NO_INTERNET")) / Exception("NETWORK_TIMEOUT") on connection failure.
     */
    suspend fun verifyAdminInFirebase(email: String): Result<Boolean> {
        if (email.isBlank()) return Result.success(value = false)

        if (!isNetworkAvailable()) {
            return Result.failure(Exception("NO_INTERNET"))
        }

        val cleanEmail = email.trim().lowercase()
        val emailUsername = cleanEmail.substringBefore("@")
        val sanitizedKey = cleanEmail.replace(".", ",")
        val altKey = cleanEmail.replace(".", "_")

        return try {
            val isVerified = withTimeoutOrNull(6.seconds) {
                // 1. Check in Firebase Firestore "admins" collection
                try {
                    val firestoreDb = Firebase.firestore

                    // Direct doc check
                    try {
                        val doc = firestoreDb.collection("admins").document(cleanEmail).get()
                        if (doc.exists) return@withTimeoutOrNull true
                    } catch (_: Exception) {}

                    // Fetch all docs in "admins" and check IDs and fields
                    try {
                        val allDocs = firestoreDb.collection("admins").get()
                        for (doc in allDocs.documents) {
                            val docId = doc.id.trim().lowercase()
                            if ((docId == cleanEmail) || (docId == emailUsername)) {
                                return@withTimeoutOrNull true
                            }

                            try {
                                val dataString = doc.data<Map<String, Any>>().toString().lowercase()
                                if (dataString.contains(cleanEmail)) {
                                    return@withTimeoutOrNull true
                                }
                            } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {}
                } catch (_: Exception) {}

                // 2. Check Firebase Realtime Database "admins" node
                try {
                    val rtdb = Firebase.database
                    val ref = rtdb.reference("admins")

                    val keysToCheck = listOf(sanitizedKey, altKey, cleanEmail, emailUsername)
                    for (key in keysToCheck) {
                        try {
                            val snap = ref.child(key).get()
                            if (snap.exists && (snap.value != null)) {
                                return@withTimeoutOrNull true
                            }
                        } catch (_: Exception) {}
                    }

                    try {
                        val fullSnap = ref.get()
                        if (fullSnap.exists) {
                            val fullData = fullSnap.value.toString().lowercase()
                            if (fullData.contains(cleanEmail)) {
                                return@withTimeoutOrNull true
                            }
                        }
                    } catch (_: Exception) {}
                } catch (_: Exception) {}

                false
            }

            if (isVerified == true) {
                Result.success(true)
            } else if (isVerified == null) {
                if (!isNetworkAvailable()) {
                    Result.failure(Exception("NO_INTERNET"))
                } else {
                    Result.failure(Exception("NETWORK_TIMEOUT"))
                }
            } else {
                Result.success(false)
            }
        } catch (_: Exception) {
            if (!isNetworkAvailable()) {
                Result.failure(Exception("NO_INTERNET"))
            } else {
                Result.failure(Exception("NETWORK_TIMEOUT"))
            }
        }
    }
}
