package com.mpi.klass

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mpi.klass.theme.getAppContext

@Suppress("MissingPermission")
actual suspend fun isNetworkAvailable(): Boolean {
    val context = getAppContext() ?: return true
    val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
        ?: return true
    val activeNetwork = cm.activeNetwork ?: return true
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return true
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
