package com.aifootballcoach.trainer.ui.brief

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import com.aifootballcoach.trainer.data.Availability
import com.aifootballcoach.trainer.data.Intensity
import com.aifootballcoach.trainer.data.PitchSpace
import com.aifootballcoach.trainer.data.SessionBrief
import com.aifootballcoach.trainer.data.TeamRepository
import kotlinx.coroutines.delay

private enum class BriefStep { Who, Focus, Conditions, Drafting }

/** How long each line on the drafting screen stays up. */
private const val DraftStepMillis = 1100L

/**
 * The three-step brief (17, own step 2, 18) and the drafting screen (19).
 * Calls [onDrafted] once the assistant's draft is saved, [onClose] when cancelled.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BriefFlow(
    repository: TeamRepository,
    onClose: () -> Unit,
    onDrafted: () -> Unit,
    onDarkBackground: (Boolean) -> Unit = {},
) {
    val session = remember { repository.nextSession() }
    val players = remember { repository.players() }
    val suggestions = remember { repository.focusSuggestions() }

    var step by remember { mutableStateOf(BriefStep.Who) }
    var absentIds by remember { mutableStateOf(players.filter { it.availability == Availability.Out }.map { it.id }.toSet()) }
    var focusIds by remember { mutableStateOf(setOfNotNull(suggestions.firstOrNull()?.id)) }
    var ownTheme by remember { mutableStateOf("") }
    var space by remember { mutableStateOf(PitchSpace.Half) }
    var intensity by remember { mutableStateOf(Intensity.Medium) }
    var notes by remember { mutableStateOf("") }

    BackHandler {
        when (step) {
            BriefStep.Who -> onClose()
            BriefStep.Focus -> step = BriefStep.Who
            BriefStep.Conditions -> step = BriefStep.Focus
            BriefStep.Drafting -> Unit // can't cancel mid-draft; it only takes a few seconds
        }
    }
    LaunchedEffect(step) { onDarkBackground(step == BriefStep.Drafting) }

    when (step) {
        BriefStep.Who -> BriefWhoScreen(
            session = session,
            players = players,
            absentIds = absentIds,
            onToggleAbsent = { player ->
                absentIds = if (player.id in absentIds) absentIds - player.id else absentIds + player.id
            },
            onNext = { step = BriefStep.Focus },
            onClose = onClose,
        )

        BriefStep.Focus -> BriefFocusScreen(
            suggestions = suggestions,
            selectedIds = focusIds,
            onToggle = { focusIds = if (it.id in focusIds) focusIds - it.id else focusIds + it.id },
            ownTheme = ownTheme,
            onOwnThemeChange = { ownTheme = it },
            onNext = { step = BriefStep.Conditions },
            onBack = { step = BriefStep.Who },
        )

        BriefStep.Conditions -> BriefConditionsScreen(
            space = space,
            onSpaceChange = { space = it },
            intensity = intensity,
            onIntensityChange = { intensity = it },
            notes = notes,
            onNotesChange = { notes = it },
            onDraft = { step = BriefStep.Drafting },
            onBack = { step = BriefStep.Focus },
        )

        BriefStep.Drafting -> {
            val available = players.size - absentIds.size
            val draftSteps = listOf(
                "Stats van $available spelers gelezen",
                "Thema's gekozen",
                if (space == PitchSpace.Half) "Concept voor een half veld opgeslagen" else "Concept voor een heel veld opgeslagen",
            )
            var stepsDone by remember { mutableIntStateOf(0) }
            LaunchedEffect(Unit) {
                while (stepsDone < draftSteps.size) {
                    delay(DraftStepMillis)
                    stepsDone++
                }
                repository.draftSession(
                    SessionBrief(
                        session = session,
                        absentPlayerIds = absentIds,
                        focus = suggestions.filter { it.id in focusIds }.map { it.title } +
                            listOfNotNull(ownTheme.trim().takeIf { it.isNotEmpty() }),
                        space = space,
                        intensity = intensity,
                        notes = notes,
                    ),
                )
                onDarkBackground(false)
                onDrafted()
            }
            DraftingScreen(day = session.day, steps = draftSteps, stepsDone = stepsDone)
        }
    }
}
