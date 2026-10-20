package com.mpi.klass

import platform.Foundation.NSUserDefaults

actual fun saveUserSession(session: UserSession) {
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.setBool(true, forKey = "is_logged_in")
    defaults.setObject(session.role, forKey = "user_role")
    defaults.setObject(session.email, forKey = "user_email")
    defaults.setObject(session.name, forKey = "user_name")
}

actual fun loadUserSession(): UserSession? {
    val defaults = NSUserDefaults.standardUserDefaults
    val isLoggedIn = defaults.boolForKey("is_logged_in")
    if (!isLoggedIn) return null

    val role = defaults.stringForKey("user_role") ?: ""
    val email = defaults.stringForKey("user_email") ?: ""
    val name = defaults.stringForKey("user_name") ?: ""

    if (role.isBlank() || email.isBlank()) return null
    return UserSession(role = role, email = email, name = name)
}

actual fun clearUserSession() {
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.removeObjectForKey("is_logged_in")
    defaults.removeObjectForKey("user_role")
    defaults.removeObjectForKey("user_email")
    defaults.removeObjectForKey("user_name")
    defaults.synchronize()
}
