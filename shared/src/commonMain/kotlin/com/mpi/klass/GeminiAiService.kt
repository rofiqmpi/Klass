package com.mpi.klass

expect object GeminiAiService {
    suspend fun generateStream(prompt: String, onChunk: (String) -> Unit): String
    suspend fun generateContent(prompt: String): String
}
