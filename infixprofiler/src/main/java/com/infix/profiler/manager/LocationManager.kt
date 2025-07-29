package com.infix.profiler.manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LocationResult(
    val lat: Double,
    val lng: Double,
    val accuracy: Float,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val provider: String,
    val timestamp: Long
)

object LocationManager {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocationResult? {
        return try {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                Log.w("InfixProfiler-Location", "Location permission not granted")
                return null
            }
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(
                                LocationResult(
                                    lat = location.latitude,
                                    lng = location.longitude,
                                    accuracy = location.accuracy,
                                    altitude = location.altitude,
                                    speed = location.speed,
                                    bearing = location.bearing,
                                    provider = location.provider ?: "unknown",
                                    timestamp = location.time
                                )
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        Log.e("InfixProfiler-Location", "Failed to get location: ${it.message}", it)
                        continuation.resume(null)
                    }
            }
        } catch (e: Exception) {
            Log.e("InfixProfiler-Location", "Exception getting location: ${e.message}", e)
            null
        }
    }
}