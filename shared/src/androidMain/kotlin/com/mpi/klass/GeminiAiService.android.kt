package com.mpi.klass

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiAiService"

actual object GeminiAiService {
    // Model initialized lazily per Firebase AI Logic requirements
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    actual suspend fun generateStream(prompt: String, onChunk: (String) -> Unit): String {
        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    accumulated.append(text)
                    onChunk(text)
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Model returned an empty response.")
            }
            return accumulated.toString()
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Streaming timed out", e)
            throw IllegalStateException("AI প্রতিক্রিয়া সময়সীমা অতিক্রান্ত হয়েছে (Timeout). অনুগ্রহ করে আবার চেষ্টা করুন।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Streaming generation failed", e)
            throw e
        }
    }

    actual suspend fun generateContent(prompt: String): String {
        try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val text = response.text
            if (text.isNullOrBlank()) {
                throw IllegalStateException("Model returned an empty response.")
            }
            return text
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Generation timed out", e)
            throw IllegalStateException("AI প্রতিক্রিয়া সময়সীমা অতিক্রান্ত হয়েছে (Timeout). অনুগ্রহ করে আবার চেষ্টা করুন।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed", e)
            throw e
        }
    }
}
