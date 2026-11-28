package com.mpi.klass

import kotlinx.coroutines.CoroutineDispatcher

/** Background dispatcher for blocking work (Dispatchers.IO on Android, Default on the web). */
internal expect val IoDispatcher: CoroutineDispatcher

/** Runs [block] while holding [lock] on platforms with threads; plain call on single-threaded platforms. */
internal expect fun <T> klassSynchronized(lock: Any, block: () -> T): T
