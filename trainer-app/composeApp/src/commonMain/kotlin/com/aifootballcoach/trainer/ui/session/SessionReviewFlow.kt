package com.aifootballcoach.trainer.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import com.aifootballcoach.trainer.data.SessionDraft
import com.aifootballcoach.trainer.data.TeamRepository

/**
 * Going through a draft (07), editing a block (20) and sending it (21).
 * Calls [onSent] after the session is sent, [onClose] when the trainer leaves halfway.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SessionReviewFlow(
    repository: TeamRepository,
    draft: SessionDraft,
    onClose: () -> Unit,
    onSent: () -> Unit,
    onDarkBackground: (Boolean) -> Unit = {},
) {
    var blocks by remember { mutableStateOf(draft.blocks) }
    var index by remember { mutableIntStateOf(0) }
    var statuses by remember { mutableStateOf(emptyMap<String, BlockStatus>()) }
    var editedIds by remember { mutableStateOf(emptySet<String>()) }
    var editing by remember { mutableStateOf(false) }
    var homeExercises by remember { mutableStateOf(true) }
    val reviewed = statuses.size == blocks.size

    fun decide(status: BlockStatus) {
        statuses = statuses + (blocks[index].id to status)
        if (index < blocks.lastIndex) index++
    }

    BackHandler {
        when {
            editing -> editing = false
            // Back from the send screen, or within the review, goes one block back and undoes that choice.
            reviewed || index > 0 -> {
                if (!reviewed) index--
                statuses = statuses - blocks[index].id
            }
            else -> onClose()
        }
    }
    LaunchedEffect(editing, reviewed) { onDarkBackground(!editing && !reviewed) }

    when {
        blocks.isEmpty() -> LaunchedEffect(Unit) { onClose() }

        editing -> EditBlockScreen(
            block = blocks[index],
            number = index + 1,
            onSave = { edited ->
                blocks = blocks.map { if (it.id == edited.id) edited else it }
                editedIds = editedIds + edited.id
                editing = false
                decide(BlockStatus.Accepted)
            },
            onCancel = { editing = false },
        )

        reviewed -> {
            val accepted = blocks.filter { statuses[it.id] == BlockStatus.Accepted }
            val players = blocks.first().players
            SendSessionScreen(
                totalBlocks = blocks.size,
                acceptedBlocks = accepted.size,
                minutes = accepted.sumOf { it.minutes },
                editedBlocks = accepted.count { it.id in editedIds },
                skippedBlocks = blocks.size - accepted.size,
                players = players,
                homeExercises = homeExercises,
                onHomeExercisesChange = { homeExercises = it },
                onSend = {
                    repository.sendDraft(
                        draft.copy(blocks = accepted, minutes = accepted.sumOf { it.minutes }, sentTo = players),
                        homeExercises,
                    )
                    onSent()
                },
                onBack = { statuses = statuses - blocks[index].id },
            )
        }

        else -> ReviewBlocksScreen(
            blocks = blocks,
            index = index,
            statuses = statuses,
            onAccept = { decide(BlockStatus.Accepted) },
            onEdit = { editing = true },
            onSkip = { decide(BlockStatus.Skipped) },
            onClose = {
                onDarkBackground(false)
                onClose()
            },
        )
    }
}
