# AI Football Coach

Een app voor voetbaltrainers, om hun spelers beter te maken. Het werkt een beetje als een voetbalmanager-spel:
de trainer noteert oefeningen, de app laat per speler stats van 1 tot 20 zien (20 is het best) en een
totaalcijfer van 1 tot 99, zoals een FIFA-kaartje. Kleuren laten in één oogopslag zien wie sterk of zwak is.
Zo blijven de spelers gemotiveerd om beter te worden. Later moet een AI-assistent de stats lezen en advies geven.

Dit is een groepsproject voor Fontys. We zijn met z'n tweeën: Berkan en ik (Younes).

## Wie doet wat

| Deel | Wie |
|------|-----|
| De schermen en het ontwerp van de app | Berkan |
| De database, de stats en de koppeling met de app | Younes |

## Waar staat wat

- `trainer-app/`: de app voor trainers (Kotlin, Compose Multiplatform). Zie `trainer-app/README.md`.
- `supabase/`: de database (tabellen, beveiliging, berekening van de cijfers, testdata).
- `docs/database.md`: mijn uitleg over de database.
- `docs/testverslag.md`: wat ik getest heb en wat daaruit kwam.
- `CONTRIBUTING.md`: hoe Berkan en ik samenwerken.

## Zo draai je het

1. Start de database: `supabase start` en dan `supabase db reset` (Docker moet aan staan).
2. Zet het adres en de publieke sleutel in `trainer-app/local.properties`. Hoe dat moet staat in `trainer-app/README.md`.
3. Start de app: `cd trainer-app && ./gradlew :androidApp:installDebug`.

## Hoe het er nu voor staat

- Teams, spelers, oefeningen, wedstrijden en de cijfers staan in de database en werken in de app.
- Er is nog geen echt inlogscherm. De app logt anoniem in, en elke installatie heeft zijn eigen team.
- De wedstrijden in de database zijn nog vaste testdata.
- De AI-assistent bestaat nog niet. Er is nog geen API en geen sleutel. Het "concept" in de app is wat de trainer zelf invult.

## AI-gebruik

Ik gebruik AI-hulp bij het schrijven van code en teksten voor dit project. Alles wat ik toevoeg test ik zelf
voordat het in `main` komt (zie `docs/testverslag.md`).
