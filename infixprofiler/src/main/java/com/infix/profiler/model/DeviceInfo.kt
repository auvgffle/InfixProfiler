package com.infix.profiler.model
data class DeviceInfo(
    val deviceId: String,
    val brand: String? = null,
    val model: String? = null,
    val systemName: String? = null,
    val systemVersion: String? = null,
    val appVersion: String? = null,
    val buildNumber: String? = null,
    val packageName: String? = null,
    val manufacturer: String? = null,
    val deviceName: String? = null,
    val deviceType: String? = null,
    val totalMemory: Long? = null,
    val usedMemory: Long? = null,
    val isTablet: Boolean? = null,
    val adId: String? = null,
    val androidId: String? = null,
    val network: NetworkInfoWrapper? = null,
    val location: LocationInfo? = null,
    val timestamp: String,
    val timezone: String? = null,
    val platform: String = "android",
    val hasNotch: Boolean? = null,
    val hasDynamicIsland: Boolean? = null
)





