package com.infix.profiler

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.infix.profiler.manager.DeviceInfoManager
import com.infix.profiler.manager.NetworkInfoManager
import com.infix.profiler.manager.LocationManager
import com.infix.profiler.manager.AdIdManager
import com.infix.profiler.api.SdkApiService
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import com.infix.profiler.model.DeviceInfo
import com.infix.profiler.manager.NetworkInfoResult
import com.infix.profiler.manager.LocationResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import java.net.URL
import java.net.HttpURLConnection
import org.json.JSONObject
import android.Manifest
import android.app.Activity

/**
 * InfixProfiler SDK
 *
 * Usage (in host app):
 * 1. Add required permissions to your AndroidManifest.xml based on enabled services:
 *    - Device info: No extra permissions
 *    - Network info: <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
 *    - Location: <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 *                <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 *    - AdId: No extra permissions (uses Google Play Services)
 *    - All: <uses-permission android:name="android.permission.INTERNET"/>
 * 2. In your Application or MainActivity:
 *    InfixProfiler.init(context, appId = "YOUR_APP_ID", contact = mapOf(...), options = Options(...))
 *
 * The SDK will collect enabled info and send it to the API every 3 minutes.
 */
@SuppressLint("StaticFieldLeak")
object InfixProfiler : CoroutineScope {
    data class Options(
        val enableDeviceInfo: Boolean = true,
        val enableNetworkInfo: Boolean = true,
        val enableLocation: Boolean = true,
        val enableAdId: Boolean = true
    )

