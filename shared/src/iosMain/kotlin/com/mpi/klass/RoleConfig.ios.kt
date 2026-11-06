package com.mpi.klass

import platform.Foundation.NSUserDefaults

actual fun saveLocalProfile(email: String, name: String, photoUrl: String?, provider: String, category: String) {
    val defaults = NSUserDefaults.standardUserDefaults
    val cleanEmail = email.trim().lowercase()
    defaults.setObject(name, forKey = "${cleanEmail}_name")
    defaults.setObject(photoUrl ?: "", forKey = "${cleanEmail}_photo")
    defaults.setObject(provider, forKey = "${cleanEmail}_provider")
    defaults.setObject(category, forKey = "${cleanEmail}_category")
}

actual fun fetchLocalProfile(email: String): UserProfile? {
    val defaults = NSUserDefaults.standardUserDefaults
    val cleanEmail = email.trim().lowercase()
    val name = defaults.stringForKey("${cleanEmail}_name") ?: return null
    val photoUrl = defaults.stringForKey("${cleanEmail}_photo")?.ifBlank { null }
    val provider = defaults.stringForKey("${cleanEmail}_provider") ?: ""
    val category = defaults.stringForKey("${cleanEmail}_category") ?: "STUDENT"
    return UserProfile(
        email = cleanEmail,
        name = name,
        photoUrl = photoUrl,
        provider = provider,
        category = category,
    )
}

actual fun deleteLocalProfile(email: String) {
    val defaults = NSUserDefaults.standardUserDefaults
    val cleanEmail = email.trim().lowercase()
    defaults.removeObjectForKey("${cleanEmail}_name")
    defaults.removeObjectForKey("${cleanEmail}_photo")
    defaults.removeObjectForKey("${cleanEmail}_provider")
    defaults.removeObjectForKey("${cleanEmail}_category")
}

actual fun updateLocalCategory(email: String, category: String) {
    val defaults = NSUserDefaults.standardUserDefaults
    val cleanEmail = email.trim().lowercase()
    defaults.setObject(category, forKey = "${cleanEmail}_category")
}
