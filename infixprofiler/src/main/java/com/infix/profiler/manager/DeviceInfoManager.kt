package com.infix.profiler.manager

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.infix.profiler.model.DeviceInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.app.ActivityManager
import android.view.WindowInsets
import android.view.WindowManager

class DeviceInfoManager {
    fun getDeviceInfo(context: Context): DeviceInfo {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val metrics = context.resources.displayMetrics
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
            sdf.timeZone = TimeZone.getDefault()
            val timestamp = sdf.format(Date(now))

            // Memory info
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalMemory = memInfo.totalMem
            val usedMemory = totalMemory - memInfo.availMem

            // Notch detection (simple heuristic)
            val hasNotch = try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                val windowInsets = windowManager?.currentWindowMetrics?.windowInsets
                windowInsets?.displayCutout != null
            } catch (e: Exception) {
                false
            }

            DeviceInfo(
                deviceId = androidId ?: "unknown_device_id",
                brand = Build.BRAND,
                model = Build.MODEL,
                systemName = "Android",
                systemVersion = Build.VERSION.RELEASE ?: "Unknown",
                appVersion = packageInfo.versionName ?: "Unknown",
                buildNumber = packageInfo.versionCode.toString(),
                packageName = packageName,
                manufacturer = Build.MANUFACTURER,
                deviceName = Build.DEVICE,
                deviceType = if (metrics.widthPixels >= 600) "Tablet" else "Phone",
                totalMemory = totalMemory,
                usedMemory = usedMemory,
                isTablet = metrics.widthPixels >= 600,
                adId = null, // Set by SDK
                androidId = androidId,
                network = null, // Set by SDK
                location = null, // Set by SDK
                timestamp = timestamp,
                timezone = TimeZone.getDefault().id,
                platform = "android",
                hasNotch = hasNotch,
                hasDynamicIsland = false // Not available on Android
            )
        } catch (e: Exception) {
            e.printStackTrace()
            DeviceInfo(
                deviceId = "unknown_device_id",
                brand = "Unknown",
                model = "Unknown",
                systemName = "Android",
                systemVersion = "Unknown",
                appVersion = "Unknown",
                buildNumber = "Unknown",
                packageName = "Unknown",
                manufacturer = "Unknown",
                deviceName = "Unknown",
                deviceType = "Unknown",
                totalMemory = null,
                usedMemory = null,
                isTablet = null,
                adId = null,
                androidId = "Unknown",
                network = null,
                location = null,
                timestamp = "",
                timezone = TimeZone.getDefault().id,
                platform = "android",
                hasNotch = false,
                hasDynamicIsland = false
            )
        }
    }
}