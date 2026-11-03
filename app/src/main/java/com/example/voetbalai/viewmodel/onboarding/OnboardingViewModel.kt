package com.example.voetbalai.viewmodel.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voetbalai.domain.model.ClubPlayer
import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team
import com.example.voetbalai.domain.model.TeamWithPlayers
import com.example.voetbalai.domain.repository.TeamRepository
import com.example.voetbalai.domain.rules.TeamRules
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UITLEG: Dit is de ViewModel voor het team opzetten (onboarding). Het belangrijkste idee: ik werk in een
// CONCEPT (alleen in het geheugen). Pas als er op afronden gedrukt wordt (onFinish) gaat het naar de
// database. Zo staat er nooit een half team in de database.

// Een clubspeler uit de lijst, met of hij al in de selectie zit (dan kan hij er niet nog een keer bij).
data class ClubPlayerChoice(val name: String, val position: Position, val alreadyAdded: Boolean)

// Alles wat het onboarding-scherm moet tonen. De velden met "get()" onderaan reken ik steeds
// opnieuw uit de rest uit, bijvoorbeeld of de knop "Opslaan" aan mag.
data class OnboardingUiState(
    val teamName: String = "",
    val trainingDays: Set<Day> = emptySet(),
    val sessionMinutes: Int = 90,
    val players: List<Player> = emptyList(),
    val clubList: List<ClubPlayer> = emptyList(),
    val inviteLink: String = "",
    val isSaving: Boolean = false,
    /** Laatste fout van een actie; null als alles goed ging. */
    val errorMessage: String? = null
) {
    /** Stap 1 kan pas verder met een teamnaam en minstens één trainingsdag. */
    val canContinueStep1: Boolean get() = teamName.isNotBlank() && trainingDays.isNotEmpty()

    /** Wat er aan de selectie ontbreekt om een echt team te zijn (zie [TeamRules]); leeg als het klopt. */
    val squadProblems: List<String> get() = TeamRules.problems(players)

    /** Het team kan pas worden opgeslagen als stap 1 klopt en de selectie voldoet aan [TeamRules]. */
    val canSave: Boolean get() = canContinueStep1 && squadProblems.isEmpty()

    /** De clublijst met per speler of die al in de selectie staat. */
    val clubChoices: List<ClubPlayerChoice>
        get() = clubList.map { club ->
            ClubPlayerChoice(club.name, club.position, alreadyAdded = players.any { it.name == club.name })
        }
}

/**
 * Eén ViewModel voor de drie onboarding-stappen: de stappen delen één concept-team dat pas bij
 * [onFinish] wordt opgeslagen.
 */
class OnboardingViewModel(private val repository: TeamRepository) : ViewModel() {
    // Dit is de code voor de uitnodigingslink. Nieuwe spelers in het concept krijgen een tijdelijk NEGATIEF id
    // (-1, -2, ...), zodat ze niet door elkaar lopen met echte database-id's (die zijn positief).
    // De database geeft bij het opslaan het echte id.
    private var inviteCode = randomCode()
    private var nextTempId = -1L

