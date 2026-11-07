package com.mpi.klass

import kotlinx.browser.localStorage

actual fun saveUserSession(session: UserSession) {
    try {
        localStorage.setItem("is_logged_in", "true")
        localStorage.setItem("user_role", session.role)
        localStorage.setItem("user_email", session.email)
        localStorage.setItem("user_name", session.name)
        localStorage.setItem("user_photo", session.photoUrl ?: "")
    } catch (_: Throwable) {}
}

actual fun loadUserSession(): UserSession? {
    return try {
        val isLoggedIn = localStorage.getItem("is_logged_in") == "true"
        if (!isLoggedIn) return null

        val role = localStorage.getItem("user_role") ?: ""
        val email = localStorage.getItem("user_email") ?: ""
        val name = localStorage.getItem("user_name") ?: ""
        val photoUrl = localStorage.getItem("user_photo")?.ifBlank { null }

        if (role.isBlank() || email.isBlank()) return null
        UserSession(role = role, email = email, name = name, photoUrl = photoUrl)
    } catch (_: Throwable) {
        null
    }
}

actual fun clearUserSession() {
    try {
        localStorage.removeItem("is_logged_in")
        localStorage.removeItem("user_role")
        localStorage.removeItem("user_email")
        localStorage.removeItem("user_name")
        localStorage.removeItem("user_photo")
    } catch (_: Throwable) {}
}
