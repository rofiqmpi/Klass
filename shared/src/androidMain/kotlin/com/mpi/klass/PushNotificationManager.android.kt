package com.mpi.klass

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mpi.klass.theme.getAppContext
import java.util.Calendar
import org.json.JSONArray
import org.json.JSONObject

private const val PUSH_TAG = "MPIKlassPush"
private const val PREFS_NAME = "klass_push_prefs_v1"
private const val KEY_ENABLED = "push_enabled"
private const val KEY_REMINDER_MINS = "reminder_minutes_before"
private const val KEY_NOTIFY_START = "notify_at_class_start"
private const val KEY_ACTIVE_ALARMS = "active_alarms_json"
private const val KEY_SEEN_NOTIFICATION_IDS = "seen_notification_ids"

const val CHANNEL_WELCOME = "klass_welcome_channel"
const val CHANNEL_CLASS_ALERTS = "klass_class_alerts_channel"

private const val EXTRA_ALARM_ID = "alarm_id"
private const val EXTRA_REQUEST_CODE = "request_code"
private const val EXTRA_TITLE = "title"
private const val EXTRA_BODY = "body"
private const val EXTRA_NEXT_TRIGGER = "next_trigger_millis"
private const val EXTRA_CHANNEL_ID = "channel_id"

private const val WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L

fun ensurePushNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val welcomeChannel = NotificationChannel(
            CHANNEL_WELCOME,
            "Welcome & Sign-In Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Instant push notification when you sign in to MPI Klass."
            enableVibration(true)
        }

        val classChannel = NotificationChannel(
            CHANNEL_CLASS_ALERTS,
            "Automatic Class Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Automatic scheduled push alerts before and at the start of every class."
            enableVibration(true)
        }

        manager.createNotificationChannel(welcomeChannel)
        manager.createNotificationChannel(classChannel)
    }
}

private fun resolveSmallIconRes(context: Context): Int {
    val statId = context.resources.getIdentifier("ic_stat_klass", "drawable", context.packageName)
    if (statId != 0) return statId
    val splogId = context.resources.getIdentifier("ic_splog", "drawable", context.packageName)
    if (splogId != 0) return splogId
    return android.R.drawable.ic_popup_reminder
}

private fun hasSystemNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

fun postSystemNotification(
    context: Context,
    notificationId: Int,
    channelId: String,
    title: String,
    body: String,
) {
    ensurePushNotificationChannels(context)
    if (!hasSystemNotificationPermission(context)) return

    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = if (launchIntent != null) {
        PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    } else {
        null
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(resolveSmallIconRes(context))
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    if (contentPendingIntent != null) {
        builder.setContentIntent(contentPendingIntent)
    }

    try {
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (sec: SecurityException) {
        Log.w(PUSH_TAG, "Notification permission rejected at dispatch: ${sec.message}")
    } catch (e: Exception) {
        Log.w(PUSH_TAG, "Failed posting notification: ${e.message}")
    }
}

private fun scheduleAlarmOnSystem(
    context: Context,
    alarmId: String,
    requestCode: Int,
    title: String,
    body: String,
    triggerAtMillis: Long,
    channelId: String,
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
        data = Uri.parse("klass://class_alarm/$alarmId")
        putExtra(EXTRA_ALARM_ID, alarmId)
        putExtra(EXTRA_REQUEST_CODE, requestCode)
        putExtra(EXTRA_TITLE, title)
        putExtra(EXTRA_BODY, body)
        putExtra(EXTRA_NEXT_TRIGGER, triggerAtMillis)
        putExtra(EXTRA_CHANNEL_ID, channelId)
    }
    val pi = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    try {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
    } catch (e: Exception) {
        Log.w(PUSH_TAG, "Failed scheduling alarm $alarmId: ${e.message}")
    }
}

private fun cancelAlarmOnSystem(context: Context, alarmId: String, requestCode: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
        data = Uri.parse("klass://class_alarm/$alarmId")
    }
    val pi = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
    )
    if (pi != null) {
        alarmManager.cancel(pi)
        pi.cancel()
    }
}

class ClassAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, alarmId.hashCode())
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Class Reminder"
        val body = intent.getStringExtra(EXTRA_BODY) ?: "Your scheduled class is coming up."
        val triggerAtMillis = intent.getLongExtra(EXTRA_NEXT_TRIGGER, System.currentTimeMillis())
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: CHANNEL_CLASS_ALERTS

        postSystemNotification(
            context = context,
            notificationId = requestCode,
            channelId = channelId,
            title = title,
            body = body,
        )

        // Automatically roll alarm forward +7 days for continuous weekly notifications
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, true)) return

        val now = System.currentTimeMillis()
        var nextTrigger = triggerAtMillis + WEEK_MILLIS
        while (nextTrigger <= now + 60_000L) {
            nextTrigger += WEEK_MILLIS
        }

        scheduleAlarmOnSystem(
            context = context,
            alarmId = alarmId,
            requestCode = requestCode,
            title = title,
            body = body,
            triggerAtMillis = nextTrigger,
            channelId = channelId,
        )
        updateStoredAlarmTrigger(context, alarmId, nextTrigger)
    }
}

class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        ensurePushNotificationChannels(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, true)) return

        val raw = prefs.getString(KEY_ACTIVE_ALARMS, null) ?: return
        val arr = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return
        }
        val now = System.currentTimeMillis()
        val updatedArr = JSONArray()

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val alarmId = obj.optString("id")
            if (alarmId.isBlank()) continue
            val reqCode = obj.optInt("request_code")
            val title = obj.optString("title")
            val body = obj.optString("body")
            val channelId = obj.optString("channel_id", CHANNEL_CLASS_ALERTS)
            var trigger = obj.optLong("next_trigger_millis")
            if (trigger <= 0L) continue
            while (trigger <= now + 5_000L) {
                trigger += WEEK_MILLIS
            }
            scheduleAlarmOnSystem(context, alarmId, reqCode, title, body, trigger, channelId)
            obj.put("next_trigger_millis", trigger)
            updatedArr.put(obj)
        }
        prefs.edit().putString(KEY_ACTIVE_ALARMS, updatedArr.toString()).apply()
    }
}

private fun updateStoredAlarmTrigger(context: Context, alarmId: String, nextTrigger: Long) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_ACTIVE_ALARMS, null) ?: return
    val arr = try {
        JSONArray(raw)
    } catch (_: Exception) {
        return
    }
    var changed = false
    for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        if (obj.optString("id") == alarmId) {
            obj.put("next_trigger_millis", nextTrigger)
            changed = true
            break
        }
    }
    if (changed) {
        prefs.edit().putString(KEY_ACTIVE_ALARMS, arr.toString()).apply()
    }
}

actual fun hasNotificationPermission(): Boolean {
    val ctx = getAppContext() ?: return false
    return hasSystemNotificationPermission(ctx)
}

@Composable
actual fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    val latestCallback = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        latestCallback.value(granted)
    }

    return {
        val ctx = getAppContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ctx != null && hasSystemNotificationPermission(ctx)) {
                latestCallback.value(true)
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            val enabled = ctx?.let { NotificationManagerCompat.from(it).areNotificationsEnabled() } ?: true
            latestCallback.value(enabled)
        }
    }
}

actual fun getNotificationPrefs(): NotificationPrefs {
    val ctx = getAppContext() ?: return NotificationPrefs()
    val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return NotificationPrefs(
        enabled = prefs.getBoolean(KEY_ENABLED, true),
        reminderMinutesBefore = prefs.getInt(KEY_REMINDER_MINS, 10),
        notifyAtClassStart = prefs.getBoolean(KEY_NOTIFY_START, true),
    )
}

actual fun saveNotificationPrefs(prefs: NotificationPrefs) {
    val ctx = getAppContext() ?: return
    ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_ENABLED, prefs.enabled)
        .putInt(KEY_REMINDER_MINS, prefs.reminderMinutesBefore)
        .putBoolean(KEY_NOTIFY_START, prefs.notifyAtClassStart)
        .apply()
}

