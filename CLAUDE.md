# CLAUDE.md

## Build Commands

```bash
./gradlew :app:assembleTennisDebug       # Build tennis phone APK
./gradlew :app:assembleBadmintonDebug     # Build badminton phone APK
./gradlew :app:assemblePickleballDebug    # Build pickleball phone APK
./gradlew :app:installTennisDebug         # Install tennis app
./gradlew :wear:installTennisDebug        # Install tennis wear app
./gradlew :app:testTennisDebugUnitTest    # Run tests for tennis flavor
./gradlew :app:testDebugUnitTest          # Run tests for all flavors
./gradlew ktlintCheck detekt              # Run linters (ktlint + detekt)
./gradlew assembleDebug                   # Build all flavors (phone + wear)
./gradlew :app:preparePlayStoreScreenshotsTennis    # Generate + copy screenshots for Play Store
./gradlew :app:publishTennisReleaseBundle            # Publish phone AAB + metadata + screenshots
./gradlew :wear:publishTennisReleaseBundle           # Publish wear AAB to wear:internal track
```

Install tasks depend on `test{Flavor}DebugUnitTest`, `ktlintCheck`, `lint{Flavor}Debug`, and `detekt` — all checks run automatically before install.

Three product flavors (`tennis`, `badminton`, `pickleball`) across all modules. Each produces a separate APK with distinct applicationId, app name, icon, and sport default.

## Linting

Always run `./gradlew ktlintCheck detekt` after making changes. ktlint and detekt are configured at the root `build.gradle.kts` and applied to all subprojects.

## Architecture

Multi-module Android app (app + shared + wear) built with Kotlin and Jetpack Compose (min SDK 26, wear min SDK 30). Package: `com.nuttyknot.tennisscoretracker`.

### Scoring Engine

- **`TennisScoreState.kt`** — Immutable data classes: `TennisMatchState` (full match state), `PlayerScore` (sealed class: Love/Fifteen/Thirty/Forty/Advantage/TiebreakScore), `MatchFormat` enum. Also contains pure announcement-generation functions.
- **`ScoreModel.kt`** — ViewModel holding `StateFlow<TennisMatchState>`. Contains the `Processor` inner class that implements the scoring state machine and a `ScoringLogic` object for deuce/advantage/regular point transitions. Uses an `ArrayDeque` history stack for undo.
- Three match formats: STANDARD (best-of-3), LEAGUE (best-of-3, 3rd set is 10-point match tiebreak), FAST (single 8-game pro set, no advantage scoring).

### Input

- **`KeyEventManager.kt`** — Coroutine-based temporal debouncing to distinguish single click, double click, and long press from a Bluetooth HID button.
- **`MainActivity.kt`** — Single activity, dispatches key events and hosts the Compose navigation.

### UI (Jetpack Compose)

- **Navigation**: `TennisAppNavigation.kt` with routes defined in `Routes.kt` (score, settings, help, match_summary).
- **Screens**: `ScoreScreen`, `SettingsScreen`, `HelpScreen`, `MatchSummaryScreen`.
- **Settings**: `SettingsManager.kt` uses Jetpack DataStore Preferences for player names, key codes, latency thresholds, and theme.
- **TTS**: `TtsManager.kt` — UK English text-to-speech for umpire-style score announcements.

### Testing

Unit tests in `app/src/test/` (`ScoreModelTest.kt`, `TennisScoreStateTest.kt`, `KeyEventManagerTest.kt`) and `shared/src/test/` (`WearScoreDisplayTest.kt`). Uses JUnit 4 and `kotlinx-coroutines-test`.

### Wear OS

- **`shared/`** — Android library module with `WearConstants` (data paths/commands) and `WearScoreDisplay` (JSON-serializable score data class shared between phone and watch)
- **`wear/`** — Standalone Wear OS app (min SDK 30, Compose Material3 for Wear). `WearMainActivity` with ambient mode, `WearRemoteViewModel` (DataClient listener + MessageClient sender), `WearScoreScreen` (tap left=user point, right=opponent, long-press=undo)
- **`WearSyncManager.kt`** (in app module) — Pushes `TennisMatchState` to watch via Wearable Data Layer API, receives commands via `MessageClient`
- **`WearDataListenerService.kt`** (in wear module) — Background service that receives data/messages from the phone app

## Rules

- Never commit or push without explicit user permission.
- Always run linters and fix issues before presenting work as complete.
