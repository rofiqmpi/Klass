package com.mpi.klass

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val IoDispatcher: CoroutineDispatcher = Dispatchers.IO

internal actual fun <T> klassSynchronized(lock: Any, block: () -> T): T = synchronized(lock, block)
