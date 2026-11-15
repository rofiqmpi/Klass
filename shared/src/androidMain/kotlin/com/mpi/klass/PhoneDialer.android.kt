package com.mpi.klass

import android.content.Intent
import android.net.Uri
import com.mpi.klass.theme.getAppContext

actual fun openPhoneDialer(phone: String) {
    val ctx = getAppContext() ?: return
    val cleaned = phone.trim().filter { it.isDigit() || it == '+' }
    if (cleaned.isBlank()) return
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleaned")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    } catch (_: Exception) {
    }
}

actual fun getTodayWorkingDayName(): String {
    return when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.SUNDAY -> "Sunday"
        java.util.Calendar.MONDAY -> "Monday"
        java.util.Calendar.TUESDAY -> "Tuesday"
        java.util.Calendar.WEDNESDAY -> "Wednesday"
        java.util.Calendar.THURSDAY -> "Thursday"
        java.util.Calendar.FRIDAY -> "Friday"
        java.util.Calendar.SATURDAY -> "Saturday"
        else -> "Sunday"
    }
}
