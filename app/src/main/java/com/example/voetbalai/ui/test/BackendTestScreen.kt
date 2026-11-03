package com.example.voetbalai.ui.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.voetbalai.viewmodel.chat.ChatUiState
import com.example.voetbalai.viewmodel.chat.ChatViewModel
import com.example.voetbalai.viewmodel.onboarding.OnboardingUiState
import com.example.voetbalai.viewmodel.stats.PlayerStatsUiState
import com.example.voetbalai.viewmodel.stats.PlayerStatsViewModel
import com.example.voetbalai.viewmodel.onboarding.OnboardingViewModel
import com.example.voetbalai.viewmodel.today.TodayUiState
import com.example.voetbalai.viewmodel.today.TodayViewModel

// UITLEG: Dit is het hoofdscherm van het testscherm: een statusregel bovenaan, tabbladen daaronder en de
// inhoud van het gekozen tabblad. Een @Composable-functie is een stukje scherm. Hij doet zelf geen
// berekeningen; hij toont alleen wat in de UiState staat en geeft knoppen door aan de ViewModel.
private val tabs = listOf("Team", "Spelers", "Aanwezigheid", "Stats", "Chat", "Debug")

@Composable
fun BackendTestScreen(
    onboardingState: OnboardingUiState,
    todayState: TodayUiState,
    chatState: ChatUiState,
    statsState: PlayerStatsUiState,
    onboarding: OnboardingViewModel,
    today: TodayViewModel,
    chat: ChatViewModel,
    stats: PlayerStatsViewModel
) {
    // Dit hoort alleen bij het scherm zelf en niet in de ViewModel: welk tabblad open staat en wat
    // de laatste actie was. rememberSaveable onthoudt het ook als je de telefoon draait.
    var tab by rememberSaveable { mutableStateOf(0) }
    var status by rememberSaveable { mutableStateOf("Klaar") }

    // Een foutmelding uit de UiState gaat voor op de laatste actie.
    val error = todayState.errorMessage ?: onboardingState.errorMessage ?: chatState.errorMessage ?: statsState.errorMessage

    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        Text(if (error != null) "FOUT: $error" else "Status: $status")
        PrimaryScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        // Elk tabblad scrolt zelf (en begint bovenaan als je ernaartoe wisselt).
        key(tab) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
                when (tab) {
                    0 -> TeamTab(onboardingState, onboarding) { status = it }
                    1 -> PlayersTab(onboardingState, todayState, onboarding, today) { status = it }
                    2 -> AttendanceTab(todayState, today) { status = it }
                    3 -> StatsTab(statsState, stats) { status = it }
                    4 -> ChatTab(chatState, chat) { status = it }
                    else -> DebugTab(onboardingState, todayState, today) { status = it }
                }
            }
        }
    }
}

/** Kopje boven een sectie, met een scheidingslijn ervoor. */
@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    HorizontalDivider()
    Text(title)
    content()
}