    // _uiState mag ik zelf aanpassen (private). uiState is de alleen-lezen versie voor het scherm.
    private val _uiState = MutableStateFlow(
        OnboardingUiState(inviteLink = linkFor(inviteCode))
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // De clublijst en een eventueel al opgeslagen team komen uit de database. Een nieuwe trainer heeft nog
        // geen team, dan blijft het concept leeg.
        viewModelScope.launch {
            try {
                val clubList = repository.clubList()
                _uiState.update { it.copy(clubList = clubList) }
                repository.currentTeam()?.let { loadTeamIntoDraft(it, withCode = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Laden mislukt: ${e.message}") }
            }
        }
    }

    // De functies hieronder passen steeds het concept aan met update { it.copy(...) }.
    // copy() maakt een kopie van de UiState met één veld veranderd, en zo ontstaat er een nieuwe state.
    fun onTeamNameChanged(name: String) = _uiState.update { it.copy(teamName = name) }

    // Een dag zet ik aan of uit: staat hij erin, dan haal ik hem eruit; anders voeg ik hem toe.
    fun onTrainingDayToggled(day: Day) = _uiState.update {
        it.copy(trainingDays = if (day in it.trainingDays) it.trainingDays - day else it.trainingDays + day)
    }

    fun onSessionMinutesChosen(minutes: Int) = _uiState.update { it.copy(sessionMinutes = minutes) }

    fun onPlayerAdded(name: String, position: Position) {
        if (name.isBlank()) return
        val player = Player(id = nextTempId--, name = name.trim(), position = position)
        _uiState.update { it.copy(players = it.players + player) }
    }

    fun onPlayerRemoved(playerId: Long) = _uiState.update { state ->
        state.copy(players = state.players.filterNot { it.id == playerId })
    }

    /** Voegt de gekozen clubspelers toe (op naam); spelers die er al staan worden overgeslagen. */
    fun onClubPlayersImported(names: Set<String>) {
        _uiState.value.clubList
            .filter { it.name in names }
            .forEach { club ->
                if (_uiState.value.players.none { it.name == club.name }) onPlayerAdded(club.name, club.position)
            }
    }

    /** Laadt het opgeslagen team opnieuw in het concept; aanroepen voor "Team aanpassen". */
    fun onEditTeam() {
        viewModelScope.launch {
            val existing = try {
                repository.currentTeam()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Team laden mislukt: ${e.message}") }
                return@launch
            }
            if (existing == null) {
                _uiState.update { it.copy(errorMessage = "Geen opgeslagen team om te bewerken: sla eerst een team op of maak een demo-team (tabblad Debug).") }
            } else {
                loadTeamIntoDraft(existing, withCode = true)
            }
        }
    }

    /** Slaat het team op, markeert de onboarding als afgerond en roept daarna [onDone] aan. */
    // Dit is de volgorde: eerst controleer ik (teamnaam, dagen, teamregels). Klopt iets niet, dan geef ik een foutmelding
    // en stop ik. Klopt alles, dan sla ik op in de database, vink ik de onboarding af en roep ik pas daarna de
    // callback aan (bijvoorbeeld om naar het volgende scherm te gaan).
    fun onFinish(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return
        if (!state.canContinueStep1) {
            _uiState.update { it.copy(errorMessage = "Vul een teamnaam in en kies minstens één trainingsdag.") }
            return
        }
        val problems = state.squadProblems
        if (problems.isNotEmpty()) {
            _uiState.update { it.copy(errorMessage = "Selectie onvolledig: ${problems.joinToString(" ")}") }
            return
        }
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.saveTeam(
                    Team(
                        id = 0,
                        name = state.teamName.trim(),
                        trainingDays = state.trainingDays,
                        sessionMinutes = state.sessionMinutes,
                        inviteCode = inviteCode
                    ),
                    state.players
                )
                repository.markOnboarded()
                _uiState.update { it.copy(isSaving = false) }
                onDone()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Opslaan mislukt: ${e.message}") }
            }
        }
    }

    private fun loadTeamIntoDraft(existing: TeamWithPlayers, withCode: Boolean) {
        val (team, players) = existing
        if (withCode) inviteCode = team.inviteCode
        _uiState.update {
            it.copy(
                teamName = team.name,
                trainingDays = team.trainingDays,
                sessionMinutes = team.sessionMinutes,
                players = players,
                inviteLink = linkFor(inviteCode),
                errorMessage = null
            )
        }
    }

    // Hulpfuncties. De link is alleen tekst; er is geen echte server achter.
    private companion object {
        fun linkFor(code: String) = "voetbalai.app/join/$code"

        // Dit maakt een willekeurige code van 4 tekens. Tekens die op elkaar lijken (I, l, O, 0, 1) heb ik weggelaten,
        // zodat de code makkelijk over te typen is.
        fun randomCode(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"
            return (1..4).map { chars.random() }.joinToString("")
        }
    }
}
