# OneBusAway Vehicle Tracker (Android)

A modern Android application designed for real-time vehicle tracking and telemetry ingestion into the OneBusAway ecosystem.

## 🚀 Features

- **Real-time Tracking**: Uses high-accuracy location services to track vehicle positions in the background.
- **Foreground Service**: Ensures continuous tracking even when the app is minimized, following Android battery and permission best practices.
- **Dynamic Identification**: Operators can input `vehicle_id` and `trip_id` directly to sync with existing GTFS schedules.
- **Modern Architecture**:
    - **Jetpack Compose**: 100% declarative UI.
    - **Hilt (Dagger)**: Dependency injection for modularity and testability.
    - **Retrofit**: Robust networking with the vehicle-positions backend.
    - **Material 3**: Clean, professional design following M3 guidelines.

## 🛠️ Requirements

- Android SDK 24+ (Android 7.0+)
- Google Play Services (for Fused Location Provider)
- A running instance of the [vehicle-positions backend](https://github.com/OneBusAway/vehicle-positions).

## 📦 Setup & Installation

1. Clone this repository.
2. Open in Android Studio (Iguana+ recommended).
3. Ensure the `baseUrl` in `NetworkModule.kt` points to your backend (default is `10.0.2.2` for emulator).
4. Build and Run.

## 🛡️ Network Security

The app is configured with a `network_security_config.xml` to allow cleartext (HTTP) communication with local IP `10.0.2.2` specifically for development environments. Production deployments should use HTTPS.

---
Part of the Open Transit Software Foundation.
