package com.aifootballcoach.trainer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.aifootballcoach.trainer.data.MockTeamRepository
import com.aifootballcoach.trainer.data.TeamRepository
import com.aifootballcoach.trainer.theme.CoachTheme
import com.aifootballcoach.trainer.ui.home.MainShell
import com.aifootballcoach.trainer.ui.onboarding.AddPlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.InvitePlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.TeamSetupScreen
import com.aifootballcoach.trainer.ui.onboarding.WelcomeScreen

private enum class Screen(val darkBackground: Boolean = false) {
    Welcome(darkBackground = true),
    TeamSetup,
    AddPlayers,
    InvitePlayers,
    Main,
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
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(screen) { onDarkBackground(screen.darkBackground) }

    CoachTheme {
        when (screen) {
            Screen.Welcome -> WelcomeScreen(
                trainerName = repository.trainer().firstName,
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

            Screen.InvitePlayers -> InvitePlayersScreen(
                teamLink = repository.teamLink(),
                onShare = { /* TODO: platform share sheet */ },
                onCopy = { clipboard.setText(AnnotatedString("https://${repository.teamLink()}")) },
                onDone = { screen = Screen.Main },
            )

            Screen.Main -> MainShell(repository, onDarkBackground = onDarkBackground)
        }
    }
}
