package com.mpi.klass

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mpi.klass.theme.getAppContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@Suppress("MissingPermission")
actual suspend fun isNetworkAvailable(): Boolean {
    val context = getAppContext() ?: return checkRealInternetPing()
    val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
        ?: return checkRealInternetPing()
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false

    val hasCapability = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    return hasCapability && checkRealInternetPing()
}

private suspend fun checkRealInternetPing(): Boolean = withContext(Dispatchers.IO) {
    try {
        val result = withTimeoutOrNull(2200.milliseconds) {
            try {
                val url = URL("https://www.google.com/generate_204")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Android")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1800
                connection.readTimeout = 1800
                connection.connect()
                val responseCode = connection.responseCode
                connection.disconnect()
                (responseCode == 204) || (responseCode == 200)
            } catch (_: Exception) {
                false
            }
        }
        result == true
    } catch (_: Exception) {
        false
    }
}
