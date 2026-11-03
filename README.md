> **Archief.** Dit is mijn eerste, losse Android-testapp: alleen de backend met een kaal testscherm.
> De huidige app staat in `trainer-app/` op `main` (de schermen van Berkan met mijn database), en de database in `supabase/`.
> Ik bewaar dit voor de uitleg en de 64 tests. De database-mappen waar hieronder naar verwezen wordt, staan daar.

# VoetbalAI

Android-app (Kotlin) voor voetbaltrainers: team opzetten, aanwezigheid bijhouden en met de AI chatten over de training.

Dit project bevat nu alleen de **backend**. De enige UI is een kaal testscherm (`BackendTestActivity`) om de backend uit te proberen, zonder thema, kleuren, lettertypes of navigatie. De echte schermen komen later in `ui/`.

**Alle gegevens staan in een Supabase-database** (PostgreSQL), niet meer in de app. De app bevat geen testdata meer: het demo-team en de clublijst staan in de database. De app logt zonder scherm anoniem in, zodat elke telefoon een eigen team heeft.

## De database (Supabase)

De database staat in de map `supabase/` (die staat naast deze map, in `Groepsproject Sem2/`):
- `supabase/migrations/…create_teams_and_players.sql`: de tabellen `teams`, `players` en `club_players`, de beveiliging (RLS) en twee functies: `save_team` (team + spelers in één keer opslaan) en `create_demo_team` (het demo-team JO17-1 met 16 spelers).
- `supabase/migrations/…add_stats_training_matches.sql`: het voetbalmanager-gedeelte (zie hieronder).
- `supabase/seed.sql`: de clublijst (8 verzonnen spelers), wordt bij een lokale `supabase db reset` gevuld.
- `supabase/config.toml`: instellingen, waaronder anonieme aanmelding (`enable_anonymous_sign_ins = true`).

### Spelersstatistieken (voetbalmanager)

Het idee: de trainer legt **trainingsresultaten** vast (bijvoorbeeld een spits: afwerken, 100 schoten, 100 goals). Uit die resultaten rekent de **database** de cijfers uit, zodat de app en de website precies dezelfde cijfers tonen.

| Wat | Waar | Uitleg |
|---|---|---|
| `stat_types` | opzoektabel | de 8 stats: afwerken, passen, dribbelen, verdedigen, snelheid, conditie, kracht, keepen |
| `position_weights` | opzoektabel | hoe zwaar elke stat meetelt per positie (bijvoorbeeld afwerken telt 35% voor een aanvaller) |
| `drill_results` | tabel | één oefening: speler, stat, pogingen en gelukt |
| `matches`, `match_players` | tabellen | wedstrijden en wat elke speler deed (minuten, goals, assists) |
| `player_stats` | view | **stat 1 tot 20** (20 is het best): 0% geslaagd is 1, 100% is 20 |
| `player_cards` | view | **totaalcijfer 1 tot 99** per speler, volgens de gewichten van zijn positie (zoals een FIFA-kaartje) |

De kleuren (1-4 rood, 5-8 oranje, 9-12 geel, 13-16 lichtgroen, 17-20 donkergroen) staan in `domain/stats/RatingColors.kt`. Het demo-team (`create_demo_team`) krijgt vaste oefeningen en 3 wedstrijden, met verschil tussen sterke en zwakke spelers. De chatbot-API ontbreekt nog; later kan de AI deze cijfers lezen en adviezen geven.

**Lokaal draaien** (Docker moet aan staan):

```bash
cd "Groepsproject Sem2"
supabase start        # database lokaal starten
supabase db reset     # tabellen en clublijst opbouwen
supabase status       # laat het adres en de sleutels zien
```

**De app koppelen** met `local.properties` (staat niet in git, per computer anders):

```
supabase.url=http://10.0.2.2:54321
supabase.key=<publishable key uit `supabase status`>
```

`10.0.2.2` is "mijn computer" vanuit de emulator. Voor de online database zet je hier het `https://…supabase.co`-adres en de publishable key van dat project. Gebruik nooit de `service_role` / secret key in de app.

**Android 17 (API 37) en lokale databases:** Android blokkeert verbindingen met adressen op het eigen netwerk tenzij de app de permissie `ACCESS_LOCAL_NETWORK` heeft (staat in de manifest). In de emulator geef je die één keer met:

```bash
adb shell pm grant com.example.voetbalai android.permission.ACCESS_LOCAL_NETWORK
```

Zonder die permissie loopt elke aanvraag op een time-out. Bij de online database (gewone internetadressen) is dit niet nodig.

**Online zetten:** `supabase link --project-ref <ref>` en `supabase db push`. De clublijst uit `seed.sql` komt niet vanzelf online; vul `club_players` daar zelf (of gebruik `supabase db push --include-seed`). Zet in het dashboard onder Authentication → Sign In / Providers de **Anonymous sign-ins** aan.

