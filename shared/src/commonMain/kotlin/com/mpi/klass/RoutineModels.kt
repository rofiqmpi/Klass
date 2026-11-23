package com.mpi.klass

enum class TeacherTimePreference(val key: String, val label: String, val subtitle: String) {
    EARLY("EARLY", "Early Hours", "Prefers morning / first periods"),
    LATE("LATE", "Late Hours", "Prefers afternoon / later periods"),
    ANY("ANY", "Flexible (Any)", "Available during any shift period");

    companion object {
        fun fromRaw(raw: String?): TeacherTimePreference =
            entries.firstOrNull { it.key.equals(raw?.trim(), ignoreCase = true) } ?: ANY
    }
}

enum class ClassroomType(val key: String, val label: String) {
    THEORY("THEORY", "Theory Room"),
    LAB("LAB", "Lab / Workshop");

    companion object {
        fun fromRaw(raw: String?): ClassroomType =
            entries.firstOrNull { it.key.equals(raw?.trim(), ignoreCase = true) } ?: THEORY
    }
}

enum class EditRequestStatus(val key: String, val label: String) {
    PENDING("PENDING", "Pending"),
    APPROVED("APPROVED", "Approved"),
    DECLINED("DECLINED", "Declined");

    companion object {
        fun fromRaw(raw: String?): EditRequestStatus =
            entries.firstOrNull { it.key.equals(raw?.trim(), ignoreCase = true) } ?: PENDING
    }
}

data class TeacherSchedulingPref(
    val email: String = "",
    val name: String = "",
    val department: String = "",
    val shift: String = "",
    val timePreference: TeacherTimePreference = TeacherTimePreference.ANY,
    val semesterSubjects: Map<String, List<String>> = emptyMap(),
    val schedulingSetupComplete: Boolean = false,
    val updatedAt: Long = 0L,
)

data class ClassroomItem(
    val id: String = "",
    val roomNumber: String = "",
    val buildingOrFloor: String = "",
    val roomType: ClassroomType = ClassroomType.THEORY,
    val department: String = "ALL",
    val nearestDepartment: String = "",
    val nearestDepartments: List<String> = emptyList(),
    val shifts: List<String> = listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2),
    val capacity: Int = 40,
    val updatedAt: Long = 0L,
) {
    fun effectiveNearestDepartments(): List<String> {
        val raw = nearestDepartments.ifEmpty {
            nearestDepartment.split(",", "/").map { it.trim() }
        }
        return raw
            .map { RoutineRules.normalizeDepartmentShort(it) }
            .filter { it.isNotBlank() && it != "ALL" }
            .distinct()
    }
}

data class RoutineConfig(
    val shift1StartTime: String = "08:00",
    val shift2StartTime: String = "13:30",
    val periodDurationMinutes: Int = 45,
    val periodsPerDay: Int = 7,
    val activeSemesters: List<String> = RoutineRules.ALL_SEMESTER_KEYS,
    val validMonths: Int = 6,
)

data class RoutineEntry(
    val id: String = "",
    val day: String = "",
    val periodIndex: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val shift: String = "",
    val department: String = "",
    val semester: String = "",
    val group: String = "",
    val subjectCode: String = "",
    val subjectTitle: String = "",
    val isPractical: Boolean = false,
    val teacherEmail: String = "",
    val teacherName: String = "",
    val roomId: String = "",
    val roomNumber: String = "",
    val isManualOverride: Boolean = false,
)

data class RoutineConflict(
    val id: String = "",
    val severity: String = "WARNING",
    val department: String = "",
    val semester: String = "",
    val shift: String = "",
    val group: String = "",
    val subjectCode: String = "",
    val subjectTitle: String = "",
    val reason: String = "",
)

data class RoutinePlan(
    val id: String = "master_6m",
    val status: String = "DRAFT", // DRAFT or PUBLISHED
    val config: RoutineConfig = RoutineConfig(),
    val entries: List<RoutineEntry> = emptyList(),
    val conflicts: List<RoutineConflict> = emptyList(),
    val generatedAt: Long = 0L,
    val publishedAt: Long = 0L,
    val generatedBy: String = "",
)

