<div align="center">

<img src="https://github.com/user-attachments/assets/0a46a8c6-7ffc-4bfd-b672-472c8d67a160" alt="Whozit App Icon" width="112" style="border-radius: 26px; box-shadow: 0 8px 24px rgba(0,0,0,0.12);" />

# Whozit

### A shared turn and schedule manager featuring fluid Liquid Glass UI for keeping everyday routines organized.

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.06-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-31%20(Android%2012+)-informational?style=flat-square)](https://developer.android.com/about/versions/12)
[![Latest Release](https://img.shields.io/github/v/release/theheartbreakid/Whozit?style=flat-square&label=Release&color=success)](https://github.com/theheartbreakid/Whozit/releases)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

<br/>

<a href="#-screenshots--visual-showcase">
  <img src="https://github.com/user-attachments/assets/855836b1-8bfb-42f5-a9ef-f9a855d9e17d" alt="Whozit Liquid Glass Interface Overview" width="90%" style="border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,0.15);" />
</a>

<p align="center">
  <em>Managing shared routines, turn rotations, and schedule coordination across three distinct UI experiences, spearheaded by fluid Liquid Glass.</em>
</p>

</div>

---

## 💡 Why Whozit?

When households, roommates, project partners, or teams share recurring responsibilities, tracking who did what and whose turn is next often breaks down into guesswork and scattered chat threads.

**Whozit** solves this everyday problem with clarity and precision:
- **Whose turn is it today?** Immediate glanceability on your daily dashboard.
- **What is coming up?** An interactive multi-schedule calendar and turn projection timeline.
- **What has already been completed?** Comprehensive historical logs with skip/swap audit trails and expense tracking.
- **How do groups stay synchronized?** Lightweight shared rooms with QR-code and room-code joining.
- **What fits your visual style?** Three distinct switchable presentation engines — spearheaded by **Liquid Glass (iOS-Style Refraction)**, alongside **Material 3 Expressive** and **Gaussian Blur**.

---

## 🎬 In Action

<details>
  <summary><b>🎥 Click to view Interactive Motion Demo (Liquid UI)</b></summary>
  <br/>
  <div align="center">
    <video src="https://github.com/user-attachments/assets/ccb304f0-ed69-44ed-9fbe-5f66b68f51b7" autoplay loop muted playsinline width="340" style="border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.2);"></video>
    <p><em>Turn progression, quick completion actions, and routine switching in fluid Liquid Glass motion.</em></p>
  </div>
</details>

---

## ✨ Features

### 🔄 Intelligent Turn Rotation
- **Strict Turn Fairness:** Automatically rotates to the next participant upon turn completion.
- **Interactive Actions:** Complete turns with a single tap, record optional expense amounts, swap turns with another participant, or skip turns when schedules change.
- **Overdue & Today Prioritization:** Highlights urgent routines at the top of the Today dashboard.

### 📅 Recurring Schedules & Calendar
- **Flexible Recurrence:** Create daily, weekly (custom weekday selection), monthly (specific day-of-month), or interval-based routines.
- **Schedule Pausing:** Temporarily pause schedules with automatic resume dates without breaking rotation histories.
- **Visual Calendar:** Month-view calendar displaying scheduled occurrences, pending assignments, and completion states day by day.

### 📜 History & Audit Trails
- **Occurrence History:** Searchable, filterable ledger of every completed, skipped, or swapped turn.
- **Financial Turn Support:** For shared expenses (such as grocery or utility rotations), tracks default currency amounts and custom paid amounts per occurrence.

### 👥 Shared Rooms (Local & P2P-Ready)
- **Room Creation & Joining:** Generate 6-character room codes or camera-scannable QR codes using ZXing & CameraX.
- **Participant Roles:** Owner and member roles with live member presence indicators.
- **Offline-First Architecture:** Local-first storage backed by Room with an in-memory broadcast event bus for zero-dependency local network synchronization.

### 🔔 Smart Reminders & In-App Updates
- **Proactive Notifications:** Daily reminders scheduled via `AlarmManager` and `WorkManager` with direct notification action intents.
- **Built-in GitHub Release Updater:** Automatically checks for new GitHub releases via `UpdateManager`, downloads release APKs securely, and launches the system package installer.

---

## 🎨 Three Distinct UI Presentation Modes

Whozit features three fully realized design systems switchable on the fly from **Settings → Appearance**:

| 💎 Liquid Glass (Flagship Experience) | 🎨 Material 3 Expressive | 🌫️ Gaussian Blur |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/1c589cae-bf0b-47d4-a3bc-2f5e5e9d287b" width="220" /> | <img src="https://github.com/user-attachments/assets/7a72d616-cb16-4d92-ba8e-ae8e0d20061e" width="220" /> | <img src="https://github.com/user-attachments/assets/b8c394c6-3280-431e-ada7-d9bd15d04049" width="220" /> |
| Native real-time AGSL shader refraction, specular highlights, dynamic chromatic aberrations, and reactive wallpaper backgrounds. | Google M3E design tokens with dynamic fluid shapes, expressive typography, and spring physics. | Soft multi-layered Gaussian depth, translucent glass card elevations, and custom wallpaper tinting. |

---

## 📸 Screenshots & Visual Showcase

### 💎 Liquid UI Showcase & Motion Demonstrations
<details>
<summary><b>🔍 Click to view Liquid Glass Screens & Motion Demos (Flagship)</b></summary>
<br/>

<div align="center">
  <img src="https://github.com/user-attachments/assets/855836b1-8bfb-42f5-a9ef-f9a855d9e17d" alt="Liquid UI Collage" width="90%" style="border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,0.15);" />
</div>

<br/>

| Interactive Liquid Navigation | Dynamic Background Customization |
| :---: | :---: |
| <video src="https://github.com/user-attachments/assets/ccb304f0-ed69-44ed-9fbe-5f66b68f51b7" autoplay loop muted playsinline width="320" style="border-radius: 12px;"></video> | <video src="https://github.com/user-attachments/assets/454d4174-53df-48ad-8492-91d6ddc49d38" autoplay loop muted playsinline width="320" style="border-radius: 12px;"></video> |

<br/>

| Dashboard | Calendar | Schedules |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/1c589cae-bf0b-47d4-a3bc-2f5e5e9d287b" width="260" alt="Liquid Dashboard" /> | <img src="https://github.com/user-attachments/assets/36dcfba9-576a-4b92-b343-2776272a9422" width="260" alt="Liquid Calendar" /> | <img src="https://github.com/user-attachments/assets/66c84187-fd03-42ce-a5b2-9c1cdc9bd4d4" width="260" alt="Liquid Schedules" /> |

| History | Rooms | Settings |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/d3e17eb4-aec2-40db-a14a-7d92c14f1d92" width="260" alt="Liquid History" /> | <img src="https://github.com/user-attachments/assets/8207c8bb-b4ad-41ad-9290-6ff1a9b81fcf" width="260" alt="Liquid Rooms" /> | <img src="https://github.com/user-attachments/assets/966942e6-6b53-47e3-9409-1104c03fd0ed" width="260" alt="Liquid Settings" /> |

</details>

### 🎨 Material 3 Expressive Showcase
<details>
<summary><b>🔍 Click to view Material 3 Expressive Screens & Motion Demo</b></summary>
<br/>

<div align="center">
  <img src="https://github.com/user-attachments/assets/4143bb73-8bb0-4890-b099-e58e5d541ba3" alt="Material 3 Expressive Collage" width="90%" style="border-radius: 12px;" />
</div>

<br/>

<div align="center">
  <video src="https://github.com/user-attachments/assets/1dd11527-0fb8-41c5-89c3-c8f09d36bbcf" autoplay loop muted playsinline width="320" style="border-radius: 12px;"></video>
  <p><em>Google Material 3 Expressive token-driven animations and fluid interactions.</em></p>
</div>

| Dashboard | Calendar | Schedules |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/7a72d616-cb16-4d92-ba8e-ae8e0d20061e" width="260" alt="M3E Dashboard" /> | <img src="https://github.com/user-attachments/assets/94033e16-a5fa-4fba-a106-10049f469c63" width="260" alt="M3E Calendar" /> | <img src="https://github.com/user-attachments/assets/c286bf5a-cfe7-4492-9de9-73f9d16d504c" width="260" alt="M3E Schedules" /> |

| History | Rooms | Settings |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/f80f2f80-e261-4900-8782-c9bbc2da6575" width="260" alt="M3E History" /> | <img src="https://github.com/user-attachments/assets/00ab33db-a543-4500-a73a-072e277c4ca7" width="260" alt="M3E Rooms" /> | <img src="https://github.com/user-attachments/assets/5212d01a-0bc0-41da-bf02-ba061ecc1f79" width="260" alt="M3E Settings" /> |

</details>

### 🌫️ Gaussian Blur Showcase
<details>
<summary><b>🔍 Click to view Gaussian Blur Screens & Motion Demo</b></summary>
<br/>

<div align="center">
  <img src="https://github.com/user-attachments/assets/f784d0d6-cd80-47fb-9ab7-2dbe14ce5c94" alt="Gaussian Blur Collage" width="90%" style="border-radius: 12px;" />
</div>

<br/>

<div align="center">
  <video src="https://github.com/user-attachments/assets/a55d5be5-6f6b-463a-b2b2-ac398f8c7f76" autoplay loop muted playsinline width="320" style="border-radius: 12px;"></video>
  <p><em>Gaussian depth filtering with adaptive contrast adjustments.</em></p>
</div>

| Dashboard | Calendar | Schedules |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/b8c394c6-3280-431e-ada7-d9bd15d04049" width="260" alt="Gaussian Dashboard" /> | <img src="https://github.com/user-attachments/assets/ea280e40-83f0-4d75-933e-281b62cb3eb1" width="260" alt="Gaussian Calendar" /> | <img src="https://github.com/user-attachments/assets/a3eaa5de-d3ff-4cdf-84da-f0000f3fd1dc" width="260" alt="Gaussian Schedules" /> |

| History | Rooms | Settings |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/bccf5b0c-73f7-4f34-b071-b1fcc82f84f5" width="260" alt="Gaussian History" /> | <img src="https://github.com/user-attachments/assets/546db990-f078-459a-84e7-ed8988490842" width="260" alt="Gaussian Rooms" /> | <img src="https://github.com/user-attachments/assets/7b8a30d3-db50-40a3-b9fd-34b3dd29f7b8" width="260" alt="Gaussian Settings" /> |

</details>

---

## 🏗️ Technical Architecture & Stack

Whozit follows modern Android architectural patterns:

- **Language:** [Kotlin 2.2.10](https://kotlinlang.org/) with JVM Toolchain 21 and Context Parameters
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Compose BOM `2026.06.00`)
- **Design System:** [Material 3 `1.5.0-alpha29`](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary) + AndroidX Graphics Shapes (`1.0.1`)
- **Navigation:** AndroidX Navigation Compose (`2.8.4`)
- **Persistence:** [Room Database `2.7.0-alpha13`](https://developer.android.com/training/data-storage/room) with KSP (`2.2.10-2.0.2`)
- **Preferences:** AndroidX DataStore Preferences (`1.1.1`)
- **Concurrency & Reactivity:** Kotlin Coroutines & Flow
- **Background Work:** AndroidX WorkManager (`2.9.1`) + `AlarmManager`
- **QR Codes & Camera:** ZXing Core (`3.5.3`) + CameraX (`1.4.1`)
- **Image Loading:** Coil Compose (`2.6.0`)
- **Shader & Glass Rendering:**
  - Embedded backdrop and shader engine adapted from [Kyant's](https://github.com/Kyant0) backdrop primitives (`io.github.kyant0:shapes:1.2.0`)
  - [Prismal `1.0.5`](https://github.com/styropyr0/Prismal) for OpenGL ES glass-morphism parity

---

## 🛠️ Building from Source

### Prerequisites
- **JDK 21** (Eclipse Temurin, Azul Zulu, or Amazon Corretto)
- **Android SDK** with Platform `37` (Android 16 DP) and Build Tools installed
- Minimum Device / Emulator Version: **Android 12 (API 31)** or higher (AGSL shaders require API 33+ for advanced refraction effects)

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/theheartbreakid/Whozit.git
   cd Turnly
   ```

2. Assemble the debug APK:

   **Linux / macOS:**
   ```bash
   ./gradlew assembleDebug
   ```

   **Windows:**
   ```powershell
   .\gradlew.bat assembleDebug
   ```

3. The generated APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📦 Releases & Updates

Automated release builds are built via GitHub Actions CI on every release tag.

- Download the latest signed APK directly from the [Releases Page](https://github.com/theheartbreakid/Whozit/releases).
- Whozit includes an internal **Check for Updates** mechanism in **Settings → About** that checks the GitHub Releases API and guides you through updating without requiring third-party app stores.

---

## 🤖 How It Was Built (AI-Assisted Development)

Whozit was built through an **AI-assisted, vibe-coded development workflow**, with manual architectural guidance, bug fixing, and continuous refinement. 

The project explores how AI-assisted programming can unite ambitious mobile interface paradigms (such as AGSL runtime shaders, real-time backdrop blur, and fluid glass dynamics) with practical offline-first routine management in Kotlin and Jetpack Compose.

---

## 💖 Open-Source Acknowledgements

Whozit proudly builds upon and draws inspiration from exceptional open-source libraries:

- **[AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)** by [Kyant](https://github.com/Kyant0) and contributors  
  *Heartfelt thanks to Kyant for open-sourcing the Liquid Glass effect and shape components for Compose. Their innovative AGSL runtime shaders and backdrop capture techniques made Whozit’s Liquid Glass mode possible.*

- **[Prismal](https://github.com/styropyr0/Prismal)** by [styropyr0](https://github.com/styropyr0) and contributors  
  *Thank you to styropyr0 for Prismal, an Android glass-morphism library built with OpenGL ES, which provides rock-solid visual parity across diverse rendering pipelines.*

- **[Haze](https://github.com/chrisbanes/haze)** by [Chris Banes](https://github.com/chrisbanes) and contributors  
  *We credit and thank Chris Banes for Haze, which established the golden standard and conceptual foundation for backdrop blurring and translucent surface treatments in Jetpack Compose.*

*All product names, logos, and brands are property of their respective owners.*

---

## 🤝 Contributing

Contributions, feedback, and issue reports are warmly welcomed! Please review our [Contribution Guidelines](CONTRIBUTING.md) before opening an issue or pull request.

---

## ⚖️ License
 
Whozit is open-source software licensed under the **[Apache License 2.0](LICENSE)**.
 
```text
Copyright 2026 Mohsin Mulla and contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
