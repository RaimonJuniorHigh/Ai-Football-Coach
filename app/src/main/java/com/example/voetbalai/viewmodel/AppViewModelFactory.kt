package com.example.voetbalai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voetbalai.AppContainer
import com.example.voetbalai.viewmodel.app.AppViewModel
import com.example.voetbalai.viewmodel.chat.ChatViewModel
import com.example.voetbalai.viewmodel.onboarding.OnboardingViewModel
import com.example.voetbalai.viewmodel.stats.PlayerStatsViewModel
import com.example.voetbalai.viewmodel.today.TodayViewModel
import com.example.voetbalai.viewmodel.welcome.WelcomeViewModel

// UITLEG: Een ViewModel met eigen onderdelen (de repository) kan Android niet zelf aanmaken. Deze
// "factory" (fabriek) doet dat voor mij: als Android een TodayViewModel vraagt, bouw ik die hier en
// geef ik hem de repository en de chat-API uit de AppContainer mee. Dat heet dependency injection:
// ik geef de onderdelen van buiten mee en maak ze niet binnen de klasse zelf aan.

/** Maakt de ViewModels aan met de afhankelijkheden uit [AppContainer]. */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel(container.teamRepository)
        WelcomeViewModel::class.java -> WelcomeViewModel()
        OnboardingViewModel::class.java -> OnboardingViewModel(container.teamRepository)
        TodayViewModel::class.java -> TodayViewModel(container.teamRepository)
        PlayerStatsViewModel::class.java -> PlayerStatsViewModel(container.statsRepository)
        ChatViewModel::class.java -> ChatViewModel(container.teamRepository, container.chatApi)
        else -> throw IllegalArgumentException("Onbekende ViewModel: ${modelClass.name}")
    } as T
}
