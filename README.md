# Tennis Score Tracker

A Native Android application built with Kotlin and Jetpack Compose designed to track tennis match scores using a bluetooth remote. 

## Features
- **Hardware Interception**: The app securely intercepts raw HID KeyEvent inputs in the foreground without requiring accessible services permissions.
- **Temporal Debouncing**: Distinguishes between three events originating from a single hardware button using a precise temporal debouncing algorithm (Kotlin Coroutines)
  - Single Click: Increment your score
  - Double Click: Increment the opponent's score
  - Long Press: Undo the last score recording
- **True State Management**: Scoring logic operates as an independent finite state machine. Complex point reversals utilize an immutable Last-In-First-Out (LIFO) stack.
- **Text-To-Speech (TTS)**: The application audibly announces standard tennis terminology sequentially upon state changes.
- **Minimalist Jetpack Compose UI**: Provides high contrast visual indicators with 3 configurable themes: Grand Slam (yellow & white), Miami Night (cyan & magenta), and Colorblind Safe (orange & blue). Utilizes `BoxWithConstraints` to scale text and implements battery saving/KeepScreenOn logic.
- **Tap Input**: Supports touch screen tap input in addition to hardware button control.
- **Configurable Settings**: Player names, key bindings, and click latency thresholds are all adjustable via the settings screen.
- **Match Format**: Best-of-3 sets with tiebreak at 6-6 and proper server rotation during tiebreaks.
- **First-Run Help**: Onboarding overlay guides new users through the app on first launch.

## Project Structure
- Architecture: `Finite State Machine` with Jetpack Compose
- Hardware Debouncing: Managed by `KeyEventManager.kt` using Coroutines Delay mechanism
- Storage: Jetpack `Preferences DataStore` for variable target KeyCodes and latency thresholds.
- CI/CD: Automated Gradle building and testing using GitHub Actions (`.github/workflows/android.yml`)
- Formatting/Linting: Integrated with `Ktlint` and `Detekt` for robust style and defect detection.

## Requirements
- Android 8.0+ (API 26)

## Building and Verification
1. Open the project in Android Studio (or run through Gradle)
2. Assemble and build a debug APK:
```bash
./gradlew assembleDebug
```
3. Run Local Unit Tests:
```bash
./gradlew testDebugUnitTest
```
4. Run Linter validation:
```bash
./gradlew ktlintCheck detekt
```

## How to Install on Your Device
To install the application on your physical Android device, follow these steps:

1. **Enable Developer Options**: On your Android device, go to `Settings > About phone` and tap `Build number` seven times.
2. **Enable USB Debugging**: Go to `Settings > System > Developer options` and enable `USB debugging`.
3. **Connect Device**: Connect your device to your computer via USB.
4. **Install via Gradle**: Run the following command in your terminal:
```bash
./gradlew installDebug
```
Note: `installDebug` also runs unit tests and linters (`ktlintCheck`, `detekt`, `lintDebug`) before installing.

Alternatively, you can find the generated APK at `app/build/outputs/apk/debug/app-debug.apk` and transfer it manually to your device.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
