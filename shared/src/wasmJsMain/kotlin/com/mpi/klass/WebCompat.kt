package com.mpi.klass

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val IoDispatcher: CoroutineDispatcher = Dispatchers.Default

internal actual fun <T> klassSynchronized(lock: Any, block: () -> T): T = block()
