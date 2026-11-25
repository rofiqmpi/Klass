package com.mpi.klass

data class ZedKnowledgeEntry(
    val id: String = "",
    val topic: String = "",
    val fact: String = "",
    val domain: String = "GENERAL",
    val authorRole: String = "USER",
    val verified: Boolean = true,
    val createdAt: Long = 0L,
)

enum class ZedActionType(val key: String) {
    OPEN_BOOKS("OPEN_BOOKS"),
    OPEN_BLOOD("OPEN_BLOOD"),
    OPEN_TEACHERS("OPEN_TEACHERS"),
    OPEN_CLASSROOMS("OPEN_CLASSROOMS"),
    OPEN_ROUTINE("OPEN_ROUTINE"),
    OPEN_NOTIFICATIONS("OPEN_NOTIFICATIONS"),
    COPY_TEXT("COPY_TEXT"),
    ASK_FOLLOWUP("ASK_FOLLOWUP");

    companion object {
        fun fromRaw(raw: String?): ZedActionType? =
            entries.firstOrNull { it.key.equals(raw?.trim(), ignoreCase = true) }
    }
}

data class ZedA2UiAction(
    val label: String,
    val actionType: ZedActionType,
    val dept: String = "",
    val semester: String = "",
    val bloodGroup: String = "",
    val query: String = "",
)

data class ZedContextSnapshot(
    val books: List<LibraryBook> = emptyList(),
    val teachers: List<TeacherDirectoryItem> = emptyList(),
    val teacherPrefs: List<TeacherSchedulingPref> = emptyList(),
    val classrooms: List<ClassroomItem> = emptyList(),
    val routinePlan: RoutinePlan? = null,
    val donors: List<BloodDonorItem> = emptyList(),
    val learnedKnowledge: List<ZedKnowledgeEntry> = emptyList(),
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
    val action: ZedA2UiAction? = null,
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

    data class ActionRow(
        val title: String = "Quick Actions",
        val actions: List<ZedA2UiAction>,
    ) : ZedA2UiBlock
}

data class ZedReply(
    val answerText: String,
    val sourceBadge: String,
    val isSupportedDomain: Boolean = true,
    val blocks: List<ZedA2UiBlock> = emptyList(),
    val learnedFact: ZedKnowledgeEntry? = null,
    val wasModerated: Boolean = false,
)

expect fun currentDayNameEnglish(): String

expect suspend fun fetchZedKnowledgeFromFirestore(): List<ZedKnowledgeEntry>

expect suspend fun saveZedKnowledgeInFirestore(entry: ZedKnowledgeEntry): Result<Unit>
