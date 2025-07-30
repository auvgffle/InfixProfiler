# InfixProfiler Android SDK

InfixProfiler is a lightweight Android SDK that collects device information, ad identifiers, location data, and network state for profiling.

## 🚀 Features

- Collects rich device and app metadata
- Fetches Google Ad ID
- Captures network and IP info
- Optional location collection
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
    implementation 'com.github.auvgffle:InfixProfiler:v1.0.2'
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

## 🧩 Usage

### 1. Initialize SDK

You can initialize InfixProfiler from your Application or MainActivity (after location permission if needed):

```kotlin
InfixProfiler.init(
    appContext = applicationContext,
    appId = "YOUR_APP_ID",
    contact = mapOf("email" to "support@example.com"),
    options = InfixProfiler.Options(
        enableDeviceInfo = true,
        enableNetworkInfo = true,
        enableLocation = true,
        enableAdId = true
    )
)
```

### 2. Inspect Payload (Optional)

```kotlin
GlobalScope.launch {
    val payload = InfixProfiler.getCurrentPayload()
    Log.d("InfixProfiler", "Payload: $payload")
}
```

---

## 🛑 Stop Data Collection

```kotlin
InfixProfiler.stop()
```

---

## 📬 Support

If you need assistance integrating this SDK, feel free to reach out:

- 📧 Email: auvgffle@gmail.com

---

## 📜 License

MIT License © 2025 InfixProfiler
