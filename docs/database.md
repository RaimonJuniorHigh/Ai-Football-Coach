# De database

De database staat in Supabase (een gehoste PostgreSQL-database). Alles staat als SQL-bestanden in `supabase/`, dus iedereen in het team kan dezelfde database opbouwen.

## Waarom ik dit zo gedaan heb

- **De cijfers rekent de database uit, niet de app.** Dan gebruiken de app en de website precies dezelfde cijfers, en als ik de formule verander, hoeft dat maar op één plek.
- **Stats komen uit oefeningen.** Een oefening geeft een duidelijk getal (hoeveel pogingen en hoeveel gelukt), dus je kunt spelers eerlijk vergelijken.
- **Een totaalcijfer per positie.** Een spits wordt anders beoordeeld dan een keeper. Daarom heeft elke positie eigen gewichten.
- **Klein houden.** Weinig tabellen, zodat ik het kan uitleggen en overzien.

## De tabellen

| Tabel | Wat staat erin |
|-------|----------------|
| `teams` | het team van een trainer: naam, trainingsdagen, duur, uitnodigingscode |
| `players` | de spelers van een team, met positie en of ze er zijn |
| `club_players` | een clublijst waaruit een trainer spelers kan kiezen (voor iedereen hetzelfde) |
| `stat_types` | de acht stats (afwerken, passen, dribbelen, verdedigen, snelheid, conditie, kracht, keepen) |
| `position_weights` | hoe zwaar elke stat meetelt voor het totaalcijfer van een positie |
| `drill_results` | één oefening: een speler deed 100 pogingen en 70 lukten |
| `matches` en `match_players` | wedstrijden en wat elke speler daarin deed (nu vaste testdata) |

## Hoe de cijfers werken

- **Stat (1 tot 20):** het slagingspercentage van alle oefeningen van die stat. 0% geeft 1 en 100% geeft 20. De view heet `player_stats`.
- **Totaalcijfer (1 tot 99):** het gewogen gemiddelde van de stats van een speler, volgens de gewichten van zijn positie, omgerekend naar 99. De view heet `player_cards`. Heeft een speler nog geen oefeningen, dan is het cijfer leeg.
- **Kleuren:** 1-4 rood, 5-8 oranje, 9-12 geel, 13-16 lichtgroen, 17-20 donkergroen. Het totaalcijfer wordt eerst omgerekend naar de schaal van 1 tot 20. Dat staat in de app (`data/Stats.kt`).

## De beveiliging

Elke tabel heeft row level security. Een trainer ziet en wijzigt alleen zijn eigen team, zijn eigen spelers en hun oefeningen. De views volgen dezelfde regels (`security_invoker`). Wie niet ingelogd is, ziet niets. Alleen de clublijst kan iedereen die ingelogd is lezen. Ik heb dit getest, zie `docs/testverslag.md`.

De app heeft nog geen inlogscherm. Ze logt anoniem in (`signInAnonymously`) en Supabase onthoudt die sessie op het toestel. Daarom moet "Anonymous sign-ins" aan staan in het Supabase-dashboard (Authentication, Sign In / Providers).

## Functies

- `save_team`: slaat het team op. Bestaat het team al, dan past die alleen naam, dagen en duur aan. De spelers en oefeningen blijven staan en de uitnodigingscode blijft hetzelfde.
- `replace_team`: maakt een nieuw team en gooit het oude weg. Alleen `create_demo_team` gebruikt die.
- `create_demo_team`: maakt een demo-team met 16 spelers, oefeningen en drie wedstrijden. De cijfers worden uit de naam van de speler berekend, dus ze zijn elke keer hetzelfde.

## Migraties

In `supabase/migrations` staan de wijzigingen op volgorde:

1. `create_teams_and_players`: teams, spelers, clublijst, beveiliging en de eerste functies.
2. `add_stats_training_matches`: de stats, oefeningen, wedstrijden en de twee views.
3. `save_team_keeps_players`: opnieuw opslaan van het team gooit de spelers niet meer weg.

De regel: een bestaande migratie pas ik nooit aan. Wil ik iets veranderen, dan maak ik een nieuw bestand.

## Draaien

Lokaal (Docker moet aan staan):

```bash
supabase start
supabase db reset     # bouwt alles opnieuw op en vult de clublijst
supabase status       # laat het adres en de publieke sleutel zien
```

Online (project `voetbalai-dev`):

```bash
supabase db push --remote --dry-run   # eerst kijken wat er zou gebeuren
supabase db push --remote
```

De publieke (publishable) sleutel mag in de app. De geheime sleutel (secret of service_role) staat nooit in de app, in git of in een chat.
