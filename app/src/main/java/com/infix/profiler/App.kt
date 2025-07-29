package com.infix.profiler

import android.app.Application
import com.infix.profiler.InfixProfiler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        InfixProfiler.init(
            appContext = this,
            appId = "TEST_APP_ID",
            contact = mapOf("email" to "test@example.com"),
            options = InfixProfiler.Options(
                enableDeviceInfo = true,
                enableNetworkInfo = true,
                enableLocation = true,
                enableAdId = true
            )
        )

        // Example: log the payload after a short delay
        GlobalScope.launch {
            val payload = InfixProfiler.getCurrentPayload()
            android.util.Log.d("InfixProfilerTest", "Payload: $payload")
        }
    }
} 