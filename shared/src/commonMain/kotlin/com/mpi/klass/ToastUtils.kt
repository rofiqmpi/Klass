package com.mpi.klass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppToastTone {
    INFO,
    SUCCESS,
    ERROR,
}

data class AppToastMessage(
    val id: Long,
    val message: String,
    val tone: AppToastTone,
)

object AppToastState {
    private var nextId = 1L
    var currentToast by mutableStateOf<AppToastMessage?>(null)
        internal set

    fun show(message: String, tone: AppToastTone? = null) {
        val clean = message.trim()
        if (clean.isBlank()) return
        currentToast = AppToastMessage(
            id = nextId++,
            message = clean,
            tone = tone ?: inferToastTone(clean),
        )
    }

    fun dismiss(id: Long? = null) {
        if (id == null || currentToast?.id == id) {
            currentToast = null
        }
    }
}

private fun inferToastTone(message: String): AppToastTone {
    val lower = message.lowercase()
    val errorKeywords = listOf(
        "failed", "error", "invalid", "declined", "denied", "expired",
        "could not", "cannot", "empty", "wrong", "no classes",
        "ব্যর্থ", "ভুল", "সঠিক নয়", "পারেননি", "সমস্যা", "কমপক্ষে", "নয়",
    )
    if (errorKeywords.any { lower.contains(it) }) return AppToastTone.ERROR

    val successKeywords = listOf(
        "saved", "copied", "sent", "published", "approved", "added",
        "updated", "success", "connected", "downloaded",
        "সফল", "সংরক্ষিত", "সংযুক্ত", "পাঠানো হয়েছে", "কপি", "তৈরি হয়েছে", "সম্পন্ন",
    )
    if (successKeywords.any { lower.contains(it) }) return AppToastTone.SUCCESS

    return AppToastTone.INFO
}

expect fun showToast(message: String)
expect fun getCurrentTimeMillis(): Long
