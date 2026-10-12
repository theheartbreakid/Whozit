# Contributing to Whozit

Thank you for your interest in contributing to **Whozit**! Whozit is an open-source Android turn and schedule manager for recurring routines, built using Kotlin and Jetpack Compose.

---

## 🛠️ Development Setup & Prerequisites

Before submitting changes, ensure your local development environment meets the following requirements:

- **JDK**: Java Development Kit 21 (Temurin, Zulu, or Corretto recommended; toolchain configured to JVM 21)
- **Android Studio**: Android Studio Ladybug (2024.2.1+) or Meerkat recommended
- **Android SDK**:
  - `compileSdk`: **37**
  - `minSdk`: **31** (Android 12+)
  - `targetSdk`: **36**
- **Gradle**: Uses the Gradle Wrapper (`./gradlew`) included in the repository

### Building from Source

To compile and verify debug builds locally:

**Linux / macOS:**
```bash
./gradlew assembleDebug
```

**Windows (PowerShell / Command Prompt):**
```powershell
.\gradlew.bat assembleDebug
```

To run instrumented or unit tests:
```bash
./gradlew test
```

---

## 🔄 Contribution Workflow

1. **Fork the Repository**: Create your personal fork on GitHub.
2. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bugfix-description
   ```
3. **Make Focused Changes**: Keep Pull Requests focused on a single responsibility or bug fix. Avoid unrelated formatting sweeps across large files.
4. **Preserve Business Logic & Data Integrity**:
   - Verify that changes to Room entities (`Entities.kt`) include appropriate database migration strategies or schema testing.
   - Maintain schedule rotation rules, recurrence calculation logic, and conflict resolution behavior.
5. **Verify UI Changes**:
   - Test across all three UI runtime modes (**Material 3 Expressive**, **Liquid Glass**, and **Gaussian Blur**).
   - Ensure semantic tokens in `WhozitVisualTokens.kt` are respected (avoid hardcoding raw colors like `Color.White` or `Color.Black`).
   - Include before-and-after screenshots or screen recordings with your Pull Request.
6. **Submit a Pull Request**:
   - Provide a clear title and description explaining what was changed and why.
   - For bug fixes, include detailed reproduction steps.

---

## 📜 Third-Party Libraries & Licensing

Whozit incorporates and builds upon remarkable open-source libraries:
- **[AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)** by Kyant
- **[Prismal](https://github.com/styropyr0/Prismal)** by styropyr0
- **[Haze](https://github.com/chrisbanes/haze)** by Chris Banes (conceptual inspiration for backdrop blurring)

When contributing:
- Respect the licensing and copyright notices of all upstream libraries.
- Avoid introducing heavy third-party dependencies unless strictly necessary and discussed beforehand.
- Never introduce placeholder, simulated, or fake APIs where functional implementations are expected.

---

## 🤝 Code of Conduct & Vibe-Coded Transparency

Whozit was developed using an AI-assisted, vibe-coded workflow paired with manual architectural refinement, testing, and debugging. We welcome human developers and AI-assisted contributors alike! Clear communication, constructive reviews, and thoughtful technical feedback are deeply appreciated.

---

## ⚖️ License

By contributing to Whozit, you agree that your contributions will be licensed under the project's [Apache License 2.0](LICENSE).

