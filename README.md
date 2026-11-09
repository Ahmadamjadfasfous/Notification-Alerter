# 🚨 Smart Notification Alerter
### Advanced VIP Emergency Alert System for Android

<p align="center">
  <strong>Never Miss What Matters.</strong>
</p>

## 📌 Overview

**Smart Notification Alerter** is an advanced native Android application built with **Kotlin and Jetpack Compose**, designed to transform important incoming notifications into high-priority emergency alerts.

Unlike traditional notification systems that rely on standard notification sounds and vibration, Smart Notification Alerter actively monitors incoming system notifications and evaluates them against customizable user-defined rules.

When a matching notification is detected, the application immediately triggers a continuous emergency ringtone, customizable vibration patterns, and an animated alert interface.

The application is designed to ensure that critical notifications from VIP contacts, specific channels, or important keywords receive immediate attention — even when the device is locked, in Silent Mode, or operating under Do Not Disturb (DND).

Supporting applications include platforms such as:

- WhatsApp
- Telegram
- Discord
- Other applications that generate accessible Android notifications.

## ✨ Key Features

### 🕵️ Background Notification Interception
Uses Android's native `NotificationListenerService` to monitor incoming system notifications without requiring the application to remain open on the screen.

### 🎯 Advanced Notification Filtering
Create highly customizable notification rules using three matching methods:

- `CONTAINS` – Detects notifications containing a specific keyword.
- `EXACT` – Matches exact text.
- `REGEX` – Supports advanced regular expression matching.

Choose exactly where matching should occur:
- Notification title.
- Notification body.
- Both title and body.

### 🚨 Emergency Audio System
Transforms matching notifications into high-priority emergency alerts.

- Continuous ringtone playback.
- Custom audio file selection.
- Configurable volume levels.
- Alarm audio stream integration.
- Tested Silent Mode and DND alert functionality.

### 📳 Custom Vibration Engine
Supports configurable vibration behavior, including dedicated SOS-style vibration patterns.

### 🔒 Interactive Lock-Screen Alert
Displays a dedicated emergency interface featuring animated pulsing effects and immediate alert controls.

The alert interface is designed to bring critical notifications to the user's attention even when the device is locked.

### 🔄 Persistent Background Monitoring
Uses Android Foreground Services to maintain background alert processing and manage active emergency notifications.

### 🗄️ Local Database Management
Uses Room Database to securely store notification rules and application configurations locally.

### 🎵 Intelligent Audio Fallback
Automatically switches to a default emergency ringtone if a selected custom audio resource becomes unavailable.

---

## 🛠️ Technology Stack

| Technology | Implementation |
|---|---|
| Kotlin | Native Android development |
| Jetpack Compose | Modern declarative UI |
| Android SDK | Native system integration |
| MVVM | Presentation architecture |
| Clean Architecture | Separation of concerns |
| Room Database | Local persistent storage |
| Kotlin Coroutines | Asynchronous processing |
| NotificationListenerService | Notification interception |
| Foreground Services | Background execution |
| MediaPlayer | Emergency audio playback |
| WakeLock | CPU wake management |
| GitHub Actions | Automated APK builds |

## 🏗️ Project Architecture

The project follows a modular architecture designed to separate data management, background services, and user interface responsibilities.

```text
Smart Notification Alerter/
│
├── app/
│   ├── src/main/
│   │   ├── java/com/smartalerter/notification/
│   │   │
│   │   ├── data/
│   │   │   ├── db/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   └── RuleDao.kt
│   │   │   └── model/
│   │   │       ├── InstalledAppInfo.kt
│   │   │       └── RuleEntity.kt
│   │   │
│   │   ├── service/
│   │   │   ├── AlarmForegroundService.kt
│   │   │   └── NotificationInterceptorService.kt
│   │   │
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── ActiveAlertActivity.kt
│   │   │   │   ├── AddEditRuleScreen.kt
│   │   │   │   ├── AppSelectionScreen.kt
│   │   │   │   └── MainScreen.kt
│   │   │   └── viewmodel/
│   │   │       └── MainViewModel.kt
│   │   │
│   │   └── util/
│   │       └── PermissionHelper.kt
│   │
│   └── AndroidManifest.xml
│
├── .github/
│   └── workflows/
│       └── build-apk.yml
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## 🧩 Core Modules

| Module | Responsibility |
|---|---|
| `NotificationInterceptorService.kt` | Monitors notifications, extracts available content, and evaluates matching rules. |
| `AlarmForegroundService.kt` | Controls continuous audio playback, vibration, and background alert execution. |
| `ActiveAlertActivity.kt` | Provides the animated emergency interface on the lock screen. |
| `AddEditRuleScreen.kt` | Configures keywords, matching methods, audio files, and alert behavior. |
| `AppSelectionScreen.kt` | Selects installed applications for notification monitoring. |
| `RuleDao.kt` | Manages notification rule database operations. |
| `MainViewModel.kt` | Coordinates UI state and application data. |

## ⚙️ How It Works

The application follows a simple but powerful notification processing pipeline:

```text
Incoming System Notification
           |
           ▼