data class RoutineEditRequest(
    val id: String = "",
    val requestedByEmail: String = "",
    val requestedByName: String = "",
    val targetEntryId: String = "",
    val department: String = "",
    val semester: String = "",
    val shift: String = "",
    val group: String = "",
    val subjectCode: String = "",
    val subjectTitle: String = "",
    val originalDay: String = "",
    val originalPeriod: Int = 0,
    val originalRoomNumber: String = "",
    val requestedDay: String = "",
    val requestedPeriod: Int = 0,
    val requestedRoomId: String = "",
    val requestedRoomNumber: String = "",
    val reason: String = "",
    val status: EditRequestStatus = EditRequestStatus.PENDING,
    val adminNote: String = "",
    val createdAt: Long = 0L,
    val resolvedAt: Long = 0L,
)

data class BloodDonorItem(
    val id: String = "",
    val name: String = "",
    val bloodGroup: String = "",
    val phone: String = "",
    val roleLabel: String = "",
    val department: String = "",
    val address: String = "",
    val email: String = "",
)

object RoutineRules {
    const val SHIFT_1 = "1st Shift"
    const val SHIFT_2 = "2nd Shift"

    val WORKING_DAYS = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")

    val TECH_DEPARTMENTS = listOf("CST", "CT", "ET", "MT", "PT", "ENT", "EMT")
    val ALL_TEACHER_DEPARTMENTS = TECH_DEPARTMENTS + "Non-Tech"

    // Canonical Bengali keys stored inside BtebBooksCatalog for semesters 1-7
    val ALL_SEMESTER_KEYS = listOf(
        "১ম সেমিস্টার",
        "২য় সেমিস্টার",
        "৩য় সেমিস্টার",
        "৪র্থ সেমিস্টার",
        "৫ম সেমিস্টার",
        "৬ষ্ঠ সেমিস্টার",
        "৭ম সেমিস্টার",
    )

    val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    fun semesterNumber(raw: String): Int? {
        val clean = AppStrings.convertBengaliDigitsToAscii(raw).trim().lowercase()
        if (clean.contains("1") || raw.contains("১")) return 1
        if (clean.contains("2") || raw.contains("২")) return 2
        if (clean.contains("3") || raw.contains("৩")) return 3
        if (clean.contains("4") || raw.contains("৪")) return 4
        if (clean.contains("5") || raw.contains("৫")) return 5
        if (clean.contains("6") || raw.contains("৬")) return 6
        if (clean.contains("7") || raw.contains("৭")) return 7
        return null
    }

    fun canonicalSemesterKey(raw: String): String {
        return when (semesterNumber(raw)) {
            1 -> "১ম সেমিস্টার"
            2 -> "২য় সেমিস্টার"
            3 -> "৩য় সেমিস্টার"
            4 -> "৪র্থ সেমিস্টার"
            5 -> "৫ম সেমিস্টার"
            6 -> "৬ষ্ঠ সেমিস্টার"
            7 -> "৭ম সেমিস্টার"
            else -> ""
        }
    }

    fun semesterDisplayLabel(raw: String): String {
        return when (semesterNumber(raw)) {
            1 -> "1st Semester"
            2 -> "2nd Semester"
            3 -> "3rd Semester"
            4 -> "4th Semester"
            5 -> "5th Semester"
            6 -> "6th Semester"
            7 -> "7th Semester"
            else -> raw.trim()
        }
    }

    fun semesterShortLabel(raw: String): String {
        return when (semesterNumber(raw)) {
            1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            4 -> "4th"
            5 -> "5th"
            6 -> "6th"
            7 -> "7th"
            else -> raw.trim()
        }
    }

    fun normalizeDepartmentShort(raw: String): String {
        val clean = raw.trim()
        if (clean.isBlank()) return ""
        return when {
            clean.equals("CST", true) || clean.startsWith("CST ", true) -> "CST"
            clean.equals("CT", true) || clean.startsWith("CT ", true) -> "CT"
            clean.equals("ET", true) || clean.startsWith("ET ", true) -> "ET"
            clean.equals("MT", true) || clean.startsWith("MT ", true) -> "MT"
            clean.equals("PT", true) || clean.startsWith("PT ", true) -> "PT"
            clean.equals("ENT", true) || clean.startsWith("ENT ", true) -> "ENT"
            clean.equals("EMT", true) || clean.startsWith("EMT ", true) -> "EMT"
            clean.contains("নন টেক", true) || clean.contains("নন-টেক", true) ||
                clean.equals("Non-Tech", true) || clean.equals("Non Tech", true) -> "Non-Tech"
            clean.equals("ALL", true) -> "ALL"
            else -> clean
        }
    }

