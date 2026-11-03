package com.aifootballcoach.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.data.TeamRepository
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.theme.CoachTheme
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.home.MainShell
import com.aifootballcoach.trainer.ui.onboarding.AddPlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.InvitePlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.TeamSetupScreen
import com.aifootballcoach.trainer.ui.onboarding.WelcomeScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
    repository: TeamRepository,
    onDarkBackground: (Boolean) -> Unit = {},
) {
    var screen by remember { mutableStateOf(Screen.Welcome) }
    var team by remember { mutableStateOf(repository.loadTeam()) }
    var players by remember { mutableStateOf(repository.players()) }
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // The repository loads from the database first; until then (or when that fails) show a status screen.
    var loaded by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(repository, attempt) {
        try {
            repository.refresh()
            team = repository.loadTeam()
            players = repository.players()
            loaded = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            problem = e.message ?: "Onbekende fout"
        }
    }

    // Runs a database call and shows the problem screen instead of crashing when it fails.
    fun launchSafely(block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                problem = e.message ?: "Onbekende fout"
            }
        }
    }

    LaunchedEffect(screen) { onDarkBackground(screen.darkBackground) }

    CoachTheme {
        if (!loaded || problem != null) {
            StatusScreen(
                problem = problem,
                onRetry = {
                    problem = null
                    attempt++
                },
            )
            return@CoachTheme
        }
        when (screen) {
            Screen.Welcome -> WelcomeScreen(
                onStart = { screen = Screen.TeamSetup },
            )

            Screen.TeamSetup -> TeamSetupScreen(
                team = team,
                onTeamChange = { team = it },
                onNext = {
                    launchSafely {
                        repository.saveTeam(team)
                        team = repository.loadTeam()
                        players = repository.players()
                        screen = Screen.AddPlayers
                    }
                },
            )

            Screen.AddPlayers -> AddPlayersScreen(
                players = players,
                onAddPlayer = { name, position ->
                    launchSafely {
                        repository.addPlayer(name, position)
                        players = repository.players()
                    }
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

/** Shown while the database is being read, or with a retry button when that failed. */
@Composable
private fun StatusScreen(problem: String?, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(CoachColors.Paper).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (problem == null) {
            CircularProgressIndicator(color = CoachColors.Pitch)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("De database is niet bereikbaar", style = MaterialTheme.typography.headlineLarge)
                Text(problem, style = MaterialTheme.typography.bodyMedium, color = CoachColors.Muted)
                PillButton(text = "Opnieuw proberen", onClick = onRetry)
            }
        }
    }
}

/** Shown instead of the app when it can't start, for example when the database settings are missing. */
@Composable
fun SetupProblemScreen(message: String) {
    CoachTheme {
        StatusScreen(problem = message, onRetry = {})
    }
}
