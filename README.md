# 🛡️ Bouncer — Wi-Fi Access Control App

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Design-Material%203-7986CB?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

**A minimalist Android app that lets a home network admin see who's on their Wi-Fi (Airtel/Nokia AOT4221NK router) and kick devices off for a set number of hours — like a club bouncer, but for bandwidth.**

</div>

---

## ⚡ Features

- 🔍 **Live Device Discovery:** Scrapes and lists all active DHCP clients connected to your Nokia / Airtel ONT router (`http://192.168.1.1`).
- 🚫 **One-Tap Instant Ban:** Block rogue or bandwidth-hogging devices immediately via router MAC filtering rules.
- ⏱️ **Flexible Timed Bans:** Choose between preset durations (1 hr, 2 hrs, 4 hrs, 8 hrs) or enter an arbitrary custom duration (e.g. 0.5 hrs or 12 hrs).
- 🔄 **Autonomous Background Unbans:** Uses Android `WorkManager` with `RECEIVE_BOOT_COMPLETED` so unban tasks run reliably even if the app is killed or the phone is rebooted.
- ⏳ **Live Countdown Badges:** Real-time countdowns on banned device cards showing exactly how much time remains.
- 🔓 **Instant Early Unban:** Manually unban any device on-demand with one tap, canceling the pending background task.
- 🔒 **Zero Hardcoded Secrets:** Credentials are typed interactively and stored in hardware-backed `EncryptedSharedPreferences` (AES-256 GCM).

---

## 🏗️ Architecture

```
com.example.bouncer
├── MainActivity.kt              // Compose host & screen crossfade navigation
├── ui/
│   ├── DeviceListScreen.kt      // Material 3 pull-to-refresh & device listing
│   ├── DeviceCard.kt            // Device stats, countdown & action triggers
│   ├── BanDurationDialog.kt     // Preset & custom hours modal
│   ├── LoginSetupScreen.kt      // Router credential entry & validation
│   └── theme/                   // Theme.kt, Color.kt, Type.kt (Dark-first design)
├── data/
│   ├── RouterRepository.kt      // Interface for router communication
│   ├── AOT4221NKRepository.kt   // Concrete implementation for Nokia/Airtel ONT
│   ├── ConnectedDevice.kt       // Model: name, IP, MAC address
│   ├── BanRecord.kt             // Entity: mac, deviceName, bannedAt, unbanAt, workRequestId
│   └── local/
│       ├── BouncerDatabase.kt   // Room DB singleton
│       ├── BanRecordDao.kt      // CRUD & Flow observation
│       └── CredentialStore.kt   // AES-256 EncryptedSharedPreferences wrapper
├── network/
│   ├── RouterHttpClient.kt      // OkHttpClient singleton
│   └── RouterCookieJar.kt       // Session cookie store
├── worker/
│   └── UnbanWorker.kt           // CoroutineWorker scheduled via WorkManager
└── viewmodel/
    └── DeviceListViewModel.kt   // Reactive StateFlow combining Room + Network + Workers
```

---

## 🚀 Getting Started

### Prerequisites
- Android 7.0 (API Level 24) or higher.
- Java Development Kit (JDK 17).
- Connected to your Airtel / Nokia ONT router's Wi-Fi network.

### Build & Run
1. Clone this repository:
   ```bash
   git clone https://github.com/ratna3/bouncer.git
   cd bouncer/bouncer-app
   ```
2. Build the Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. Install to your connected device:
   ```bash
   ./gradlew installDebug
   ```

---

## 📖 Usage Guide

1. **Connect to Home Wi-Fi:** Ensure your Android phone is connected to your Nokia / Airtel ONT router.
2. **First-Time Setup:**
   - Launch **Bouncer**.
   - Verify the base URL (defaults to `http://192.168.1.1`).
   - Enter your router admin credentials (usually printed on the sticker on the back/bottom of your router).
   - Tap **Save & Continue**.
3. **Pausing Devices:**
   - Tap the red **Pause** button on any device card.
   - Choose a duration or type custom hours, then tap **Ban Device**.
4. **Unbanning:**
   - The device will automatically regain access when the timer expires.
   - You can also tap **Unban Now** to restore access immediately.

---

## 👤 Author

<div align="center">

**Ratna Kirti**

[![GitHub](https://img.shields.io/badge/GitHub-ratna3-181717?style=for-the-badge&logo=github)](https://github.com/ratna3)
[![Twitter](https://img.shields.io/badge/Twitter-@RatnaKirti1-1DA1F2?style=for-the-badge&logo=twitter)](https://x.com/RatnaKirti1)
[![Discord](https://img.shields.io/badge/Discord-Join%20Server-5865F2?style=for-the-badge&logo=discord)](https://discord.gg/VRPSujmH)
[![Email](https://img.shields.io/badge/Email-Contact-D14836?style=for-the-badge&logo=gmail)](mailto:ratnakirti03@gmail.com)

</div>

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
