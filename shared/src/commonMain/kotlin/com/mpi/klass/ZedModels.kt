package com.mpi.klass

data class ZedContextSnapshot(
    val books: List<LibraryBook> = emptyList(),
    val teachers: List<TeacherDirectoryItem> = emptyList(),
    val routinePlan: RoutinePlan? = null,
    val donors: List<BloodDonorItem> = emptyList(),
    val userProfile: StudentProfile? = null,
    val userRole: UserRole? = null,
    val userDisplayName: String = "Friend",
)

data class ZedReply(
    val answerText: String,
    val sourceBadge: String,
    val isSupportedDomain: Boolean = true,
    val suggestedFollowUps: List<String> = emptyList(),
)

expect fun currentDayNameEnglish(): String
