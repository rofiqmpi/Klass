package com.mpi.klass

import kotlinx.browser.localStorage

actual fun saveLocalProfile(email: String, name: String, photoUrl: String?, provider: String, category: String) {
    val cleanEmail = email.trim().lowercase()
    try {
        localStorage.setItem("${cleanEmail}_name", name)
        localStorage.setItem("${cleanEmail}_photo", photoUrl ?: "")
        localStorage.setItem("${cleanEmail}_provider", provider)
        localStorage.setItem("${cleanEmail}_category", category)
    } catch (_: Throwable) {}
}

actual fun fetchLocalProfile(email: String): UserProfile? {
    val cleanEmail = email.trim().lowercase()
    return try {
        val name = localStorage.getItem("${cleanEmail}_name") ?: return null
        val photoUrl = localStorage.getItem("${cleanEmail}_photo")?.ifBlank { null }
        val provider = localStorage.getItem("${cleanEmail}_provider") ?: ""
        val category = localStorage.getItem("${cleanEmail}_category") ?: "STUDENT"
        UserProfile(
            email = cleanEmail,
            name = name,
            photoUrl = photoUrl,
            provider = provider,
            category = category,
        )
    } catch (_: Throwable) {
        null
    }
}

actual fun deleteLocalProfile(email: String) {
    val cleanEmail = email.trim().lowercase()
    try {
        localStorage.removeItem("${cleanEmail}_name")
        localStorage.removeItem("${cleanEmail}_photo")
        localStorage.removeItem("${cleanEmail}_provider")
        localStorage.removeItem("${cleanEmail}_category")
    } catch (_: Throwable) {}
}

actual fun updateLocalCategory(email: String, category: String) {
    val cleanEmail = email.trim().lowercase()
    try {
        localStorage.setItem("${cleanEmail}_category", category)
    } catch (_: Throwable) {}
}