    private var initialized = false
    private lateinit var context: Context
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job
    private var periodicJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())

    private var appId: String? = null
    private var contact: Map<String, Any?>? = null
    private var options: Options = Options()
    private var token: String? = null
    private var tokenExpiry: Long = 0L
    private var lastPayload: Map<String, Any?>? = null
    private var lastSendResult: Boolean = false
    private var lastError: String? = null
    @Volatile
    var lastApiResponse: Any? = null
        private set

    // Use the real API base URL
    private const val BASE_URL = "https://sdk.intelvis.org/"
    private val apiService: SdkApiService by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SdkApiService::class.java)
    }

    fun init(
        appContext: Context,
        appId: String,
        contact: Map<String, Any?>? = null,
        options: Options = Options()
    ) {
        if (initialized) return
        context = appContext.applicationContext
        this.appId = appId
        this.contact = contact
        this.options = options

        if (options.enableLocation) {
            try {
                if (appContext is Activity) {
                    requestLocationPermission(appContext)
                } else {
                    Log.w("InfixProfiler", "Location permission not requested: context is not an Activity")
                }
            } catch (e: Exception) {
                Log.e("InfixProfiler", "Failed to request location permission: ${e.message}", e)
            }
        }

        initialized = true
        startPeriodicDataCollection()
    }


    //    Location permissions
    private fun requestLocationPermission(activity: Activity) {
        val permissionsNeeded = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val shouldRequest = permissionsNeeded.any {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (shouldRequest) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded, 1001)
            Log.d("InfixProfiler", "Location permission requested")
        } else {
            Log.d("InfixProfiler", "Location permission already granted")
        }
    }


    fun requestLocationPermissionIfNeeded(activity: Activity) {
        try {
            if (!options.enableLocation) {
                Log.i("InfixProfiler", "Location collection is disabled in options")
                return
            }

            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            val missing = permissions.filter {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missing.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity, missing.toTypedArray(), 1001)
                Log.d("InfixProfiler", "Requested location permission manually")
            } else {
                Log.d("InfixProfiler", "Location permission already granted")
            }

        } catch (e: Exception) {
            Log.e("InfixProfiler", "Error while requesting location permission: ${e.message}", e)
        }
    }


    private fun startPeriodicDataCollection() {
        periodicJob = launch {
            while (isActive) {
                sendData()
                delay(TimeUnit.MINUTES.toMillis(3))
            }
        }
    }

    suspend fun getCurrentPayload(): Map<String, Any?> {
        return buildPayload()
    }

    suspend fun sendData(extraPayload: Map<String, Any?> = emptyMap()) {
        val payload = buildRichPayload(extraPayload)
        lastPayload = payload
        val maxRetries = 3
        val retryDelayMs = 2000L
        var attempt = 1
        lastApiResponse = null
        while (attempt <= maxRetries) {
            try {
                logInfo("API", "[Attempt $attempt/$maxRetries] Sending data to $BASE_URL/events: $payload")
                val response = apiService.sendDeviceDataMap(payload)
                lastApiResponse = response.body() ?: response.errorBody()?.string()
                logInfo("API", "API response: ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    lastSendResult = true
                    lastError = null
                    Log.d("InfixProfiler", "Data sent successfully: $payload")
                    break
                } else {
                    logError("API", "API error response: ${response.code()} ${response.message()}")
                    lastSendResult = false
                    lastError = response.message()
                }
            } catch (e: Exception) {
                lastSendResult = false
                lastError = e.message
                lastApiResponse = e.message
                logError("API", "Error sending data (attempt $attempt): ${e.message}", e)
            }
            if (attempt < maxRetries) {
                logInfo("API", "Retrying in ${retryDelayMs}ms...")
                delay(retryDelayMs)
            }
            attempt++
        }
        if (!lastSendResult) {
            logError("API", "Failed to send data after $maxRetries attempts.")
        }
    }

    private suspend fun buildPayload(extraPayload: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        val deviceInfo = DeviceInfoManager().getDeviceInfo(context)
        var networkInfo: NetworkInfoResult? = null
        var location: LocationResult? = null
        var adId: String? = null
        var publicIp: String? = null
        try {
            if (options.enableNetworkInfo) {
                networkInfo = NetworkInfoManager.getNetworkInfo(context)
                publicIp = fetchPublicIp()
                logInfo("Network", "Network info collected: $networkInfo, publicIp: $publicIp")
            }
            if (options.enableLocation) {
                location = LocationManager.getCurrentLocation(context)
                logInfo("Location", "Location collected: $location")
            }
            if (options.enableAdId) {
                adId = getAdIdSuspend()
                logInfo("AdId", "Ad ID collected: $adId")
            }
        } catch (e: Exception) {
            logError("Payload", "Error collecting info: ${e.message}", e)
        }
        // Compose the inner payload
        val innerPayload = mutableMapOf<String, Any?>()
        innerPayload.putAll(deviceInfo.asMap())
        innerPayload["network"] = networkInfo?.copy(publicIp = publicIp)
        innerPayload["location"] = location
        innerPayload["adId"] = adId
        innerPayload["timestamp"] = deviceInfo.timestamp
        innerPayload["timezone"] = deviceInfo.timezone
        innerPayload["platform"] = deviceInfo.platform
        // Add more fields as needed (e.g., memory, notch, etc.)
        // Compose the top-level payload
        val apiKey = fetchApiKey(deviceInfo.deviceId) // Implement token logic if needed
        val topPayload = mutableMapOf<String, Any?>(
            "apiKey" to apiKey,
            "device_id" to deviceInfo.deviceId,
            "payload" to innerPayload,
            "platform" to deviceInfo.platform
        )
        if (contact != null) topPayload["contact"] = contact
        topPayload.putAll(extraPayload)
        return topPayload
    }

    private suspend fun getAdIdSuspend(): String? = suspendCancellableCoroutine { cont ->
        val adIdManager = AdIdManager(context)
        adIdManager.fetchAdId(object : AdIdManager.Callback {
            override fun onSuccess(adId: String) { cont.resume(adId) {} }
            override fun onFailure(errorMessage: String) { cont.resume(null) {} }
        })
    }

    private suspend fun fetchPublicIp(): String? {
        return try {
            val url = URL("https://api.ipify.org?format=json")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.connect()
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            json.optString("ip")
        } catch (e: Exception) {
            logWarning("Network", "Failed to fetch public IP: ${e.message}")
            null
        }
    }

    private suspend fun buildRichPayload(extraPayload: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        val deviceInfo = DeviceInfoManager().getDeviceInfo(context)
        var networkInfo: NetworkInfoResult? = null
        var location: LocationResult? = null
        var adId: String? = null
        var publicIp: String? = null
        try {
            if (options.enableNetworkInfo) {
                networkInfo = NetworkInfoManager.getNetworkInfo(context)
                publicIp = fetchPublicIp()
                logInfo("Network", "Network info collected: $networkInfo, publicIp: $publicIp")
            }
            if (options.enableLocation) {
                location = LocationManager.getCurrentLocation(context)
                logInfo("Location", "Location collected: $location")
            }
            if (options.enableAdId) {
                adId = getAdIdSuspend()
                logInfo("AdId", "Ad ID collected: $adId")
            }
        } catch (e: Exception) {
            logError("Payload", "Error collecting info: ${e.message}", e)
        }
        // Compose the inner payload
        val innerPayload = mutableMapOf<String, Any?>()
        innerPayload.putAll(deviceInfo.asMap())
        innerPayload["network"] = networkInfo?.copy(publicIp = publicIp)
        innerPayload["location"] = location
        innerPayload["adId"] = adId
        innerPayload["timestamp"] = deviceInfo.timestamp
        innerPayload["timezone"] = deviceInfo.timezone
        innerPayload["platform"] = deviceInfo.platform
        // Add more fields as needed (e.g., memory, notch, etc.)
        // Compose the top-level payload
        val apiKey = fetchApiKey(deviceInfo.deviceId) // Implement token logic if needed
        val topPayload = mutableMapOf<String, Any?>(
            "apiKey" to apiKey,
            "device_id" to deviceInfo.deviceId,
            "payload" to innerPayload,
            "platform" to deviceInfo.platform
        )
        if (contact != null) topPayload["contact"] = contact
        topPayload.putAll(extraPayload)
        return topPayload
    }

    private suspend fun fetchApiKey(deviceId: String): String? {
        // Implement token logic here if needed, or return null for now
        return null
    }

    fun stop() {
        periodicJob?.cancel()
        job.cancel()
        initialized = false
        Log.d("InfixProfiler", "Periodic data sending stopped.")
    }

    fun healthCheck(): Map<String, Any?> = mapOf(
        "initialized" to initialized,
        "appId" to appId,
        "contact" to contact,
        "options" to options,
        "lastPayload" to lastPayload,
        "lastSendResult" to lastSendResult,
        "lastError" to lastError,
        "isRunning" to (periodicJob?.isActive == true)
    )

    private fun logInfo(tag: String, message: String) = Log.i("InfixProfiler-$tag", message)
    private fun logError(tag: String, message: String, throwable: Throwable? = null) = Log.e("InfixProfiler-$tag", message, throwable)
    private fun logWarning(tag: String, message: String) = Log.w("InfixProfiler-$tag", message)

    private fun DeviceInfo.asMap(): Map<String, Any?> = mapOf(
        "deviceId" to deviceId,
        "brand" to brand,
        "model" to model,
        "systemName" to systemName,
        "systemVersion" to systemVersion,
        "appVersion" to appVersion,
        "buildNumber" to buildNumber,
        "packageName" to packageName,
        "manufacturer" to manufacturer,
        "deviceName" to deviceName,
        "deviceType" to deviceType,
        "totalMemory" to totalMemory,
        "usedMemory" to usedMemory,
        "isTablet" to isTablet,
        "adId" to adId,
        "androidId" to androidId,
        "hasNotch" to hasNotch,
        "hasDynamicIsland" to hasDynamicIsland

    )
}