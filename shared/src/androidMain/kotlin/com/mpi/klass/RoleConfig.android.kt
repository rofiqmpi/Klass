package com.mpi.klass

import android.content.Context
import com.mpi.klass.theme.getAppContext

actual fun saveLocalProfile(email: String, name: String, photoUrl: String?, provider: String, category: String) {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return
    val cleanEmail = email.trim().lowercase()
    prefs.edit()
        .putString("${cleanEmail}_name", name)
        .putString("${cleanEmail}_photo", photoUrl ?: "")
        .putString("${cleanEmail}_provider", provider)
        .putString("${cleanEmail}_category", category)
        .putLong("${cleanEmail}_time", System.currentTimeMillis())
        .apply()
}

actual fun fetchLocalProfile(email: String): UserProfile? {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return null
    val cleanEmail = email.trim().lowercase()
    val name = prefs.getString("${cleanEmail}_name", null) ?: return null
    val photoUrl = prefs.getString("${cleanEmail}_photo", "")?.ifBlank { null }
    val provider = prefs.getString("${cleanEmail}_provider", "") ?: ""
    val category = prefs.getString("${cleanEmail}_category", "STUDENT") ?: "STUDENT"
    val time = prefs.getLong("${cleanEmail}_time", 0L)
    return UserProfile(
        email = cleanEmail,
        name = name,
        photoUrl = photoUrl,
        provider = provider,
        category = category,
        registeredAt = time,
        updatedAt = time,
    )
}

actual fun deleteLocalProfile(email: String) {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return
    val cleanEmail = email.trim().lowercase()
    prefs.edit()
        .remove("${cleanEmail}_name")
        .remove("${cleanEmail}_photo")
        .remove("${cleanEmail}_provider")
        .remove("${cleanEmail}_category")
        .remove("${cleanEmail}_time")
        .apply()
}

actual fun updateLocalCategory(email: String, category: String) {
    val prefs = getAppContext()?.getSharedPreferences("klass_local_db", Context.MODE_PRIVATE) ?: return
    val cleanEmail = email.trim().lowercase()
    prefs.edit()
        .putString("${cleanEmail}_category", category)
        .apply()
}
