package com.mpi.klass

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull

object GuestConfig {

    /**
     * Fetches Guest Mode settings from Firebase Firestore "guests" collection.
     * Returns failure if offline or network connection fails.
     */
    suspend fun fetchGuestConfig(): Result<Map<String, Any>?> {
        var networkError: Exception? = null

        return try {
            val configMap = withTimeoutOrNull(5.seconds) {
                try {
                    val firestoreDb = Firebase.firestore
                    val doc = firestoreDb.collection("guests").document("config").get()
                    if (doc.exists) {
                        doc.data<Map<String, Any>>()
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    networkError = e
                    null
                }
            }

            if (configMap != null) {
                Result.success(configMap)
            } else if (networkError != null) {
                Result.failure(Exception("NO_INTERNET"))
            } else {
                // If doc doesn't exist or timed out without exception
                Result.success(null)
            }
        } catch (_: Exception) {
            Result.failure(Exception("NO_INTERNET"))
        }
    }
}
