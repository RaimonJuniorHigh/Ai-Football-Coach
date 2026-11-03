package com.example.voetbalai.viewmodel.welcome

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// UITLEG: Dit is de ViewModel voor het welkomstscherm. Er is nu alleen een naam (nog vast "Tom"). Het
// scherm gebruikt hem nog niet; ik heb hem klaargezet voor de echte schermen die later komen.
data class WelcomeUiState(
    /** Naam van de trainer; voorlopig vast, later uit een account. */
    val userName: String = "Tom"
)

/** Geen acties: doorgaan naar de volgende stap is navigatie en wordt door de NavHost geregeld. */
class WelcomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()
}
