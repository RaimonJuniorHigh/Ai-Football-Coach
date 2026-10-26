package com.aifootballcoach.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.data.MockTeamRepository
import com.aifootballcoach.trainer.data.TeamRepository
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.theme.CoachTheme
import com.aifootballcoach.trainer.ui.onboarding.AddPlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.TeamSetupScreen
import com.aifootballcoach.trainer.ui.onboarding.WelcomeScreen

private enum class Screen(val darkBackground: Boolean = false) {
    Welcome(darkBackground = true),
    TeamSetup,
    AddPlayers,
    InvitePlayers,
}

/**
 * Root of the trainer app.
 * @param onDarkBackground tells the platform whether the current screen is dark,
 *   so it can switch status bar icons to light (Android) — ignored on iOS for now.
 */
@Composable
fun App(
    repository: TeamRepository = remember { MockTeamRepository() },
    onDarkBackground: (Boolean) -> Unit = {},
) {
    var screen by remember { mutableStateOf(Screen.Welcome) }
    var team by remember { mutableStateOf(repository.loadTeam()) }
    var players by remember { mutableStateOf(repository.players()) }

    LaunchedEffect(screen) { onDarkBackground(screen.darkBackground) }

    CoachTheme {
        when (screen) {
            Screen.Welcome -> WelcomeScreen(
                trainerName = "Tom",
                onStart = { screen = Screen.TeamSetup },
            )

            Screen.TeamSetup -> TeamSetupScreen(
                team = team,
                onTeamChange = { team = it },
                onNext = {
                    repository.saveTeam(team)
                    screen = Screen.AddPlayers
                },
            )

            Screen.AddPlayers -> AddPlayersScreen(
                players = players,
                onAddPlayer = { name, position ->
                    repository.addPlayer(name, position)
                    players = repository.players()
                },
                onImportFromClubList = { /* TODO: club list import */ },
                onNext = { screen = Screen.InvitePlayers },
            )

            // Design screen 14 is the next batch; placeholder keeps the flow navigable.
            Screen.InvitePlayers -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().background(CoachColors.Paper).padding(32.dp),
            ) {
                Text(
                    "Step 3 · Invite players\ncomes in the next batch",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CoachColors.Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
