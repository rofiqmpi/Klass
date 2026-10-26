package com.mpi.klass

import android.content.Context
import com.mpi.klass.theme.getAppContext

actual fun saveUserSession(session: UserSession) {
    getAppContext()?.getSharedPreferences("klass_session", Context.MODE_PRIVATE)
        ?.edit()
        ?.putBoolean("is_logged_in", true)
        ?.putString("user_role", session.role)
        ?.putString("user_email", session.email)
        ?.putString("user_name", session.name)
        ?.apply()
}

actual fun loadUserSession(): UserSession? {
    val prefs = getAppContext()?.getSharedPreferences("klass_session", Context.MODE_PRIVATE)
        ?: return null
    val isLoggedIn = prefs.getBoolean("is_logged_in", false)
    if (!isLoggedIn) return null

    val role = prefs.getString("user_role", "") ?: ""
    val email = prefs.getString("user_email", "") ?: ""
    val name = prefs.getString("user_name", "") ?: ""

    if (role.isBlank() || email.isBlank()) return null
    return UserSession(role = role, email = email, name = name)
}

actual fun clearUserSession() {
    getAppContext()?.getSharedPreferences("klass_session", Context.MODE_PRIVATE)
        ?.edit()
        ?.clear()
        ?.apply()
}
