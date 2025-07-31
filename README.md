
# InfixProfiler Android SDK

InfixProfiler is a lightweight Android SDK that collects device information, ad identifiers, location data, and network state for profiling.

---

## 🚀 Features

- Collects rich device and app metadata
- Fetches Google Ad ID
- Captures network and IP info
- Optional location collection with automatic permission handling
- Periodic data transmission every 3 minutes
- Lightweight and easy to integrate

---

## 🔧 Installation

### Step 1: Add JitPack to your root `settings.gradle`

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the SDK to your module's `build.gradle`

```groovy
dependencies {
    implementation 'com.github.auvgffle:InfixProfiler:v1.0.3'
}
```

---

## 🛡 Required Permissions

Add the following to your `AndroidManifest.xml`:

```xml
<!-- Required for location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Required for networking -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

---

## 🧩 Usage Examples

### ✅ Kotlin - Application Level (Recommended)

```kotlin
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

        // 🔥 Request location permission once first activity is resumed
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                InfixProfiler.requestLocationPermissionIfNeeded(activity)
                unregisterActivityLifecycleCallbacks(this)
            }

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
            override fun onActivityStarted(p0: Activity) {}
            override fun onActivityPaused(p0: Activity) {}
            override fun onActivityStopped(p0: Activity) {}
            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
            override fun onActivityDestroyed(p0: Activity) {}
        })

        GlobalScope.launch {
            val payload = InfixProfiler.getCurrentPayload()
            android.util.Log.d("InfixProfilerTest", "Payload: $payload")
        }
    }
}
```

---

### ✅ Kotlin - MainActivity (Alternative)

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InfixProfiler.init(
            appContext = this,
            appId = "YOUR_APP_ID",
            contact = mapOf("email" to "support@example.com"),
            options = InfixProfiler.Options(
                enableDeviceInfo = true,
                enableNetworkInfo = true,
                enableLocation = true,
                enableAdId = true
            )
        )

       
    }
}
```

---

### 🟡 Java - MainActivity

```java
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.infix.profiler.InfixProfiler;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InfixProfiler.Options options = new InfixProfiler.Options(
            true, true, true, true
        );

        InfixProfiler.INSTANCE.init(
            getApplicationContext(),
            "YOUR_APP_ID",
            new HashMap<>(),
            options
        );

        InfixProfiler.INSTANCE.requestLocationPermissionIfNeeded(this);
    }
}
```

---

### 🟡 Java - Application Class

```java
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.infix.profiler.InfixProfiler;
import java.util.HashMap;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        InfixProfiler.Options options = new InfixProfiler.Options(
            true, true, true, true
        );

        InfixProfiler.INSTANCE.init(
            getApplicationContext(),
            "YOUR_APP_ID",
            new HashMap<>(),
            options
        );

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                InfixProfiler.INSTANCE.requestLocationPermissionIfNeeded(activity);
                unregisterActivityLifecycleCallbacks(this);
            }

            public void onActivityCreated(Activity a, Bundle b) {}
            public void onActivityStarted(Activity a) {}
            public void onActivityPaused(Activity a) {}
            public void onActivityStopped(Activity a) {}
            public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            public void onActivityDestroyed(Activity a) {}
        });
    }
}
```

---

## 🛑 Stop Data Collection

```kotlin
InfixProfiler.stop()
```

---

## 🧪 Debug / Inspect Payload

```kotlin
GlobalScope.launch {
    val payload = InfixProfiler.getCurrentPayload()
    Log.d("InfixProfiler", "Payload: $payload")
}
```

---

## 📬 Support

📧 Email: auvgffle@gmail.com

---

## 📜 License

MIT License © 2025 InfixProfiler
