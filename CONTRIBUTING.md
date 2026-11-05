# Hoe Berkan en ik samenwerken

We bouwen samen één app. Dit zijn de afspraken die ik voorstel, zodat we elkaars werk zien en elkaar niet tegenspreken.

## Wie doet wat

| Deel | Wie | Waar |
|------|-----|------|
| Schermen, ontwerp, navigatie | Berkan | `trainer-app/composeApp/.../ui`, `theme`, `androidApp` |
| Database, stats en de koppeling met de app | Younes | `supabase/`, `trainer-app/composeApp/.../data` |

We mogen elkaars deel aanpassen, maar altijd via een pull request waar de ander naar kijkt. Dat regelt `.github/CODEOWNERS` vanzelf.

## Zo werken we (Scrum, kort)

- **Taken:** elke taak is een GitHub Issue, als user story: "Als trainer wil ik … zodat …".
- **Bord:** een GitHub Project met Te doen, Bezig, Review en Klaar. Een taak is pas Klaar als de Definition of Done klopt.
- **Sprint:** één of twee weken, af te stemmen met de docent. Aan het begin plannen we samen, aan het eind doen we een demo en een korte terugblik.
- **Tussendoor:** een korte update in de chat: wat deed ik, wat doe ik nu, waar loop ik vast?

### Definition of Done
- De app bouwt en alle tests slagen.
- Ik heb het zelf op een emulator of telefoon geprobeerd.
- De ander heeft de pull request bekeken.
- De README of dit bestand is bijgewerkt als er iets verandert aan hoe het werkt.

## Git

- `main` werkt altijd. Niemand pusht rechtstreeks naar `main`.
- Eén branch per taak: `feature/<naam>-<onderwerp>`, bijvoorbeeld `feature/berkan-session-review`.
- Branches blijven kort (een paar dagen). Ik haal elke dag `main` in mijn branch (`git pull origin main`), dan blijven conflicten klein.
- Pull requests zijn klein en gaan over één onderwerp.
- Commit-berichten zijn korte zinnen over wat er verandert.

## Zodat we elkaar niet tegenspreken

1. **De database is de bron van de cijfers.** Stats (1 tot 20) en het totaal (1 tot 99) rekent de database uit. De app rekent ze niet zelf uit, zodat app en website hetzelfde laten zien.
2. **De database verander ik alleen met een nieuw migratiebestand** in `supabase/migrations`. Een bestaand bestand pas ik nooit aan. Staat er een nieuwe migratie in een pull request, dan moet hij daarna ook online.
3. **Verandert er iets aan `TeamRepository` of de modellen in `data/`?** Dan zet ik dat bovenaan de pull request en vraag ik de ander om te kijken, want alle schermen hangen daaraan.
4. **Heeft een nieuw scherm data nodig?** Dan maken we eerst een issue. De een maakt de data klaar terwijl de ander het scherm bouwt.
5. **Geen sleutels in git.** De database-sleutel staat in `trainer-app/local.properties` (die staat in `.gitignore`). Alleen de publishable sleutel gebruiken, nooit de secret.

## Lokaal draaien

Zie `trainer-app/README.md`: de database starten met `supabase start`, de instellingen in `local.properties`, dan `./gradlew :androidApp:installDebug`. Mijn testverslag staat in `docs/testverslag.md`.