Bouwen: `./gradlew assembleDebug`, tests: `./gradlew testDebugUnitTest`.

## Mappenstructuur

Code staat in `app/src/main/java/com/example/voetbalai/`.

```
voetbalai/
├── AppContainer.kt          kiest welke onderdelen de app gebruikt (repository, chat-API)
├── VoetbalApp.kt            Application-klasse, maakt de AppContainer aan
├── domain/                  het voetbalwereldje, zonder Android of database
│   ├── model/               Player, Team, Position, Attendance, Day (Models.kt)
│   ├── repository/          TeamRepository (interface: wat kan de opslag?)
│   ├── stats/               StatType, PlayerCard, PlayerProfile en de kleuren (RatingColors)
│   ├── rules/               TeamRules (13 spelers, minimaal per positie)
│   ├── schedule/            volgende training en begroeting (TrainingSchedule.kt)
│   └── chat/                ChatApi (interface) en FakeChatApi (tijdelijk nepantwoord)
├── data/                    opslag in Supabase en SharedPreferences
│   ├── remote/              praat met de database (TeamRemoteDataSource + SupabaseRemote)
│   ├── repository/          SupabaseTeamRepository (de echte uitvoering)
│   └── prefs/               onthoudt of de onboarding klaar is
├── viewmodel/               toestand en acties per scherm
│   ├── today/               TodayViewModel + TodayUiState
│   ├── chat/                ChatViewModel + ChatUiState
│   ├── stats/               PlayerStatsViewModel + PlayerStatsUiState (kaartjes en spelersprofiel)
│   ├── onboarding/          OnboardingViewModel + OnboardingUiState
│   ├── app/                 AppViewModel (waar start de app?)
│   ├── welcome/             WelcomeViewModel
│   └── AppViewModelFactory.kt
└── ui/test/                 het kale testscherm met tabbladen (mag vervangen worden)
```

| Map | Inhoud | Wie past dit aan |
|---|---|---|
| `domain/` | modellen, regels, interfaces en de chat-API (interface + nepantwoord) | backend |
| `data/` | Supabase-verbinding, `SupabaseTeamRepository` | backend |
| `viewmodel/` | ViewModels met één `UiState` en acties | backend |
| `ui/test/` | het testscherm (`BackendTestActivity`, `BackendTestScreen`, `TestTabs`) | **frontend** |
| `AppContainer.kt`, `VoetbalApp.kt` | kiest de implementaties en start de app | backend |

In de code staat bij elk bestand een stukje uitleg (zoeken op `UITLEG`).

Buiten de code: `docs/ontwerp-schermen.png` is het ontwerp van de schermen die de frontend gaat bouwen (welkom, team, spelers, uitnodigingslink). `WelcomeViewModel`, `AppViewModel` en `OnboardingViewModel` zijn daarvoor gemaakt.

**Namen:** alle namen in de code (klassen, functies, variabelen, tests) zijn Engels. Wat een gebruiker op het scherm ziet (zoals foutmeldingen en de begroeting) is Nederlands, en de uitleg in de code (`UITLEG`) ook.

> **`ui/` mag volledig worden vervangen door de frontend-collega.** Niets buiten `ui/` verwijst ernaar, behalve de `<activity>` in `AndroidManifest.xml` (nu `.ui.test.BackendTestActivity`). Verwijder `ui/test/` gerust, bouw nieuwe schermen en pas de manifest-verwijzing aan.

Afhankelijkheden lopen één kant op: `ui → viewmodel → domain ← data`. `viewmodel/` en `domain/` kennen geen Compose.

## Regels voor de frontend

- Een scherm leest alleen een `UiState` en roept alleen ViewModel-acties aan. Geen database- of businesslogica in `ui/`.
- Niet zelf aanpassen: `viewmodel/`, `domain/` en `data/`. Extra data of actie nodig? Vraag het aan de backend; het gaat altijd via de `UiState`.
- ViewModels maak je met `AppViewModelFactory((application as VoetbalApp).container)`, zie `BackendTestActivity` voor een voorbeeld.
- Tijdelijke UI-toestand (bijvoorbeeld de tekst in een invoerveld voordat die wordt verstuurd) hoort in het scherm zelf.
- Navigatie, thema en Compose-extra's (bijvoorbeeld `navigation-compose`) zijn bewust verwijderd en moeten door de frontend zelf worden toegevoegd.

## Het testscherm

`ui/test/` bevat één activity met vijf tabbladen (standaard `PrimaryScrollableTabRow`, geen eigen stijl). Bovenaan staat een statusregel met de laatste actie of de foutmelding.

