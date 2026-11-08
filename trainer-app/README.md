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

De app leest alles uit de Supabase-database in `../supabase`. Uitleg staat in `../docs/database.md`.
Kort: start de database met `supabase start` in de hoofdmap en zet in `trainer-app/local.properties` (staat niet in git)
het adres en de **publieke** sleutel, nooit de geheime:

```
supabase.url=http://10.0.2.2:54321
supabase.key=sb_publishable_...
```

`10.0.2.2` is hoe de Android-emulator jouw computer bereikt. Voor de desktop-preview zet je de omgevingsvariabelen
`SUPABASE_URL` en `SUPABASE_KEY`. Zonder die instellingen toont de app een melding. Er is nog geen inlogscherm:
de app logt anoniem in en elke installatie krijgt zijn eigen team.

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
| 04 | Player (stats, drills and matches) |
| 15 | Record a drill (sheet; replaces the design's development point + observation sheets) |
| 17 | Brief · step 1 — when & who |
| — | Brief · step 2 — focus (not in the design; themes from the squad's stats) |
| 18 | Brief · step 3 — conditions |
| 19 | Drafting |
| 07 | Review the draft, one block at a time |
| 20 | Edit a block |
| 21 | Ready to send |
