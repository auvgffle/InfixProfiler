package com.infix.profiler.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.NetworkInterface
import android.util.Log

data class NetworkInfoResult(
    val isWifiEnabled: Boolean,
    val ssid: String?,
    val bssid: String?,
    val macAddress: String?,
    val ipAddress: String?,
    val signalLevel: Int?,
    val linkSpeed: Int?,
    val networkType: String?,
    val networkClass: String?,
    val simOperator: String?,
    val simOperatorName: String?,
    val networkOperator: String?,
    val networkOperatorName: String?,
    val simCountry: String?,
    val simState: Int?,
    val isRoaming: Boolean?,
    val androidId: String?,
    val publicIp: String? = null
)

object NetworkInfoManager {

    fun getNetworkInfo(context: Context): NetworkInfoResult {
        return try {
            val hasWifiPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
            val hasPhoneStatePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            val wifiManager = if (hasWifiPermission) context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager else null
            val wifiInfo = wifiManager?.connectionInfo
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            if (!hasWifiPermission) {
                Log.w("InfixProfiler-Network", "ACCESS_WIFI_STATE permission not granted")
            }
            if (!hasPhoneStatePermission) {
                Log.w("InfixProfiler-Network", "READ_PHONE_STATE permission not granted")
            }

        val ssid = wifiInfo?.ssid?.removePrefix("\"")?.removeSuffix("\"")
        val bssid = wifiInfo?.bssid
            val signalLevel = wifiInfo?.let { WifiManager.calculateSignalLevel(it.rssi, 5) } ?: null
        val linkSpeed = wifiInfo?.linkSpeed
        val ipAddress = getIpAddress(wifiInfo)
        val macAddress = getMacAddress(wifiInfo)

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

            // Only access telephony fields if permission is granted
            val currentNetworkType = if (hasPhoneStatePermission) {
            try {
                telephonyManager.networkType
            } catch (e: SecurityException) {
                null
            }
        } else {
            null
        }

        val networkType = currentNetworkType?.let { getNetworkTypeName(it) }
        val networkClass = currentNetworkType?.let { getNetworkClass(it) }

            val simOperator = if (hasPhoneStatePermission) {
            try { telephonyManager.simOperator } catch (e: SecurityException) { null }
        } else null

            val simOperatorName = if (hasPhoneStatePermission) {
            try { telephonyManager.simOperatorName } catch (e: SecurityException) { null }
        } else null

            val networkOperator = if (hasPhoneStatePermission) {
            try { telephonyManager.networkOperator } catch (e: SecurityException) { null }
        } else null

            val networkOperatorName = if (hasPhoneStatePermission) {
            try { telephonyManager.networkOperatorName } catch (e: SecurityException) { null }
        } else null

            val simCountry = if (hasPhoneStatePermission) {
            try { telephonyManager.simCountryIso } catch (e: SecurityException) { null }
        } else null

            val simState = if (hasPhoneStatePermission) {
            try { telephonyManager.simState } catch (e: SecurityException) { null }
        } else null

            val isRoaming = if (hasPhoneStatePermission) {
            try { telephonyManager.isNetworkRoaming } catch (e: SecurityException) { null }
        } else null

        return NetworkInfoResult(
                isWifiEnabled = wifiManager?.isWifiEnabled ?: false,
            ssid = ssid,
            bssid = bssid,
            macAddress = macAddress,
            ipAddress = ipAddress,
            signalLevel = signalLevel,
            linkSpeed = linkSpeed,
            networkType = networkType,
            networkClass = networkClass,
            simOperator = simOperator,
            simOperatorName = simOperatorName,
            networkOperator = networkOperator,
            networkOperatorName = networkOperatorName,
            simCountry = simCountry,
            simState = simState,
            isRoaming = isRoaming,
            androidId = androidId
        )
        } catch (e: Exception) {
            Log.e("InfixProfiler-Network", "Exception getting network info: ${e.message}", e)
            return NetworkInfoResult(
                isWifiEnabled = false,
                ssid = null,
                bssid = null,
                macAddress = null,
                ipAddress = null,
                signalLevel = null,
                linkSpeed = null,
                networkType = null,
                networkClass = null,
                simOperator = null,
                simOperatorName = null,
                networkOperator = null,
                networkOperatorName = null,
                simCountry = null,
                simState = null,
                isRoaming = null,
                androidId = null
            )
        }
    }

    private fun getMacAddress(wifiInfo: android.net.wifi.WifiInfo?): String? {
        return try {
            val mac = wifiInfo?.macAddress
            if (!mac.isNullOrEmpty() && mac != "02:00:00:00:00:00") {
                mac
            } else {
                getHardwareMac()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getHardwareMac(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .firstOrNull { it.name.equals("wlan0", ignoreCase = true) }
                ?.hardwareAddress
                ?.joinToString(":") { b -> "%02X".format(b) }
        } catch (e: Exception) {
            null
        }
    }

    private fun getIpAddress(wifiInfo: android.net.wifi.WifiInfo?): String? {
        return try {
            val ip = wifiInfo?.ipAddress ?: return null
            String.format(
                "%d.%d.%d.%d",
                ip and 0xff,
                ip shr 8 and 0xff,
                ip shr 16 and 0xff,
                ip shr 24 and 0xff
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getNetworkClass(type: Int): String = when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
        TelephonyManager.NETWORK_TYPE_LTE -> "4G"
        TelephonyManager.NETWORK_TYPE_NR -> "5G"
        else -> "Unknown"
    }

    private fun getNetworkTypeName(type: Int): String = when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPAP"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
        TelephonyManager.NETWORK_TYPE_NR -> "NR"
        else -> "UNKNOWN"
    }
}