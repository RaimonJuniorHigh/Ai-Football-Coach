package com.aifootballcoach.trainer.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import com.aifootballcoach.trainer.data.PlayerProfile
import com.aifootballcoach.trainer.data.StatType
import com.aifootballcoach.trainer.data.TeamRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Player page plus the sheet to note a training result. The profile is loaded from the database. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerFlow(repository: TeamRepository, startPlayerId: String, onClose: () -> Unit) {
    val player = remember { repository.players().first { it.id == startPlayerId } }
    var sheetOpen by remember { mutableStateOf(false) }
    var sheetError by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<PlayerProfile?>(null) }
    // Bumped after a result is saved so the profile is loaded again.
    var version by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(player.id, version) {
        try {
            profile = repository.playerProfile(player.id)
            loadError = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            loadError = e.message ?: "Onbekende fout"
        }
    }

    BackHandler {
        if (sheetOpen) sheetOpen = false else onClose()
    }

    Box(Modifier.fillMaxSize()) {
        PlayerScreen(
            player = player,
            profile = profile,
            loadError = loadError,
            onBack = onClose,
            onRecordDrill = {
                sheetError = null
                sheetOpen = true
            },
        )

        if (sheetOpen) {
            RecordDrillSheet(
                player = player,
                error = sheetError,
                onSave = { stat: StatType, attempts, successes ->
                    scope.launch {
                        try {
                            repository.recordDrill(player.id, stat, attempts, successes)
                            version++
                            sheetOpen = false
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            sheetError = e.message ?: "Opslaan is niet gelukt"
                        }
                    }
                },
                onDismiss = { sheetOpen = false },
            )
        }
    }
}