actual fun getSeenNotificationIds(): Set<String> {
    val ctx = getAppContext() ?: return emptySet()
    val raw = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getStringSet(KEY_SEEN_NOTIFICATION_IDS, null)
    return raw?.toSet() ?: emptySet()
}

actual fun saveSeenNotificationIds(ids: Set<String>) {
    val ctx = getAppContext() ?: return
    ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_SEEN_NOTIFICATION_IDS, HashSet(ids))
        .apply()
}

actual fun sendWelcomePushNotification(userName: String, roleLabel: String) {
    val ctx = getAppContext() ?: return
    val cleanName = userName.trim().ifBlank { "Member" }
    val cleanRole = roleLabel.trim().ifBlank { "User" }
    val title = "Welcome to MPI Klass, $cleanName!"
    val body = "Signed in as $cleanRole • Automatic push reminders for your classes are active."
    postSystemNotification(
        context = ctx,
        notificationId = 900_001,
        channelId = CHANNEL_WELCOME,
        title = title,
        body = body,
    )
}

actual fun sendInstantTestPushNotification(title: String, body: String) {
    val ctx = getAppContext() ?: return
    postSystemNotification(
        context = ctx,
        notificationId = 900_002,
        channelId = CHANNEL_CLASS_ALERTS,
        title = title,
        body = body,
    )
}

private fun calendarDayForName(dayName: String): Int? = when (dayName.trim().lowercase()) {
    "sunday" -> Calendar.SUNDAY
    "monday" -> Calendar.MONDAY
    "tuesday" -> Calendar.TUESDAY
    "wednesday" -> Calendar.WEDNESDAY
    "thursday" -> Calendar.THURSDAY
    "friday" -> Calendar.FRIDAY
    "saturday" -> Calendar.SATURDAY
    else -> null
}

private fun nextOccurrenceEpochMillis(
    dayName: String,
    startHour24: Int,
    startMinute: Int,
    minutesBefore: Int,
    nowMillis: Long,
): Long? {
    val calDay = calendarDayForName(dayName) ?: return null
    val cal = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.DAY_OF_WEEK, calDay)
        set(Calendar.HOUR_OF_DAY, startHour24)
        set(Calendar.MINUTE, startMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MINUTE, -minutesBefore)
    }
    if (cal.timeInMillis <= nowMillis + 5_000L) {
        cal.add(Calendar.DAY_OF_YEAR, 7)
    }
    return cal.timeInMillis
}

