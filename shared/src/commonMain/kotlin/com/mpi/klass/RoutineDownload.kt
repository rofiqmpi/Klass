package com.mpi.klass

expect suspend fun downloadRoutinePdf(
    label: String,
    shift: String,
    config: RoutineConfig,
    entries: List<RoutineEntry>,
): Result<String>
