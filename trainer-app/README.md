# Trainer app (Kotlin · Compose Multiplatform)

Mobile app for trainers: set up the team, manage players, note training results
and follow every player's stats (1–20) and overall (1–99). Built with Compose Multiplatform so the
same UI code runs on Android and iOS. All data comes from the Supabase database in `../supabase`.

## Structure

- `composeApp/` — shared Kotlin/Compose code (`commonMain`): screens, theme, data layer.
  - `data/TeamRepository.kt` — what the screens need from the backend.
  - `data/SupabaseTeamRepository.kt` + `SupabaseRemote.kt` — the real implementation on the Supabase database.
  - `data/Stats.kt` — stats, drills, matches and the colour levels; `data/SquadInsights.kt` — who needs attention, what to train, next training day.
- `androidApp/` — Android entry point (`MainActivity`).
- `composeApp/src/iosMain` — iOS entry point (`MainViewController`); an Xcode project is added once Xcode is installed.

## Database

The database (tables, security rules, the stats calculation) lives in `../supabase` as SQL migrations.
Start it locally with Docker and the Supabase CLI:

```bash
cd ..            # repository root
supabase start   # prints the API URL and the publishable key
supabase db reset
```

Then put the address and the **publishable** key in `trainer-app/local.properties` (this file is not in git; never use the secret key):

```
supabase.url=http://10.0.2.2:54321
supabase.key=sb_publishable_...
```

`10.0.2.2` is how the Android emulator reaches your computer. For the desktop preview set the environment
variables `SUPABASE_URL` and `SUPABASE_KEY` instead. Without these settings the app shows a message instead of starting.
There is no login screen yet: the app signs in anonymously and every installation gets its own team.

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

From the design "AI Football Coach - Trainer App v2". The design is in English; all app text is Dutch.

| # | Screen |
|---|--------|
| 11 | Onboarding · welcome |
| 12 | Onboarding · your team (step 1 of 3) |
| 13 | Onboarding · add players (step 2 of 3) |
| 14 | Onboarding · invite players (step 3 of 3) |
| 02 | Today (trainer dashboard) |
| 03 | Squad |
| 04 | Player (development points) |
| 15 | Add a development point (sheet) |
| 16 | Log an observation after training (sheet) |
| 17 | Brief · step 1 — when & who |
| — | Brief · step 2 — focus (not in the design; AI-suggested themes) |
| 18 | Brief · step 3 — conditions |
| 19 | Drafting |
