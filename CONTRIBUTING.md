# Samenwerken aan AI Football Coach

Twee mensen, één product. Deze afspraken zorgen dat we elkaars werk zien en elkaar niet tegenspreken.

## Wie doet wat

| Deel | Eigenaar | Waar |
|------|----------|------|
| Schermen, ontwerp, navigatie | Berkan | `trainer-app/composeApp/.../ui`, `theme`, `androidApp` |
| Database, stats en koppeling | Younes | `supabase/`, `trainer-app/composeApp/.../data` |

Je mag elkaars deel aanpassen, maar via een pull request waar de eigenaar naar kijkt (dat gaat vanzelf via `.github/CODEOWNERS`).

## Werkwijze (Scrum, kort)

- **Backlog:** elke taak is een GitHub Issue, geschreven als user story: *"Als trainer wil ik … zodat …"*.
- **Bord:** een GitHub Project met de kolommen *Te doen, Bezig, Review, Klaar*. Een kaart gaat pas naar *Klaar* als de Definition of Done klopt.
- **Sprint:** één of twee weken (afstemmen met de docent). Begin: samen plannen wat erin gaat. Eind: demo en retro.
- **Dagelijks:** korte update in de chat: wat deed ik, wat doe ik nu, waar zit ik vast?

### Definition of Done
- De app bouwt en alle tests slagen.
- Het is op een emulator of telefoon geprobeerd.
- Een ander heeft de pull request bekeken.
- De README of dit bestand is bijgewerkt als de werking verandert.

## Git

- `main` werkt altijd. Niemand pusht rechtstreeks naar `main`.
- Eén branch per taak: `feature/<naam>-<onderwerp>`, bijvoorbeeld `feature/berkan-session-review`.
- Houd branches kort (een paar dagen) en haal `main` er elke dag in (`git pull origin main`), dan blijven conflicten klein.
- Pull requests zijn klein en gaan over één onderwerp. De ander kijkt ernaar voordat er gemerged wordt.
- Commit-berichten zijn korte zinnen over wat er verandert.

## Zodat we elkaar niet tegenspreken

1. **De database is de bron van de cijfers.** Stats (1 tot 20) en totaal (1 tot 99) worden in de database berekend. De app rekent ze niet zelf uit, zodat app en website hetzelfde tonen.
2. **Database wijzigen kan alleen met een nieuw migratiebestand** in `supabase/migrations`. Een bestaand bestand pas je nooit aan. Zet de migratie in de pull request en zeg erbij dat de online database ook moet.
3. **Verandert er iets aan `TeamRepository` of de modellen in `data/`?** Zet dat bovenaan de pull request en vraag de ander om te kijken. Daar hangen alle schermen aan.
4. **Nieuw scherm dat data nodig heeft?** Vraag het eerst in een issue. Dan maakt de een de data klaar terwijl de ander het scherm bouwt.
5. **Geen sleutels in git.** De database-sleutel staat in `trainer-app/local.properties` (staat in `.gitignore`). Alleen de *publishable* sleutel gebruiken, nooit de secret.
6. **De AI-assistent bestaat nog niet.** Er is geen API en geen sleutel. Laat in de app duidelijk zien wat een plaatshouder is.

## Lokaal draaien

Zie `trainer-app/README.md`: database starten met `supabase start`, instellingen in `local.properties`, dan `./gradlew :androidApp:installDebug`.
