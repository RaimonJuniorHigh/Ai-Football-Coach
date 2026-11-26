package com.aifootballcoach.trainer

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.aifootballcoach.trainer.data.Availability
import com.aifootballcoach.trainer.data.Intensity
import com.aifootballcoach.trainer.data.PitchSpace
import com.aifootballcoach.trainer.data.SessionBrief
import com.aifootballcoach.trainer.theme.CoachTheme
import com.aifootballcoach.trainer.ui.brief.BriefConditionsScreen
import com.aifootballcoach.trainer.ui.brief.BriefFocusScreen
import com.aifootballcoach.trainer.ui.brief.BriefWhoScreen
import com.aifootballcoach.trainer.ui.brief.DraftingScreen
import com.aifootballcoach.trainer.ui.components.Tab
import com.aifootballcoach.trainer.ui.home.MainShell
import com.aifootballcoach.trainer.ui.onboarding.AddPlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.InvitePlayersScreen
import com.aifootballcoach.trainer.ui.onboarding.TeamSetupScreen
import com.aifootballcoach.trainer.ui.onboarding.WelcomeScreen
import com.aifootballcoach.trainer.ui.player.PlayerScreen
import com.aifootballcoach.trainer.ui.player.RecordDrillSheet
import com.aifootballcoach.trainer.ui.session.BlockStatus
import com.aifootballcoach.trainer.ui.session.EditBlockScreen
import com.aifootballcoach.trainer.ui.session.ReviewBlocksScreen
import com.aifootballcoach.trainer.ui.session.SendSessionScreen
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Renders each screen to a PNG in build/snapshots so the UI can be compared
 * with the design without an emulator.
 */
class ScreenSnapshotTest {
    private val repository = FakeTeamRepository()

    @Test
    fun renderOnboardingScreens() {
        snapshot("11-welcome") { WelcomeScreen(onStart = {}) }
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
        val sem = repository.players().first { it.id == "2" }
        val profile = runBlocking { repository.playerProfile(sem.id) }
        val player: @Composable () -> Unit = {
            PlayerScreen(player = sem, profile = profile, onBack = {}, onRecordDrill = {})
        }
        snapshot("04-player", player)
        snapshot("15-record-drill") {
            Box {
                player()
                RecordDrillSheet(sem, onSave = { _, _, _ -> }, onDismiss = {}, initialAttempts = "100", initialSuccesses = "70")
            }
        }
    }

    @Test
    fun renderBriefScreens() {
        val players = repository.players()
        val suggestions = repository.focusSuggestions()
        snapshot("assistant") { MainShell(repository, initialTab = Tab.Assistant) }
        snapshot("17-brief-who") {
            BriefWhoScreen(
                session = repository.nextSession(),
                players = players,
                absentIds = players.filter { it.availability == Availability.Out }.map { it.id }.toSet(),
                onToggleAbsent = {},
                onNext = {},
                onClose = {},
            )
        }
        snapshot("brief-focus") {
            BriefFocusScreen(
                suggestions = suggestions,
                selectedIds = setOf(suggestions.first().id),
                onToggle = {},
                ownTheme = "",
                onOwnThemeChange = {},
                onNext = {},
                onBack = {},
            )
        }
        snapshot("18-brief-conditions") {
            BriefConditionsScreen(
                space = PitchSpace.Half,
                onSpaceChange = {},
                intensity = Intensity.Medium,
                onIntensityChange = {},
                notes = "Twee keepers. JO15 heeft de andere helft.",
                onNotesChange = {},
                onDraft = {},
                onBack = {},
            )
        }
        snapshot("19-drafting") {
            DraftingScreen(
                day = "Donderdag",
                steps = listOf(
                    "Stats van 14 spelers gelezen",
                    "Thema's gekozen",
                    "Concept voor een half veld opgeslagen",
                ),
                stepsDone = 2,
            )
        }
    }

    @Test
    fun renderSessionScreens() {
        val brief = SessionBrief(
            session = repository.nextSession(),
            absentPlayerIds = setOf("4"),
            focus = listOf("Verdedigen", "Passen"),
            space = PitchSpace.Half,
            intensity = Intensity.Medium,
            notes = "",
        )
        val blocks = repository.draftSession(brief).blocks
        snapshot("07-review-block") {
            ReviewBlocksScreen(
                blocks = blocks,
                index = 1,
                statuses = mapOf(blocks[0].id to BlockStatus.Accepted),
                onAccept = {},
                onEdit = {},
                onSkip = {},
                onClose = {},
            )
        }
        snapshot("20-edit-block") { EditBlockScreen(blocks[1], number = 2, onSave = {}, onCancel = {}) }
        snapshot("21-send") {
            SendSessionScreen(
                totalBlocks = blocks.size,
                acceptedBlocks = blocks.size - 1,
                minutes = blocks.drop(1).sumOf { it.minutes },
                editedBlocks = 1,
                skippedBlocks = 1,
                players = 4,
                homeExercises = true,
                onHomeExercisesChange = {},
                onSend = {},
                onBack = {},
            )
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
