# InfixProfiler Android SDK

## Overview

**InfixProfiler** is a customizable Android SDK for collecting device, network, location, and advertising ID information, and periodically sending this data to your backend API. It is designed for easy integration, flexibility, and production use.

---

## Features

- Collects device, network, location, and ad ID info (all optional)
- Sends data to your API every 3 minutes (customizable)
- Host app can access the current payload at any time
- Health check and stop methods for full control
- Minimal setup: just initialize and add permissions
- Highly customizable: enable/disable any data source

---

## What the SDK Collects

- **Device Info**: Manufacturer, model, OS version, app version, screen size, etc.
- **Network Info**: WiFi, SIM, carrier, IP, etc.
- **Location**: Latitude, longitude, accuracy, etc.
- **Ad ID**: Google Advertising ID

---

## Usage

### 1. Add SDK to your project

- Add the SDK as a module or dependency (see “Deployment” below).

### 2. Add required permissions to your `AndroidManifest.xml`

Add only the permissions for the services you enable:

```xml
<!-- Always required -->
<uses-permission android:name="android.permission.INTERNET"/>

<!-- If network info enabled -->
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

<!-- If location enabled -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

### 3. Initialize the SDK

In your `Application` or main `Activity`:

```kotlin
import com.infix.profiler.InfixProfiler

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        InfixProfiler.init(
            context = this,
            appId = "YOUR_APP_ID",
            contact = mapOf("email" to "user@example.com"),
            options = InfixProfiler.Options(
                enableDeviceInfo = true,
                enableNetworkInfo = true,
                enableLocation = false, // disables location collection and permission
                enableAdId = true
            )
        )
    }
}
```

### 4. Access the current payload

```kotlin
// In a coroutine scope
val payload = InfixProfiler.getCurrentPayload()
```

### 5. Check SDK health

```kotlin
val health = InfixProfiler.healthCheck()
```

### 6. Stop the SDK

```kotlin
InfixProfiler.stop()
```

---

## API Reference

### Initialization

```kotlin
InfixProfiler.init(context, appId, contact, options)
```

- `context`: Application or Activity context.
- `appId`: Your app’s unique identifier.
- `contact`: Optional map of contact info.
- `options`: `Options` object to enable/disable services.

### Options

```kotlin
InfixProfiler.Options(
    enableDeviceInfo = true,
    enableNetworkInfo = true,
    enableLocation = true,
    enableAdId = true
)
```

### Get Current Payload

```kotlin
val payload = InfixProfiler.getCurrentPayload()
```

### Health Check

```kotlin
val health = InfixProfiler.healthCheck()
```

### Stop SDK

```kotlin
InfixProfiler.stop()
```

---

## Deployment: How to Publish the SDK

### A. As a Local Module

1. Copy the `infixprofiler` module into your host app project.
2. In your host app’s `settings.gradle`, include the module:
   ```
   include(":infixprofiler")
   ```
3. In your host app’s `build.gradle`:
   ```kotlin
   implementation(project(":infixprofiler"))
   ```

### B. As an AAR/Maven Artifact

1. Build the AAR:
   ```sh
   ./gradlew :infixprofiler:assembleRelease
   ```
   The AAR will be in `infixprofiler/build/outputs/aar/`.
2. Publish to your private Maven repo or distribute the AAR file.
3. In the host app’s `build.gradle`:
   ```kotlin
   implementation("com.yourorg:infixprofiler:1.0.0")
   ```

For more, see the [official Android library publishing guide](https://developer.android.com/studio/projects/android-library).

---

## Best Practices

- Only enable the services you need.
- Always request runtime permissions for location/network if required.
- Handle privacy and user consent as per your app’s policy.
- Monitor SDK health using `healthCheck()`.
- Stop the SDK when not needed to save resources.

---

## Support & Contributions

For issues, feature requests, or contributions, please open an issue or pull request in this repository.

---

## License

[Your License Here]
