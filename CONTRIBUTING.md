# Samenwerken: backend en frontend

Twee mensen, twee mappen. Zo komen we elkaar niet in de weg.

| Wie | Werkt in | Blijft uit |
|---|---|---|
| Backend | `data/`, `domain/`, `viewmodel/`, `AppContainer.kt` | `ui/` |
| Frontend | `ui/` | `data/`, `domain/`, `viewmodel/` |

Het contract tussen beide is de `UiState` en de acties van de ViewModels (zie `README.md`). Wil de frontend iets nieuws (veld of actie), vraag het dan aan de backend.

## Branches

- `main` bouwt altijd en de tests slagen. Niemand werkt hier direct in.
- Eén branch per taak, kort van duur: `backend/<taak>` of `frontend/<taak>`.
- Samenvoegen gaat via een Pull Request; de ander kijkt mee en keurt goed.

```
git switch main && git pull
git switch -c backend/team-regels
# werken, committen
git push -u origin backend/team-regels   # daarna PR openen op GitHub
```

## Bestanden waar we allebei aan komen

`AndroidManifest.xml`, `app/build.gradle.kts`, `gradle/libs.versions.toml` en `README.md`. Stuur elkaar een berichtje voordat je hier iets in verandert.

## Voor je een PR opent

`./gradlew testDebugUnitTest assembleDebug` moet slagen.
