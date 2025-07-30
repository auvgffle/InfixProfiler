package com.infix.profiler

import android.app.Activity
import android.app.Application
import android.os.Bundle
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


        // 🔥 Delay permission request until first Activity resumes
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                InfixProfiler.requestLocationPermissionIfNeeded(activity)
                unregisterActivityLifecycleCallbacks(this) // run once
            }

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
            override fun onActivityStarted(p0: Activity) {}
            override fun onActivityPaused(p0: Activity) {}
            override fun onActivityStopped(p0: Activity) {}
            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
            override fun onActivityDestroyed(p0: Activity) {}
        })

        // Example: log the payload after a short delay
        GlobalScope.launch {
            val payload = InfixProfiler.getCurrentPayload()
            android.util.Log.d("InfixProfilerTest", "Payload: $payload")
        }
    }
} 