| Tabblad | Inhoud |
|---|---|
| Team | teamnaam, trainingsdagen, sessieduur, "Team opslaan", "Opgeslagen team laden in concept" |
| Spelers | speler toevoegen (naam + positie) aan het concept of direct aan het opgeslagen team, concept-spelers verwijderen, clublijst importeren |
| Aanwezigheid | samenvatting en per speler Aanwezig / Afwezig / Geblesseerd |
| Chat | tekstveld, "Verstuur", het gesprek eronder en "Chat leegmaken" |
| Stats | kaartjes van alle spelers (totaalcijfer 1-99, met kleur), klik op een speler voor zijn profiel (stats 1-20 met kleur, oefeningen, wedstrijden) en een knop om een test-oefening vast te leggen |
| Debug | database resetten, demo-team maken (uit de database), foutmeldingen, ruwe UiState |

## Teamregels

Een team moet op een echt team lijken (`domain/rules/TeamRules.kt`). Bij `onFinish` wordt een selectie die hier niet aan voldoet geweigerd, met een `errorMessage`:

- minimaal **13 spelers**;
- minimaal **1 keeper**, **3 verdedigers**, **3 middenvelders** en **2 aanvallers**.

Het demo-team (SQL-functie `create_demo_team`) is een geldig team van 16 spelers: 2 keepers, 5 verdedigers, 5 middenvelders en 4 aanvallers. Spelers toevoegen aan het opgeslagen team (`TodayViewModel.onPlayerAdded`) wordt niet geblokkeerd; de regels gelden bij het opslaan van het team.

## ViewModels, UiState en acties

State is een `StateFlow<…UiState>`; acties zijn gewone functies. Namen van acties en UiState-velden zijn Engels.

Domeintypes (`domain/model/Models.kt`): `Player(id, name, position, attendance)`, `Position` (`KEEPER`, `DEFENCE`, `MIDFIELD`, `ATTACK`), `Attendance` (`PRESENT`, `ABSENT`, `INJURED`), `Day` (`MON`…`FRI`), `ClubPlayer`, `ChatMessage`. Er zijn geen weergaveteksten voor deze enums; de frontend vertaalt ze zelf.

### `OnboardingViewModel`

Beheert het concept-team. Er wordt pas opgeslagen bij `onFinish`.

```kotlin
data class OnboardingUiState(
    val teamName: String,
    val trainingDays: Set<Day>,
    val sessionMinutes: Int,            // 60, 75 of 90
    val players: List<Player>,             // concept-selectie; nieuwe spelers hebben een tijdelijke (negatieve) id
    val clubList: List<ClubPlayer>,
    val inviteLink: String,          // bv. "voetbalai.app/join/k7Q2"
    val isSaving: Boolean,
    val errorMessage: String?               // null als er geen fout is
) {
    val canContinueStep1: Boolean            // naam ingevuld én minstens één dag
    val squadProblems: List<String>    // wat er aan de selectie ontbreekt (zie Teamregels); leeg = in orde
    val canSave: Boolean                // canContinueStep1 én geen squadProblems
    val clubChoices: List<ClubPlayerChoice>  // clublijst + of de speler er al in staat
}
```

| Actie | Doet |
|---|---|
| `onTeamNameChanged(name: String)` | zet de teamnaam |
| `onTrainingDayToggled(day: Day)` | zet een dag aan of uit |
| `onSessionMinutesChosen(minutes: Int)` | zet de sessielengte |
| `onPlayerAdded(name: String, position: Position)` | voegt een speler toe aan het concept (lege naam wordt genegeerd) |
| `onPlayerRemoved(playerId: Long)` | haalt een speler uit het concept |
| `onClubPlayersImported(names: Set<String>)` | voegt gekozen clubspelers toe; bestaande worden overgeslagen |
| `onEditTeam()` | laadt het opgeslagen team in het concept. Blokkeert nooit: zonder team komt er direct een `errorMessage` en blijft het concept ongemoeid |
| `onFinish(onDone: () -> Unit)` | valideert (`canContinueStep1` en de teamregels, anders `errorMessage`), slaat team en spelers op, markeert de onboarding als klaar en roept daarna de callback aan |

### `TodayViewModel`

Werkt op het opgeslagen team.

```kotlin
data class TodayUiState(
    val greeting: String,                // "Goedemiddag, trainer"
    val teamName: String,                  // "" als er geen team is
    val upcomingTraining: UpcomingTraining?, // null zonder trainingsdagen
    val players: List<Player>,             // met aanwezigheid
    val errorMessage: String?               // null als de laatste actie goed ging
) {
    val presentCount: Int
    val injuredCount: Int
    val squad: List<PositionGroup>       // spelers per positie; lege posities ontbreken
}

data class UpcomingTraining(val dateText: String, val whenText: String, val durationMinutes: Int)
```

