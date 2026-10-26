package com.mpi.klass

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

actual suspend fun isNetworkAvailable(): Boolean = withContext(Dispatchers.Default) {
    val url = NSURL.URLWithString("https://www.google.com/generate_204")
        ?: return@withContext false
    val data = NSData.dataWithContentsOfURL(url)
    data != null && data.length > 0u
}
