package com.aifootballcoach.trainer

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
