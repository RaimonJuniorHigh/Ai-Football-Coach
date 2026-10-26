# Trainer app (Kotlin · Compose Multiplatform)

Mobile app for trainers: set up the team, manage players and development points,
generate and approve AI training sessions. Built with Compose Multiplatform so the
same UI code runs on Android and iOS.

## Structure

- `composeApp/` — shared Kotlin/Compose code (`commonMain`): screens, theme, data layer.
  - `data/TeamRepository.kt` — `MockTeamRepository` holds in-memory data until the backend API exists.
- `androidApp/` — Android entry point (`MainActivity`).
- `composeApp/src/iosMain` — iOS entry point (`MainViewController`); an Xcode project is added once Xcode is installed.

## Run on Android

Needs Android Studio (for the SDK and its bundled JDK).

```bash
./gradlew :androidApp:installDebug
```

Or open `trainer-app/` in Android Studio and run the `androidApp` configuration.

## Preview on desktop

```bash
./gradlew :composeApp:run            # phone-sized window with the app
./gradlew :composeApp:desktopTest    # renders each screen to composeApp/build/snapshots/*.png
```

## Screens implemented

From the Claude design "AI Football Coach - Trainer App v2":

| # | Screen |
|---|--------|
| 11 | Onboarding · welcome |
| 12 | Onboarding · your team (step 1 of 3) |
| 13 | Onboarding · add players (step 2 of 3) |
