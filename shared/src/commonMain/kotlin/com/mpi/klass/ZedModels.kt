package com.mpi.klass

data class ZedContextSnapshot(
    val books: List<LibraryBook> = emptyList(),
    val teachers: List<TeacherDirectoryItem> = emptyList(),
    val teacherPrefs: List<TeacherSchedulingPref> = emptyList(),
    val classrooms: List<ClassroomItem> = emptyList(),
    val routinePlan: RoutinePlan? = null,
    val donors: List<BloodDonorItem> = emptyList(),
    val userProfile: StudentProfile? = null,
    val userFullProfile: UserProfile? = null,
    val userRole: UserRole? = null,
    val userDisplayName: String = "Friend",
)

data class ZedStatMetric(
    val label: String,
    val value: String,
)

data class ZedKeyValue(
    val label: String,
    val value: String,
)

data class ZedEntityItem(
    val primary: String,
    val secondary: String,
    val badge: String = "",
    val phone: String? = null,
)

data class ZedScheduleSlot(
    val dayAndPeriod: String,
    val timeRange: String,
    val subject: String,
    val detail: String,
    val isPractical: Boolean = false,
)

sealed interface ZedA2UiBlock {
    data class HeaderBanner(
        val title: String,
        val subtitle: String = "",
        val domain: String = "GENERAL",
    ) : ZedA2UiBlock

    data class StatRow(
        val stats: List<ZedStatMetric>,
    ) : ZedA2UiBlock

    data class KeyValueCard(
        val title: String,
        val items: List<ZedKeyValue>,
    ) : ZedA2UiBlock

    data class EntityListCard(
        val title: String,
        val entities: List<ZedEntityItem>,
    ) : ZedA2UiBlock

    data class ScheduleListCard(
        val title: String,
        val slots: List<ZedScheduleSlot>,
    ) : ZedA2UiBlock

    data class CalloutCard(
        val message: String,
        val isWarning: Boolean = false,
    ) : ZedA2UiBlock
}

data class ZedReply(
    val answerText: String,
    val sourceBadge: String,
    val isSupportedDomain: Boolean = true,
    val blocks: List<ZedA2UiBlock> = emptyList(),
)

expect fun currentDayNameEnglish(): String
