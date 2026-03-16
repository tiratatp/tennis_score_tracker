# CLAUDE.md

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew testDebugUnitTest      # Run unit tests
./gradlew ktlintCheck detekt     # Run linters (ktlint + detekt)
./gradlew installDebug           # Install on device (also runs tests + linters via task dependency)
```

`installDebug` depends on `testDebugUnitTest`, `ktlintCheck`, `lintDebug`, and `detekt` — all checks run automatically before install.

## Linting

Always run `./gradlew ktlintCheck detekt` after making changes. ktlint and detekt are configured at the root `build.gradle.kts` and applied to all subprojects.

## Architecture

Single-activity Android app (Kotlin, Jetpack Compose, min SDK 26). Package: `com.nuttyknot.tennisscoretracker`.

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

Unit tests in `app/src/test/`: `ScoreModelTest.kt`, `TennisScoreStateTest.kt`, `KeyEventManagerTest.kt`. Uses JUnit 4 and `kotlinx-coroutines-test`.

## Rules

- Never commit or push without explicit user permission.
- Always run linters and fix issues before presenting work as complete.
