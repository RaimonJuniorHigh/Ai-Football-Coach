package com.example.voetbalai.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voetbalai.domain.chat.ChatApi
import com.example.voetbalai.domain.chat.ChatMessage
import com.example.voetbalai.domain.chat.ChatRequest
import com.example.voetbalai.domain.chat.ChatRole
import com.example.voetbalai.domain.repository.TeamRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UITLEG: Dit is de ViewModel van de chat. De trainer typt een bericht, ik stuur het samen met de
// teamgegevens naar de API en zet het antwoord onder het gesprek. Het gesprek zelf (de lijst met
// berichten) bewaar ik hier in de UiState, zodat het scherm het alleen hoeft te tonen.

// Alles wat het chatscherm moet tonen.
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    /** True zolang ik op het antwoord van de API wacht (dan kan het scherm "aan het typen..." tonen). */
    val isBusy: Boolean = false,
    /** Laatste fout van een actie; null als alles goed ging. */
    val errorMessage: String? = null
) {
    // Een nieuw bericht versturen mag alleen als er niet al op een antwoord gewacht wordt.
    val canSend: Boolean get() = !isBusy
}

class ChatViewModel(
    private val repository: TeamRepository,
    private val api: ChatApi
) : ViewModel() {

    // _uiState mag ik zelf aanpassen (private). uiState is de alleen-lezen versie voor het scherm.
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Dit is de knop "Verstuur". Ik doe dit: lege berichten negeren, het bericht van de trainer meteen
    // aan het gesprek toevoegen, teamgegevens ophalen, de API om een antwoord vragen en dat antwoord
    // onder het gesprek zetten. Gaat er iets mis, dan zet ik een errorMessage klaar.
    fun onMessageSent(text: String) {
        val message = text.trim()
        if (message.isEmpty() || _uiState.value.isBusy) return
        val conversation = _uiState.value.messages + ChatMessage(ChatRole.USER, message)
        _uiState.update { it.copy(messages = conversation, isBusy = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Heb ik geen team, dan stuur ik lege teamgegevens mee; chatten kan dan nog steeds.
                val team = repository.currentTeam()
                val reply = api.reply(
                    ChatRequest(
                        teamName = team?.team?.name.orEmpty(),
                        sessionMinutes = team?.team?.sessionMinutes ?: 0,
                        players = team?.players.orEmpty(),
                        messages = conversation
                    )
                )
                _uiState.update {
                    it.copy(messages = it.messages + ChatMessage(ChatRole.ASSISTANT, reply), isBusy = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, errorMessage = "Chat mislukt: ${e.message}") }
            }
        }
    }

    // Maakt het gesprek leeg, zodat de trainer opnieuw kan beginnen.
    fun onChatCleared() = _uiState.update { ChatUiState() }
}
