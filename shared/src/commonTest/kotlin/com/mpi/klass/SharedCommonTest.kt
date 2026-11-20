package com.mpi.klass

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun quickFilterFormatsBadgeAndNormalizesGroups() {
        val cstFilter = RoutineQuickFilter(
            semesterKey = "৬ষ্ঠ সেমিস্টার",
            shift = RoutineRules.SHIFT_2,
            department = "CST",
            group = "Group C",
        )
        assertEquals("6/2/CST-A", cstFilter.toBadgeText())
        assertEquals(listOf("Group A", "Group B"), RoutineRules.groupsForDepartment("CST"))
        assertEquals(listOf("Group A", "Group B", "Group C"), RoutineRules.groupsForDepartment("CT"))
        assertEquals(listOf("Group A", "Group B", "Group C"), RoutineRules.groupsForDepartment("ET"))
    }
}