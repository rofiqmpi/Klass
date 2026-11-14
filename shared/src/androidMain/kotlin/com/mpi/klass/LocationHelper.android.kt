package com.mpi.klass

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
actual fun rememberLocationPermissionLauncher(onLocationFetched: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val latestCallback = rememberUpdatedState(onLocationFetched)
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val activeListenerHolder = remember { arrayOfNulls<LocationListener>(1) }

    DisposableEffect(context) {
        onDispose {
            val existingListener = activeListenerHolder[0]
            if (existingListener != null) {
                try {
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    lm?.removeUpdates(existingListener)
                } catch (_: Exception) {}
                activeListenerHolder[0] = null
            }
        }
    }

    val startLocationSync = remember(context) {
        {
            fetchRealAndroidDeviceLocation(
                context = context,
                mainHandler = mainHandler,
                activeListenerHolder = activeListenerHolder,
                onLocationFetched = { addr -> latestCallback.value(addr) },
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            startLocationSync()
        } else {
            latestCallback.value("")
        }
    }

    return {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            startLocationSync()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

private fun fetchRealAndroidDeviceLocation(
    context: Context,
    mainHandler: Handler,
    activeListenerHolder: Array<LocationListener?>,
    onLocationFetched: (String) -> Unit,
) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onLocationFetched("")
            return
        }

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            onLocationFetched("")
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        fun dispatchLocation(location: Location) {
            CoroutineScope(Dispatchers.IO).launch {
                val addressString = geocodeLocation(context, location.latitude, location.longitude)
                if (addressString.isNotBlank()) {
                    mainHandler.post {
                        onLocationFetched(addressString)
                    }
                }
            }
        }

        // Remove any previously registered listener before registering a fresh one
        activeListenerHolder[0]?.let { prev ->
            try {
                locationManager.removeUpdates(prev)
            } catch (_: Exception) {}
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                dispatchLocation(location)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
            override fun onProviderEnabled(p0: String) {}
            override fun onProviderDisabled(p0: String) {}
        }
        activeListenerHolder[0] = listener

        if (isGpsEnabled) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, listener, Looper.getMainLooper())
            } catch (_: Exception) {}
        }
        if (isNetworkEnabled) {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10f, listener, Looper.getMainLooper())
            } catch (_: Exception) {}
        }

        val gpsLast = if (isGpsEnabled) try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null } else null
        val netLast = if (isNetworkEnabled) try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null } else null

        val bestLast = when {
            gpsLast != null && netLast != null -> if (gpsLast.time >= netLast.time) gpsLast else netLast
            gpsLast != null -> gpsLast
            else -> netLast
        }

        if (bestLast != null) {
            dispatchLocation(bestLast)
        } else {
            onLocationFetched("")
        }
    } catch (_: Exception) {
        onLocationFetched("")
    }
}

private fun geocodeLocation(context: Context, lat: Double, lon: Double): String {
    val activeLang = loadAppLanguage()
    val gpsFallback = "${"জিপিএস লোকেশন".localized(activeLang)} (${lat.toString().take(7)}°, ${lon.toString().take(7)}°)"
    return try {
        val locale = if (activeLang == AppLanguage.BENGALI) Locale("bn", "BD") else Locale.ENGLISH
        val geocoder = Geocoder(context, locale)
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            val line = addr.getAddressLine(0)
            if (!line.isNullOrBlank()) {
                line
            } else {
                val subLoc = addr.subLocality ?: addr.locality ?: ""
                val admin = addr.adminArea ?: ""
                val country = addr.countryName ?: ""
                listOf(subLoc, admin, country).filter { it.isNotBlank() }.joinToString(", ")
            }
        } else {
            gpsFallback
        }
    } catch (_: Exception) {
        gpsFallback
    }
}
