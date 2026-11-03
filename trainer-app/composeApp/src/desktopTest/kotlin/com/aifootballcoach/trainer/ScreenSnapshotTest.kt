package com.aifootballcoach.trainer

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.aifootballcoach.trainer.data.MockTeamRepository
import com.aifootballcoach.trainer.theme.CoachTheme
import com.aifootballcoach.trainer.ui.components.Tab
import com.aifootballcoach.trainer.ui.home.MainShell
import com.aifootballcoach.trainer.ui.onboarding.AddPlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.InvitePlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.TeamSetupScreen
import com.aifootballcoach.trainer.ui.onboarding.WelcomeScreen
import com.aifootballcoach.trainer.ui.player.AddPointSheet
import com.aifootballcoach.trainer.ui.player.LogObservationSheet
import com.aifootballcoach.trainer.ui.player.PlayerScreen
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Renders each screen to a PNG in build/snapshots so the UI can be compared
 * with the Claude design without an emulator.
 */
class ScreenSnapshotTest {
    private val repository = MockTeamRepository()

    @Test
    fun renderOnboardingScreens() {
        snapshot("11-welcome") { WelcomeScreen(trainerName = "Tom", onStart = {}) }
        snapshot("12-your-team") {
            TeamSetupScreen(team = repository.loadTeam(), onTeamChange = {}, onNext = {})
        }
        snapshot("13-add-players") {
            AddPlayersScreen(
                players = repository.players().take(3),
                onAddPlayer = { _, _ -> },
                onImportFromClubList = {},
                onNext = {},
            )
        }
        snapshot("14-invite-players") {
            InvitePlayersScreen(teamLink = repository.teamLink(), onShare = {}, onCopy = {}, onDone = {})
        }
        snapshot("02-today") { MainShell(repository, initialTab = Tab.Today) }
        snapshot("03-squad") { MainShell(repository, initialTab = Tab.Squad) }
    }

    @Test
    fun renderPlayerScreens() {
        val sem = repository.players().first { it.id == "p2" }
        val points = repository.developmentPoints(sem.id)
        val player: @Composable () -> Unit = {
            PlayerScreen(
                player = sem,
                stats = repository.playerStats(sem.id),
                points = points,
                onBack = {},
                onAddPoint = {},
                onOpenPoint = {},
                onLogObservation = {},
            )
        }
        snapshot("04-player", player)
        snapshot("15-add-point") {
            Box {
                player()
                AddPointSheet(sem, onSave = { _, _, _ -> }, onDismiss = {}, initialTitle = "1-tegen-1 — binnenkant dichtzetten")
            }
        }
        snapshot("16-log-observation") {
            Box {
                player()
                LogObservationSheet(sem, points.first(), hasNextPlayer = true, onSave = { _, _ -> }, onDismiss = {}, initialRating = 4)
            }
        }
    }

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        val density = 2f
        val scene = ImageComposeScene(
            width = (390 * density).toInt(),
            height = (844 * density).toInt(),
            density = Density(density),
        ) { CoachTheme { content() } }
        try {
            val png = scene.render().encodeToData()!!.bytes
            val file = File("build/snapshots/$name.png").apply { parentFile.mkdirs() }
            file.writeBytes(png)
            assertTrue(file.length() > 0)
        } finally {
            scene.close()
        }
    }
}
