package com.example.voetbalai.viewmodel.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voetbalai.domain.repository.PlayerStatsRepository
import com.example.voetbalai.domain.stats.PlayerCard
import com.example.voetbalai.domain.stats.PlayerProfile
import com.example.voetbalai.domain.stats.StatType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UITLEG: De ViewModel voor de spelersstatistieken. Hij heeft twee dingen in de UiState:
// - cards: alle kaartjes van het team (totaalcijfer 1-99 per speler), voor het overzicht.
// - selected: het profiel van de speler waarop je geklikt hebt (stats 1-20, oefeningen, wedstrijden).
// De kleuren bepaal ik niet hier maar in RatingColors; het scherm vraagt daar de kleur bij een cijfer.

data class PlayerStatsUiState(
    val cards: List<PlayerCard> = emptyList(),
    /** Het profiel van de gekozen speler; null als er geen speler gekozen is. */
    val selected: PlayerProfile? = null,
    val isLoading: Boolean = false,
    /** Laatste fout van een actie; null als alles goed ging. */
    val errorMessage: String? = null
)

class PlayerStatsViewModel(private val repository: PlayerStatsRepository) : ViewModel() {

    // _uiState mag ik zelf aanpassen (private). uiState is de alleen-lezen versie voor het scherm.
    private val _uiState = MutableStateFlow(PlayerStatsUiState())
    val uiState: StateFlow<PlayerStatsUiState> = _uiState.asStateFlow()

    /** Haalt de kaartjes van het team opnieuw op (en ververst het gekozen profiel als dat open staat). */
    fun onCardsRequested() = runAction {
        val cards = repository.playerCards()
        val selectedId = _uiState.value.selected?.card?.playerId
        val selected = selectedId?.let { repository.playerProfile(it) }
        _uiState.update { it.copy(cards = cards, selected = selected) }
    }

    /** Opent het profiel van een speler: kaartje, stats, oefeningen en wedstrijden. */
    fun onPlayerSelected(playerId: Long) = runAction {
        val profile = repository.playerProfile(playerId)
        _uiState.update {
            it.copy(selected = profile, errorMessage = if (profile == null) "Speler niet gevonden." else null)
        }
    }

    /** Sluit het profiel. */
    fun onSelectionCleared() = _uiState.update { it.copy(selected = null) }

    /**
     * Legt een trainingsresultaat vast (bijvoorbeeld 100 schoten, 70 goals) en ververst daarna de kaartjes en het profiel,
     * want het cijfer van de speler verandert daardoor.
     */
    fun onDrillRecorded(playerId: Long, stat: StatType, attempts: Int, successes: Int) = runAction {
        repository.recordDrill(playerId, stat, attempts, successes)
        val cards = repository.playerCards()
        val profile = repository.playerProfile(playerId)
        _uiState.update { it.copy(cards = cards, selected = profile) }
    }

    // Hulpfunctie voor alle acties: laadstatus aan, fout wissen, werk doen in een coroutine, en bij een fout
    // de melding klaarzetten voor het scherm.
    private fun runAction(block: suspend () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                block()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Stats laden mislukt: ${e.message}") }
            }
        }
    }
}
