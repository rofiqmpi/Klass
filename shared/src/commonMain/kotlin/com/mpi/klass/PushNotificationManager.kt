package com.mpi.klass

import androidx.compose.runtime.Composable

data class NotificationPrefs(
    val enabled: Boolean = true,
    val reminderMinutesBefore: Int = 10,
    val notifyAtClassStart: Boolean = true,
)

data class ScheduledClassAlert(
    val id: String,
    val requestCode: Int,
    val entryId: String,
    val subjectTitle: String,
    val subjectCode: String,
    val day: String,
    val periodIndex: Int,
    val classTimeRange: String,
    val triggerTimeLabel: String,
    val roomNumber: String,
    val metaSubtitle: String,
    val triggerTitle: String,
    val triggerBody: String,
    val nextTriggerMillis: Long,
    val isPreClassReminder: Boolean,
)

internal object PushScheduleMath {

    fun parseHourMinute24(raw: String): Pair<Int, Int>? {
        val clean = raw.trim().uppercase()
        if (clean.isBlank()) return null
        val isPm = clean.endsWith("PM")
        val isAm = clean.endsWith("AM")
        val timePart = clean.removeSuffix("AM").removeSuffix("PM").trim()
        val parts = timePart.split(":")
        if (parts.size < 2) return null
        val rawHour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: return null
        val minute = parts[1].filter { it.isDigit() }.toIntOrNull() ?: return null
        if (minute !in 0..59) return null

        val hour24 = when {
            isPm && rawHour in 1..11 -> rawHour + 12
            isAm && rawHour == 12 -> 0
            isAm && rawHour in 1..11 -> rawHour
            isPm && rawHour == 12 -> 12
            rawHour in 0..23 -> rawHour
            else -> return null
        }
        return hour24 to minute
    }

    fun format12Hour(hour24: Int, minute: Int): String {
        val hMod = ((hour24 % 24) + 24) % 24
        val period = if (hMod >= 12) "PM" else "AM"
        val h12 = when {
            hMod == 0 -> 12
            hMod > 12 -> hMod - 12
            else -> hMod
        }
        val mStr = minute.toString().padStart(2, '0')
        return "$h12:$mStr $period"
    }

    fun subtractMinutes(hour24: Int, minute: Int, deltaMinutes: Int): Pair<Int, Int> {
        val total = ((hour24 * 60 + minute - deltaMinutes) % 1440 + 1440) % 1440
        return (total / 60) to (total % 60)
    }

    fun resolveClassStartTime24(entry: RoutineEntry, config: RoutineConfig): Pair<Int, Int> {
        parseHourMinute24(entry.startTime)?.let { return it }
        val (computedStart, _) = RoutineRules.periodTimeRange(entry.shift, entry.periodIndex, config)
        return parseHourMinute24(computedStart) ?: (8 to 0)
    }

    fun formatCountdownUntil(triggerMillis: Long, nowMillis: Long): String {
        val diffMs = (triggerMillis - nowMillis).coerceAtLeast(0L)
        val totalMinutes = diffMs / 60_000L
        return when {
            totalMinutes < 1L -> "Starting now"
            totalMinutes < 60L -> "In ${totalMinutes}m"
            totalMinutes < 24L * 60L -> {
                val h = totalMinutes / 60L
                val m = totalMinutes % 60L
                if (m == 0L) "In ${h}h" else "In ${h}h ${m}m"
            }
            else -> {
                val d = totalMinutes / (24L * 60L)
                val h = (totalMinutes % (24L * 60L)) / 60L
                if (h == 0L) "In ${d}d" else "In ${d}d ${h}h"
            }
        }
    }

    fun filterUserEntriesForAlerts(
        plan: RoutinePlan?,
        role: UserRole?,
        email: String,
        studentProfile: StudentProfile?,
    ): List<RoutineEntry> {
        val allEntries = plan?.entries ?: return emptyList()
        val cleanEmail = email.trim().lowercase()
        return when (role) {
            UserRole.STUDENT -> {
                val sp = studentProfile ?: return emptyList()
                if (sp.department.isBlank() || sp.semester.isBlank()) return emptyList()
                val dept = RoutineRules.normalizeDepartmentShort(sp.department)
                val shift = RoutineRules.normalizeShift(sp.shift)
                val sem = RoutineRules.canonicalSemesterKey(sp.semester)
                val grp = RoutineRules.normalizeGroup(dept, sp.section)
                allEntries.filter {
                    it.department == dept &&
                        RoutineRules.normalizeShift(it.shift) == shift &&
                        RoutineRules.canonicalSemesterKey(it.semester) == sem &&
                        RoutineRules.normalizeGroup(dept, it.group) == grp
                }
            }
            UserRole.TEACHER -> {
                if (cleanEmail.isBlank()) return emptyList()
                allEntries.filter { it.teacherEmail.trim().lowercase() == cleanEmail }
            }
            UserRole.ADMIN -> {
                allEntries.filter {
                    it.department == "CST" &&
                        RoutineRules.normalizeShift(it.shift) == RoutineRules.SHIFT_1 &&
                        RoutineRules.canonicalSemesterKey(it.semester) == RoutineRules.ALL_SEMESTER_KEYS.first() &&
                        RoutineRules.normalizeGroup("CST", it.group) == "Group A"
                }
            }
            null -> emptyList()
        }.sortedWith(
            compareBy<RoutineEntry> { RoutineRules.dayOrderIndex(it.day) }
                .thenBy { it.periodIndex }
        )
    }
}

expect fun hasNotificationPermission(): Boolean

@Composable
expect fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit

expect fun getNotificationPrefs(): NotificationPrefs

expect fun saveNotificationPrefs(prefs: NotificationPrefs)

expect fun sendWelcomePushNotification(userName: String, roleLabel: String)

expect fun sendInstantTestPushNotification(title: String, body: String)

expect fun syncAutomaticClassPushNotifications(
    entries: List<RoutineEntry>,
    config: RoutineConfig,
    role: UserRole?,
    prefs: NotificationPrefs = getNotificationPrefs(),
): List<ScheduledClassAlert>

expect fun cancelAllClassPushNotifications()
