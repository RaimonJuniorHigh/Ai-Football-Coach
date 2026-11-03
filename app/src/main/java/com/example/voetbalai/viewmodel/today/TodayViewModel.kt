package com.example.voetbalai.viewmodel.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.repository.TeamRepository
import com.example.voetbalai.domain.schedule.daysAheadLabel
import com.example.voetbalai.domain.schedule.formatTrainingDate
import com.example.voetbalai.domain.schedule.greeting
import com.example.voetbalai.domain.schedule.nextTraining
import java.util.Calendar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// UITLEG: Dit is de ViewModel van het "Vandaag"-scherm. Ik laat hem drie dingen doen:
// 1. Hij bewaart alles wat het scherm moet laten zien in één pakketje: TodayUiState.
// 2. Hij heeft een functie voor elke actie (onAttendanceChanged, onPlayerAdded, ...).
// 3. Hij praat met de repository, en nooit met de database zelf.
// Het scherm leest alleen uiState en roept alleen deze functies aan. De namen beginnen met "on"
// omdat ze reageren op iets wat de gebruiker doet (een tik op een knop).
// De chat met de AI zit niet hier, maar in ChatViewModel.

// Hier maak ik van de volgende training een tekst die het scherm kan tonen.
data class UpcomingTraining(
    /** Bijvoorbeeld "Dinsdag 27 oktober". */
    val dateText: String,
    /** "vandaag", "morgen" of "over 3 dagen". */
    val whenText: String,
    val durationMinutes: Int
)

// Hier groepeer ik de spelers per positie, zodat het scherm de selectie per positie kan tonen.
data class PositionGroup(val position: Position, val players: List<Player>)

// De UiState bevat alles wat het scherm nodig heeft. Verandert er iets, dan maak ik een nieuwe UiState
// en tekent het scherm zichzelf opnieuw. De velden met "get()" reken ik uit de rest uit.
data class TodayUiState(
    /** Bijvoorbeeld "Goedemiddag, trainer". */
    val greeting: String = "",
    val teamName: String = "",
    /** Null als er geen trainingsdagen zijn ingesteld. */
    val upcomingTraining: UpcomingTraining? = null,
    val players: List<Player> = emptyList(),
    /** Laatste fout van een actie; null als alles goed ging. */
    val errorMessage: String? = null
) {
    val presentCount: Int get() = players.count { it.attendance == Attendance.PRESENT }
    val injuredCount: Int get() = players.count { it.attendance == Attendance.INJURED }

    /** Spelers gegroepeerd per positie; posities zonder spelers ontbreken. */
    val squad: List<PositionGroup>
        get() = Position.entries
            .map { position -> PositionGroup(position, players.filter { it.position == position }) }
            .filter { it.players.isNotEmpty() }
}

class TodayViewModel(
    private val repository: TeamRepository,
    private val now: () -> Calendar = { Calendar.getInstance() }
) : ViewModel() {

    // De laatste errorMessage. MutableStateFlow is een waarde die ik mag aanpassen en die meldt dat hij veranderd is.
    private val error = MutableStateFlow<String?>(null)

    // Dit is de uiState voor het scherm. Met combine(...) plak ik drie bronnen (team, spelers, fout) aan
    // elkaar; zodra één ervan verandert, maak ik een nieuwe TodayUiState.
    // stateIn(...) maakt er een StateFlow van die altijd een huidige waarde heeft. WhileSubscribed(5_000)
    // houdt hem nog 5 seconden actief nadat het scherm weg is (handig bij het draaien van de telefoon).
    val uiState: StateFlow<TodayUiState> = combine(repository.team, repository.players, error) { team, players, error ->
        val time = now()
        TodayUiState(
            greeting = "${greeting(time.get(Calendar.HOUR_OF_DAY))}, trainer",
            teamName = team?.name.orEmpty(),
            upcomingTraining = team?.let { t ->
                nextTraining(t.trainingDays, time)?.let {
                    UpcomingTraining(formatTrainingDate(it.date), daysAheadLabel(it.daysAhead), t.sessionMinutes)
                }
            },
            players = players,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    init {
        // Bij het openen haal ik team en spelers op uit de database. Lukt dat niet (bijvoorbeeld geen internet),
        // dan komt er een errorMessage.
        runAction("Laden") { repository.refresh() }
    }

    // Hier wijzig ik de aanwezigheid van een speler. Na het opslaan haalt de repository alles opnieuw op en ververst het scherm vanzelf.
    fun onAttendanceChanged(playerId: Long, status: Attendance) =
        runAction("Aanwezigheid wijzigen") { repository.setAttendance(playerId, status) }

    fun onPlayerAdded(name: String, position: Position) {
        if (name.isBlank()) return
        runAction("Speler toevoegen") { repository.addPlayer(name, position) }
    }

    /** Verwijdert alle teams en spelers en zet de onboarding terug. */
    fun onDatabaseReset() = runAction("Database resetten") { repository.resetDatabase() }

    /** Laat de database een demo-team maken (JO17-1 met 16 spelers). Dit vervangt het huidige team. */
    fun onDemoTeamCreated() = runAction("Demo-team maken") { repository.createDemoTeam() }

    // Hulpfunctie voor alle acties: ik wis de oude fout, voer het werk uit in een coroutine en zet bij een
    // fout de melding "<actie> mislukt: ..." klaar voor het scherm. Zo herhaal ik die code niet overal.
    private fun runAction(action: String, block: suspend () -> Unit) {
        error.value = null
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = "$action mislukt: ${e.message}"
            }
        }
    }
}
