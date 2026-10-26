package com.mpi.klass

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull

object GuestConfig {

    private sealed interface FetchOutcome {
        data class Found(val configMap: Map<String, Any>) : FetchOutcome
        data object Missing : FetchOutcome
    }

    /**
     * Fetches Guest Mode settings from Firebase Firestore "guests" collection.
     *
     *  - Result.success(map)  -> config document found
     *  - Result.success(null) -> document not configured (guest mode still allowed)
     *  - Result.failure("NO_INTERNET" / "NETWORK_TIMEOUT") -> caller must NOT enter guest mode
     */
    suspend fun fetchGuestConfig(): Result<Map<String, Any>?> {
        return try {
            var networkError: Exception? = null

            val outcome = withTimeoutOrNull(5.seconds) {
                try {
                    val doc = Firebase.firestore.collection("guests").document("config").get()
                    if (doc.exists) {
                        try {
                            FetchOutcome.Found(doc.data<Map<String, Any>>())
                        } catch (_: Exception) {
                            FetchOutcome.Missing
                        }
                    } else {
                        FetchOutcome.Missing
                    }
                } catch (e: Exception) {
                    networkError = e
                    null
                }
            }

            when (outcome) {
                is FetchOutcome.Found -> Result.success(outcome.configMap)
                is FetchOutcome.Missing -> Result.success(null)
                null -> Result.failure(Exception(networkErrorMessage(networkError)))
            }
        } catch (_: Exception) {
            Result.failure(Exception(networkErrorMessage(null)))
        }
    }

    private suspend fun networkErrorMessage(caught: Exception?): String {
        // A real network failure and a silent timeout are both reported
        // precisely so the caller can pick the right error popup.
        return if ((caught != null) || !isNetworkAvailable()) "NO_INTERNET" else "NETWORK_TIMEOUT"
    }
}
