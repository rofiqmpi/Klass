package com.mpi.klass

import kotlin.concurrent.Volatile

object ZedAiKeyProvider {
    @Volatile
    private var geminiApiKey: String = ""

    fun setApiKey(rawKey: String?) {
        val trimmed = rawKey?.trim().orEmpty()
        geminiApiKey = if (trimmed.isBlank() || trimmed == "GEMINI_API_KEY_DEFAULT_VALUE") {
            ""
        } else {
            trimmed
        }
    }

    fun getApiKey(): String = geminiApiKey
}

object ZedAppCheckDiagnostics {
    @Volatile
    private var provider: (suspend () -> String?)? = null

    fun register(diagnosticProvider: suspend () -> String?) {
        provider = diagnosticProvider
    }

    suspend fun checkError(): String? {
        val fn = provider ?: return null
        return try {
            fn()
        } catch (_: Throwable) {
            null
        }
    }
}

expect suspend fun askZedWithGemini(
    prompt: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
    history: List<AiChatMessage> = emptyList(),
    onPartialText: (String) -> Unit = {},
): ZedReply