    fun normalizeShift(raw: String): String {
        val clean = AppStrings.convertBengaliDigitsToAscii(raw).trim().lowercase()
        return when {
            clean.contains("2") || raw.contains("২") || clean.contains("afternoon") || clean.contains("evening") -> SHIFT_2
            clean.contains("1") || raw.contains("১") || clean.contains("morning") -> SHIFT_1
            else -> SHIFT_1
        }
    }

    /**
     * Strict MPI institute rule:
     * ONLY CT and ET have 3 groups (Group A, Group B, Group C).
     * All other 5 departments (CST, MT, PT, ENT, EMT) have 2 groups (Group A, Group B).
     */
    fun groupsForDepartment(department: String): List<String> {
        val short = normalizeDepartmentShort(department)
        return if (short == "CT" || short == "ET") {
            listOf("Group A", "Group B", "Group C")
        } else {
            listOf("Group A", "Group B")
        }
    }

    fun normalizeGroup(department: String, rawGroup: String): String {
        val allowed = groupsForDepartment(department)
        val clean = rawGroup.trim().uppercase()
        val letter = when {
            clean.endsWith("C") || clean == "C" -> "Group C"
            clean.endsWith("B") || clean == "B" -> "Group B"
            else -> "Group A"
        }
        return if (letter in allowed) letter else allowed.first()
    }

    fun parseMinutesFromHhMm(hhMm: String, fallbackMinutes: Int): Int {
        val parts = hhMm.trim().split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return fallbackMinutes
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        if (h !in 0..23 || m !in 0..59) return fallbackMinutes
        return h * 60 + m
    }

    fun formatMinutesToTime(totalMinutes: Int): String {
        val wrapped = ((totalMinutes % 1440) + 1440) % 1440
        val h24 = wrapped / 60
        val m = wrapped % 60
        val amPm = if (h24 >= 12) "PM" else "AM"
        val h12 = when (val mod = h24 % 12) {
            0 -> 12
            else -> mod
        }
        val mStr = m.toString().padStart(2, '0')
        return "$h12:$mStr $amPm"
    }

    fun periodTimeRange(shift: String, periodIndex: Int, config: RoutineConfig): Pair<String, String> {
        val normalizedShift = normalizeShift(shift)
        val baseStart = if (normalizedShift == SHIFT_2) {
            parseMinutesFromHhMm(config.shift2StartTime, 13 * 60 + 30)
        } else {
            parseMinutesFromHhMm(config.shift1StartTime, 8 * 60)
        }
        val dur = config.periodDurationMinutes.coerceIn(25, 90)
        val pStart = baseStart + (periodIndex * dur)
        val pEnd = pStart + dur
        return formatMinutesToTime(pStart) to formatMinutesToTime(pEnd)
    }

    fun dayOrderIndex(day: String): Int {
        val clean = day.trim()
        val idx = WORKING_DAYS.indexOfFirst { it.equals(clean, ignoreCase = true) }
        return if (idx >= 0) idx else 99
    }
}

// Repository Expect Declarations
expect suspend fun saveTeacherSchedulingPrefInFirestore(pref: TeacherSchedulingPref): Result<Unit>
expect suspend fun fetchTeacherSchedulingPrefFromFirestore(email: String): TeacherSchedulingPref?
expect suspend fun fetchAllTeacherSchedulingPrefsFromFirestore(): List<TeacherSchedulingPref>

expect suspend fun fetchClassroomsFromFirestore(): List<ClassroomItem>
expect suspend fun saveClassroomInFirestore(room: ClassroomItem): Result<Unit>
expect suspend fun deleteClassroomFromFirestore(roomId: String): Result<Unit>
expect suspend fun deleteAllClassroomsFromFirestore(): Result<Unit>

expect suspend fun saveRoutinePlanInFirestore(plan: RoutinePlan): Result<Unit>
expect suspend fun fetchRoutinePlanFromFirestore(docId: String = "master_6m"): RoutinePlan?

expect suspend fun submitRoutineEditRequestInFirestore(request: RoutineEditRequest): Result<Unit>
expect suspend fun fetchRoutineEditRequestsFromFirestore(): List<RoutineEditRequest>
expect suspend fun resolveRoutineEditRequestInFirestore(
    requestId: String,
    newStatus: EditRequestStatus,
    adminNote: String,
): Result<Unit>

expect suspend fun fetchBloodDonorsFromFirestore(): List<BloodDonorItem>