NotificationListenerService
           |
           ▼
Extract Notification Data
           |
           ▼
Retrieve Active Rules
           |
           ▼
Evaluate Matching Conditions
           |
       ┌───┴───┐
       |       |
     Match   No Match
       |       |
       ▼       ▼
Trigger     Ignore
Emergency
Alert
       |
       ▼
AlarmForegroundService
       |
   ┌───┼──────────┐
   ▼   ▼          ▼
 Sound Vibration  Alert UI
       |
       ▼
 User Dismisses Alert
```

## 📝 Example Rule Configuration

Users can configure individual emergency rules for different applications and notification conditions.

```json
{
  "id": 12,
  "appName": "Discord",
  "packageName": "com.discord",
  "keywordOrContact": "Emergency_Channel",
  "matchType": "CONTAINS",
  "matchTarget": "ANY",
  "isCaseSensitive": false,
  "soundUri": "content://media/external/audio/media/432",
  "soundTitle": "Siren_Track.mp3",
  "bypassSilentMode": true,
  "volumePercent": 100,
  "vibrationEnabled": true,
  "vibrationPattern": "SOS",
  "isEnabled": true
}
```

## 🚀 Installation & Setup

### 1. Install the APK

Download and install `app-debug.apk` on your Android device.

### 2. Enable Restricted Settings

On supported Android 13+ devices:

1. Navigate to Settings → Apps.
2. Select Smart Notification Alerter.
3. Open the three-dot menu.
4. Select Allow Restricted Settings.
5. Authenticate if requested.

### 3. Enable Notification Access

1. Open the application.
2. Navigate to Notification Access settings.
3. Find Smart Notification Alerter.
4. Enable notification access.

### 4. Configure Background Permissions

1. Open Settings → Apps → Smart Notification Alerter.
2. Navigate to Battery settings.
3. Configure battery usage to Unrestricted.
4. Enable Auto-start and background activity permissions where available.

## 🛡️ Reliability & Error Handling

The application implements multiple mechanisms to improve operational reliability:

- **Audio Recovery:** Automatically falls back to a default emergency ringtone when custom audio resources become unavailable.
- **Background Execution:** Uses Foreground Services and WakeLocks to maintain active alert processing.
- **Permission Validation:** Handles required system permissions and guides users through configuration.
- **Persistent Configuration:** Maintains user-defined rules through local database storage.
- **Alert Management:** Provides dedicated controls for managing and dismissing active alerts.

## 🔐 Privacy & Security

Smart Notification Alerter is designed with a local-first approach.

- Notification processing is performed locally on the Android device.
- Notification rules are stored in a local Room database.
- No external server is required for core functionality.
- No cloud-based notification processing is required.
- Notification access is used to evaluate user-configured alert conditions.

## 📱 Android Compatibility

The application integrates directly with Android system services and permissions.

Its functionality has been tested with Silent Mode and Do Not Disturb configurations, alongside background notification processing and lock-screen alert behavior.

Device-specific settings and Android permission requirements are documented in the installation instructions.

## 🏁 Conclusion

**Smart Notification Alerter is more than a notification management application.**

It is a customizable emergency notification system that bridges the gap between passive notifications and immediate user awareness.

By combining native Android system services, intelligent text-matching rules, custom audio and vibration engines, persistent background execution, and an interactive lock-screen interface, the project delivers a practical solution for prioritizing critical communications.

The project demonstrates hands-on experience with:

- Native Android application development.
- System-level notification monitoring.
- Background service management.
- Android audio and vibration control.
- Local database architecture.
- Permission management and OS integration.
- Modern UI development using Jetpack Compose.

---

**Project Name:** Smart Notification Alerter  
**Platform:** Android  
**Programming Language:** Kotlin  
**UI Framework:** Jetpack Compose  
**Architecture:** MVVM / Clean Architecture  
**Database:** Room  
**Build Automation:** GitHub Actions
