# Testverslag: trainer-app met de Supabase-database

Getest op 3 november 2026 door Younes, op branch `feature/supabase-backend`.

Ik heb getest of Berkans schermen goed samenwerken met mijn database. Hieronder staat wat ik gedaan heb, wat eruit kwam en wat ik nog niet getest heb.

## 1. Automatische tests

```bash
cd trainer-app
./gradlew :composeApp:desktopTest
```

Resultaat: **12 tests, 0 mislukt.**

| Wat | Aantal | Waar het over gaat |
|-----|--------|--------------------|
| `SquadInsightsTest` | 9 | kleuren bij een stat en bij het totaalcijfer, wie aandacht nodig heeft, welke thema's je traint, de volgende trainingsdag (ook in het weekend) |
| `ScreenSnapshotTest` | 3 | alle schermen worden zonder fouten getekend en als plaatje bewaard |

Daarnaast bouwde de Android-app zonder fouten: `./gradlew :androidApp:assembleDebug`.

## 2. De database vanaf nul opbouwen

```bash
supabase db reset
```

Alle drie de migraties en de seed zijn zonder fouten toegepast. Daarna stonden er 18 clubspelers, 8 stats en 23 positiegewichten in.

## 3. De regels van de database

Ik heb dit in een transactie getest met twee gebruikers (A en B) en één bezoeker zonder login. Daarna heb ik alles teruggedraaid, dus er bleef niets achter.

| Test | Verwacht | Gekregen |
|------|----------|----------|
| A maakt het demo-team | 16 spelers, 3 wedstrijden | 16 spelers, 3 wedstrijden |
| A heeft een totaalcijfer per speler | 16 | 16 |
| Stats liggen tussen 1 en 20, totaal tussen 1 en 99 | ja | ja |
| A slaat het team opnieuw op | zelfde team, spelers blijven | zelfde team, 16 spelers, uitnodigingscode blijft hetzelfde |
| B kijkt naar teams, spelers, kaartjes en oefeningen van A | niets | 0, 0, 0, 0 |
| B zet een speler in het team van A | geweigerd | geweigerd door de beveiliging |
| Bezoeker zonder login ziet teams | niets | 0 |
| Bezoeker zonder login slaat een team op | geweigerd | geweigerd |

## 4. De app op de emulator (lokale database)

Met een Android-emulator (API 37) en de lokale database:

- Team met naam en trainingsdagen opslaan: het team stond meteen in de database.
- Vier spelers toevoegen (keeper, verdediger, middenvelder, aanvaller): alle vier stonden in de database.
- De app helemaal afsluiten en weer openen: team en spelers waren er nog. Hier vond ik een fout (zie hieronder).
- Selectie: elke speler heeft een gekleurd totaalcijfer (45 geel, 80 donkergroen).
- Vandaag: echte datum, en "Vraagt aandacht" liet de speler met een rode stat zien.
- Spelerspagina: acht stats met gekleurde balken. Ik noteerde 70 van 100 bij Afwerken en daarna stond die stat op 14.

## 5. De online database

- De derde migratie staat online (`supabase migration list` toont alle drie).
- Met echte aanvragen (anoniem inloggen, team opslaan, nog een keer opslaan, oefening noteren, kaartjes lezen, het demo-team): alles werkt.
- Zonder login zie je niets en kun je niets opslaan.

## Fouten die ik gevonden heb en opgelost

1. **Opnieuw opslaan van het team wiste de spelers en hun oefeningen.** Oorzaak: de app toont bij elke start het teamscherm en `save_team` maakte het team opnieuw aan. Oplossing: migratie `20261103190000_save_team_keeps_players.sql`, die het bestaande team alleen aanpast.
2. **Elke start van de app maakte een nieuwe anonieme gebruiker, dus de gebruiker zag zijn team niet meer.** Oorzaak: de app keek te vroeg of er al een sessie was. Oplossing: eerst wachten tot de opgeslagen sessie geladen is.

## Wat ik nog niet getest heb

- De app op de emulator met de **online** database. De emulator had geen internet. De code zelf heb ik wel tegen de online database laten draaien vanaf mijn computer.
- Een **echte telefoon**.
- **iOS**: daar zijn de database-instellingen nog niet gemaakt.
- **De AI-assistent**: die heeft nog geen API en geen sleutel. Het "concept" is wat de trainer zelf invult.
- Meerdere trainers tegelijk en veel data (alleen kleine hoeveelheden).

## Zelf herhalen

1. `supabase start` en `supabase db reset` in de hoofdmap.
2. In `trainer-app/local.properties`: `supabase.url=http://10.0.2.2:54321` en `supabase.key=` met de publishable key uit `supabase status`.
3. `cd trainer-app && ./gradlew :composeApp:desktopTest :androidApp:installDebug`.
4. Open de app op de emulator en doorloop de stappen bij punt 4.