| Actie | Doet |
|---|---|
| `onAttendanceChanged(playerId: Long, status: Attendance)` | slaat de aanwezigheid van een speler op |
| `onPlayerAdded(name: String, position: Position)` | voegt een speler toe aan het opgeslagen team (lege naam wordt genegeerd) |
| `onDatabaseReset()` | wist alle teams en spelers en de onboarding-vlag |
| `onDemoTeamCreated()` | laat de database het demo-team maken en vervangt het huidige team |

Een mislukte actie zet `errorMessage`; de volgende actie wist die weer.

### `ChatViewModel`

De chat met de AI. Het gesprek staat in de `UiState`; de trainer typt een bericht en de ViewModel vraagt de API om een antwoord, samen met de teamgegevens (teamnaam, sessieduur en spelers met aanwezigheid).

```kotlin
data class ChatUiState(
    val messages: List<ChatMessage>,      // het gesprek; ChatMessage(role: USER of ASSISTANT, text)
    val isBusy: Boolean,                    // true zolang er op een antwoord gewacht wordt
    val errorMessage: String?               // null als de laatste actie goed ging
) {
    val canSend: Boolean              // false zolang er op een antwoord gewacht wordt
}
```

| Actie | Doet |
|---|---|
| `onMessageSent(text: String)` | voegt het bericht toe, vraagt de API om een antwoord en zet dat eronder. Lege berichten worden genegeerd, en ook als er al op een antwoord gewacht wordt. Bij een fout komt er een `errorMessage` |
| `onChatCleared()` | wist het gesprek en de foutmelding |

De tekst die de trainer nog aan het typen is, hoort in het scherm zelf (zie het tabblad Chat in `TestTabs.kt`).

### `PlayerStatsViewModel`

De kaartjes en het spelersprofiel. De cijfers komen uit de database (views `player_cards` en `player_stats`).

```kotlin
data class PlayerStatsUiState(
    val cards: List<PlayerCard>,           // overzicht: naam, positie, totaalcijfer 1-99 (null zolang er geen oefeningen zijn)
    val selected: PlayerProfile?,          // gekozen speler: kaartje, stats 1-20, laatste oefeningen en wedstrijden
    val isLoading: Boolean,
    val errorMessage: String?              // null als de laatste actie goed ging
)
```

| Actie | Doet |
|---|---|
| `onCardsRequested()` | haalt de kaartjes opnieuw op (en ververst het open profiel) |
| `onPlayerSelected(playerId: Long)` | opent het profiel van een speler |
| `onSelectionCleared()` | sluit het profiel |
| `onDrillRecorded(playerId, stat, attempts, successes)` | legt een oefening vast; het cijfer van de speler verandert daardoor vanzelf |

Kleur bij een cijfer: `RatingColors.tierForStat(rating)` (1-20) of `RatingColors.tierForOverall(overall)` (1-99), met `.argb` voor de kleur.

### `AppViewModel` en `WelcomeViewModel`

`AppViewModel.onboardingDone: Boolean` zegt of de onboarding al is afgerond (voor de frontend om de startpositie te kiezen). `WelcomeViewModel` heeft alleen `WelcomeUiState(userName)` en geen acties. Het testscherm gebruikt beide niet.

## De echte API koppelen (backend)

De API is er nog niet. Het antwoord in de chat komt uit `ChatApi` (`domain/chat/Chat.kt`):

```kotlin
interface ChatApi {
    suspend fun reply(request: ChatRequest): String
}
```

`ChatRequest` bevat het hele gesprek en de teamgegevens. `FakeChatApi` geeft nu een tijdelijk antwoord ("De chat-API is nog niet gekoppeld"). Zodra de API er is, maak je een klasse die `ChatApi` uitvoert (bijvoorbeeld `RealChatApi`) en zet je die in `AppContainer.chatApi`. Een fout (zoals geen internet) mag je als `Exception` gooien; de ViewModel maakt er een `errorMessage` van. ViewModels en schermen hoeven niet te veranderen.

## Tests

`./gradlew testDebugUnitTest` draait pure JVM-tests, zonder emulator:

- `SupabaseTeamRepositoryTest`: de repository met een nepdatabase in het geheugen (`FakeTeamRemote`): opslaan, vervangen, aanwezigheid, speler toevoegen, reset, demo-team, clublijst en fouten.
- `SupabaseStatsRepositoryTest`, `PlayerStatsViewModelTest` en `RatingColorsTest`: kaartjes en profiel, oefeningen vastleggen, fouten en de kleuren.
- `TeamRulesTest`: de teamregels.
- `ChatViewModelTest`: een bericht krijgt een antwoord, lege berichten, wat de API meekrijgt, een fout van de API en de chat leegmaken.
- `OnboardingViewModelTest`, `TodayViewModelTest` (met `FakeTeamRepository`) en `TrainingScheduleTest`.