actual fun syncAutomaticClassPushNotifications(
    entries: List<RoutineEntry>,
    config: RoutineConfig,
    role: UserRole?,
    prefs: NotificationPrefs,
): List<ScheduledClassAlert> {
    val ctx = getAppContext() ?: return emptyList()
    ensurePushNotificationChannels(ctx)

    // Cancel any previously armed alarms before rebuilding cleanly
    cancelAllClassPushNotificationsInternal(ctx)

    val now = System.currentTimeMillis()
    val alerts = mutableListOf<ScheduledClassAlert>()

    for (entry in entries) {
        val (startH, startM) = PushScheduleMath.resolveClassStartTime24(entry, config)
        val computedRange = RoutineRules.periodTimeRange(entry.shift, entry.periodIndex, config)
        val timeRange = if (entry.startTime.isNotBlank() && entry.endTime.isNotBlank()) {
            "${entry.startTime} – ${entry.endTime}"
        } else {
            "${computedRange.first} – ${computedRange.second}"
        }
        val roomLabel = entry.roomNumber.ifBlank { "TBA" }
        val meta = if (role == UserRole.TEACHER) {
            "${entry.department} • ${RoutineRules.semesterShortLabel(entry.semester)} • ${entry.group} • ${entry.shift}"
        } else {
            "${entry.teacherName.ifBlank { "Faculty" }} • ${entry.shift}"
        }

        if (prefs.reminderMinutesBefore > 0) {
            val triggerMillis = nextOccurrenceEpochMillis(
                dayName = entry.day,
                startHour24 = startH,
                startMinute = startM,
                minutesBefore = prefs.reminderMinutesBefore,
                nowMillis = now,
            )
            if (triggerMillis != null) {
                val (trigH, trigM) = PushScheduleMath.subtractMinutes(startH, startM, prefs.reminderMinutesBefore)
                val alarmId = "pre_${entry.id}_${entry.day}_${entry.periodIndex}"
                val reqCode = (alarmId.hashCode() and 0x0FFFFFFF) + 10_000
                alerts += ScheduledClassAlert(
                    id = alarmId,
                    requestCode = reqCode,
                    entryId = entry.id,
                    subjectTitle = entry.subjectTitle,
                    subjectCode = entry.subjectCode,
                    day = entry.day,
                    periodIndex = entry.periodIndex,
                    classTimeRange = timeRange,
                    triggerTimeLabel = "${entry.day.take(3)} • ${PushScheduleMath.format12Hour(trigH, trigM)}",
                    roomNumber = roomLabel,
                    metaSubtitle = meta,
                    triggerTitle = "Class in ${prefs.reminderMinutesBefore} min • ${entry.subjectTitle}",
                    triggerBody = "Period ${entry.periodIndex + 1} ($timeRange) • Room $roomLabel • $meta",
                    nextTriggerMillis = triggerMillis,
                    isPreClassReminder = true,
                )
            }
        }

        if (prefs.notifyAtClassStart) {
            val triggerMillis = nextOccurrenceEpochMillis(
                dayName = entry.day,
                startHour24 = startH,
                startMinute = startM,
                minutesBefore = 0,
                nowMillis = now,
            )
            if (triggerMillis != null) {
                val alarmId = "start_${entry.id}_${entry.day}_${entry.periodIndex}"
                val reqCode = (alarmId.hashCode() and 0x0FFFFFFF) + 20_000
                alerts += ScheduledClassAlert(
                    id = alarmId,
                    requestCode = reqCode,
                    entryId = entry.id,
                    subjectTitle = entry.subjectTitle,
                    subjectCode = entry.subjectCode,
                    day = entry.day,
                    periodIndex = entry.periodIndex,
                    classTimeRange = timeRange,
                    triggerTimeLabel = "${entry.day.take(3)} • ${PushScheduleMath.format12Hour(startH, startM)}",
                    roomNumber = roomLabel,
                    metaSubtitle = meta,
                    triggerTitle = "Class Starting Now • ${entry.subjectTitle}",
                    triggerBody = "Room $roomLabel • $timeRange • $meta",
                    nextTriggerMillis = triggerMillis,
                    isPreClassReminder = false,
                )
            }
        }
    }

    val sortedAlerts = alerts.sortedBy { it.nextTriggerMillis }

    if (prefs.enabled) {
        val storedArr = JSONArray()
        for (a in sortedAlerts) {
            scheduleAlarmOnSystem(
                context = ctx,
                alarmId = a.id,
                requestCode = a.requestCode,
                title = a.triggerTitle,
                body = a.triggerBody,
                triggerAtMillis = a.nextTriggerMillis,
                channelId = CHANNEL_CLASS_ALERTS,
            )
            storedArr.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("request_code", a.requestCode)
                    put("title", a.triggerTitle)
                    put("body", a.triggerBody)
                    put("next_trigger_millis", a.nextTriggerMillis)
                    put("channel_id", CHANNEL_CLASS_ALERTS)
                }
            )
        }
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACTIVE_ALARMS, storedArr.toString())
            .apply()
    }

    return sortedAlerts
}

actual fun cancelAllClassPushNotifications() {
    val ctx = getAppContext() ?: return
    cancelAllClassPushNotificationsInternal(ctx)
}

private fun cancelAllClassPushNotificationsInternal(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_ACTIVE_ALARMS, null)
    if (!raw.isNullOrBlank()) {
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                val reqCode = obj.optInt("request_code")
                if (id.isNotBlank()) {
                    cancelAlarmOnSystem(context, id, reqCode)
                }
            }
        } catch (_: Exception) {}
    }
    prefs.edit().remove(KEY_ACTIVE_ALARMS).apply()
}

