package com.example.voetbalai.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voetbalai.VoetbalApp
import com.example.voetbalai.viewmodel.AppViewModelFactory
import com.example.voetbalai.viewmodel.chat.ChatViewModel
import com.example.voetbalai.viewmodel.onboarding.OnboardingViewModel
import com.example.voetbalai.viewmodel.stats.PlayerStatsViewModel
import com.example.voetbalai.viewmodel.today.TodayViewModel

/**
 * Kaal testscherm voor de backend. De hele map `ui/test/` mag worden verwijderd door de frontend;
 * pas dan de `<activity>` in AndroidManifest.xml aan. Zie README.md.
 */
// UITLEG: Dit is het beginpunt van het scherm (een Activity). Android start deze als je de app opent
// (zie AndroidManifest.xml). Dit gebeurt hier:
// 1. Ik maak de factory met de AppContainer, zodat de ViewModels hun onderdelen krijgen.
// 2. setContent { ... } is Jetpack Compose: hier beschrijf ik hoe het scherm eruitziet.
// 3. collectAsState() zet de UiState (een Flow) om naar iets waar Compose op reageert. Verandert de
//    UiState, dan tekent Compose het scherm vanzelf opnieuw.
// Het scherm zelf (BackendTestScreen) krijgt alleen de state en de ViewModels binnen.
class BackendTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = AppViewModelFactory((application as VoetbalApp).container)
        setContent {
            val onboarding: OnboardingViewModel = viewModel(factory = factory)
            val today: TodayViewModel = viewModel(factory = factory)
            val chat: ChatViewModel = viewModel(factory = factory)
            val stats: PlayerStatsViewModel = viewModel(factory = factory)
            val onboardingState by onboarding.uiState.collectAsState()
            val todayState by today.uiState.collectAsState()
            val chatState by chat.uiState.collectAsState()
            val statsState by stats.uiState.collectAsState()
            BackendTestScreen(onboardingState, todayState, chatState, statsState, onboarding, today, chat, stats)
        }
    }
}
