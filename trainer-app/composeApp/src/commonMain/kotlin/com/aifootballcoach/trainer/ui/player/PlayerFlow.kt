package com.aifootballcoach.trainer.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import com.aifootballcoach.trainer.data.TeamRepository

private sealed interface PlayerSheet {
    data object AddPoint : PlayerSheet
    data class Observation(val pointId: String) : PlayerSheet
}

/**
 * Player page plus its two sheets. After saving an observation it moves straight
 * on to the next player with open points, so the trainer can go round the squad
 * after training.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerFlow(repository: TeamRepository, startPlayerId: String, onClose: () -> Unit) {
    var playerId by remember { mutableStateOf(startPlayerId) }
    var sheet by remember { mutableStateOf<PlayerSheet?>(null) }
    // Bumped after every write so the lists below are read again from the repository.
    var version by remember { mutableIntStateOf(0) }

    val players = remember { repository.players() }
    val player = players.first { it.id == playerId }
    val points = remember(playerId, version) { repository.developmentPoints(playerId) }
    val stats = remember(playerId) { repository.playerStats(playerId) }
    val nextPlayer = remember(playerId, version) {
        players.drop(players.indexOf(player) + 1)
            .firstOrNull { repository.developmentPoints(it.id).isNotEmpty() }
    }

    BackHandler {
        if (sheet != null) sheet = null else onClose()
    }

    Box(Modifier.fillMaxSize()) {
        PlayerScreen(
            player = player,
            stats = stats,
            points = points,
            onBack = onClose,
            onAddPoint = { sheet = PlayerSheet.AddPoint },
            onOpenPoint = { sheet = PlayerSheet.Observation(it.id) },
            onLogObservation = { points.firstOrNull()?.let { sheet = PlayerSheet.Observation(it.id) } },
        )

        when (val current = sheet) {
            PlayerSheet.AddPoint -> AddPointSheet(
                player = player,
                onSave = { title, category, score ->
                    repository.addDevelopmentPoint(player.id, title, category, score)
                    version++
                    sheet = null
                },
                onDismiss = { sheet = null },
            )

            is PlayerSheet.Observation -> points.firstOrNull { it.id == current.pointId }?.let { point ->
                LogObservationSheet(
                    player = player,
                    point = point,
                    hasNextPlayer = nextPlayer != null,
                    onSave = { rating, note ->
                        repository.logObservation(point.id, rating, note)
                        version++
                        val next = nextPlayer
                        if (next == null) {
                            sheet = null
                        } else {
                            playerId = next.id
                            sheet = PlayerSheet.Observation(repository.developmentPoints(next.id).first().id)
                        }
                    },
                    onDismiss = { sheet = null },
                )
            }

            null -> Unit
        }
    }
}
