package com.aifootballcoach.trainer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aifootballcoach.trainer.data.Availability
import com.aifootballcoach.trainer.data.PlayerFlag
import com.aifootballcoach.trainer.data.TeamRepository
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachBottomBar
import com.aifootballcoach.trainer.ui.components.Tab
import com.aifootballcoach.trainer.ui.squad.SquadScreen

/** Height of the floating tab bar plus its bottom margin; content scrolls under it. */
private val BottomBarSpace = 60.dp + 12.dp + 16.dp

/** The signed-in app: tab content with the floating bottom bar on top. */
@Composable
fun MainShell(repository: TeamRepository, initialTab: Tab = Tab.Today) {
    var tab by remember { mutableStateOf(initialTab) }
    val players = remember { repository.players() }

    val statusBar = WindowInsets.statusBars.asPaddingValues()
    val navBar = WindowInsets.navigationBars.asPaddingValues()
    val contentPadding = PaddingValues(
        top = statusBar.calculateTopPadding(),
        bottom = navBar.calculateBottomPadding() + BottomBarSpace,
    )

    Box(Modifier.fillMaxSize().background(CoachColors.Paper)) {
        when (tab) {
            Tab.Today -> TodayScreen(
                trainer = repository.trainer(),
                // TODO: real date and time-of-day greeting once kotlinx-datetime is added.
                dateLabel = "Dinsdag 22 september",
                greeting = "Goedenavond",
                draft = repository.nextSessionDraft(),
                stats = SquadStats(
                    available = players.count { it.availability == Availability.In },
                    squad = players.size,
                    stalled = players.count { it.flag == PlayerFlag.Stalled },
                ),
                needsYou = repository.needsYou(),
                onReviewDraft = { /* TODO: session review (design 07) */ },
                onOpenAttention = { /* TODO: player detail (design 04) */ },
                contentPadding = contentPadding,
            )

            Tab.Squad -> SquadScreen(
                teamName = repository.loadTeam().name,
                players = players,
                onOpenPlayer = { /* TODO: player detail (design 04) */ },
                contentPadding = contentPadding,
            )

            Tab.Assistant, Tab.Sessions -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(contentPadding),
            ) {
                Text("${tab.label} komt later", style = MaterialTheme.typography.bodyLarge, color = CoachColors.Muted)
            }
        }

        CoachBottomBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        )
    }
}
