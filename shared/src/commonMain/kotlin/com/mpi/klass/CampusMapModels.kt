package com.mpi.klass

import kotlin.math.hypot

data class DepartmentPoint(
    val department: String = "",
    val xMeters: Float = 0f,
    val yMeters: Float = 0f,
    val headingDegrees: Float = 0f,
    val updatedAt: Long = 0L,
)

data class CampusMapConfig(
    val departmentPoints: Map<String, DepartmentPoint> = emptyMap(),
    val originSet: Boolean = false,
    val updatedAt: Long = 0L,
) {
    fun pointFor(department: String): DepartmentPoint? {
        val norm = RoutineRules.normalizeDepartmentShort(department)
        if (norm.isBlank() || norm == "ALL") return null
        return departmentPoints[norm]
            ?: departmentPoints.entries.firstOrNull { (k, _) ->
                RoutineRules.normalizeDepartmentShort(k) == norm
            }?.value
    }

    /**
     * Computes Euclidean distance in meters between two departments using pinned campus coordinates.
     * - Same normalized department returns 0.0.
     * - "ALL" or unmapped rooms return a larger fallback distance so exact or nearby mapped rooms rank first.
     */
    fun distanceBetween(deptA: String, deptB: String): Double {
        val normA = RoutineRules.normalizeDepartmentShort(deptA)
        val normB = RoutineRules.normalizeDepartmentShort(deptB)
        if (normA.isNotBlank() && normB.isNotBlank() && normA == normB) return 0.0
        if (normA.isBlank() || normB.isBlank() || normA == "ALL" || normB == "ALL") {
            return 85.0
        }
        val pA = pointFor(normA)
        val pB = pointFor(normB)
        if (pA == null || pB == null) return 120.0
        return hypot(
            (pA.xMeters - pB.xMeters).toDouble(),
            (pA.yMeters - pB.yMeters).toDouble(),
        )
    }
}

expect suspend fun saveCampusMapConfigInFirestore(config: CampusMapConfig): Result<Unit>
expect suspend fun fetchCampusMapConfigFromFirestore(): CampusMapConfig
