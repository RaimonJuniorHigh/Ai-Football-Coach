package com.aifootballcoach.trainer.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.DrillResult
import com.aifootballcoach.trainer.data.MatchAppearance
import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.PlayerProfile
import com.aifootballcoach.trainer.data.RatingColors
import com.aifootballcoach.trainer.data.RatingTier
import com.aifootballcoach.trainer.data.StatType
import com.aifootballcoach.trainer.data.formatShortDate
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.InitialsAvatar
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.components.SectionLabel
import com.aifootballcoach.trainer.ui.components.StatCard
import com.aifootballcoach.trainer.ui.squad.OverallBadge

/** Highest stat; the bars fill up to this. */
private const val MaxStat = 20f

/** The player page: overall, the eight stats, the latest training results and matches. */
@Composable
fun PlayerScreen(
    player: Player,
    profile: PlayerProfile?,
    loadError: String? = null,
    onRetry: () -> Unit = {},
    /** Opens the stats explanation straight away; used for the screenshot test. */
    statsHelpOpen: Boolean = false,
    onBack: () -> Unit,
    onRecordDrill: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CoachColors.Card)
                    .clickable(onClick = onBack),
            ) {
                Icon(CoachIcons.ChevronLeft, contentDescription = "Terug", tint = CoachColors.Ink, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(
                    player.initials,
                    size = 64.dp,
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        player.name,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp),
                        color = CoachColors.Ink,
                    )
                    Text(player.position.label, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
                }
                OverallBadge(profile?.overall)
            }

            if (loadError != null) {
                Spacer(Modifier.height(16.dp))
                Text(loadError, style = MaterialTheme.typography.bodyMedium, color = CoachColors.Alert)
                Text(
                    "Opnieuw proberen",
                    style = MaterialTheme.typography.labelMedium,
                    color = CoachColors.Pitch,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(onClick = onRetry)
                        .minimumInteractiveComponentSize(),
                )
            }

            if (profile != null) {
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(profile.overall?.toString() ?: "–", "totaal", Modifier.weight(1f))
                    StatCard(profile.drills.size.toString(), "oefeningen", Modifier.weight(1f))
                    StatCard(profile.matches.size.toString(), "wedstrijden", Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))
                StatsHeader(initiallyOpen = statsHelpOpen)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatType.entries.forEach { type ->
                        StatRow(type, profile.stats.firstOrNull { it.stat == type }?.rating)
                    }
                }

                Spacer(Modifier.height(24.dp))
                SectionLabel("Laatste oefeningen")
                if (profile.drills.isEmpty()) {
                    Text(
                        "Nog geen oefeningen. Noteer er een om ${player.firstName} te volgen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoachColors.Muted,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    profile.drills.forEach { DrillRow(it) }
                }

                Spacer(Modifier.height(24.dp))
                SectionLabel("Wedstrijden")
                if (profile.matches.isEmpty()) {
                    Text("Nog geen wedstrijden.", style = MaterialTheme.typography.bodyMedium, color = CoachColors.Muted, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    profile.matches.forEach { MatchRow(it) }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        PillButton(
            text = "Oefening noteren",
            onClick = onRecordDrill,
            containerColor = CoachColors.Pitch,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
    }
}

/** Score ranges per colour level, in the order of [RatingTier]. */
private val tierRanges = listOf(
    RatingTier.VeryLow to "1–4",
    RatingTier.Low to "5–8",
    RatingTier.Average to "9–12",
    RatingTier.Good to "13–16",
    RatingTier.Excellent to "17–20",
)

/** "Stats" header with a "Wat betekent dit?" link that opens a short explanation of the scores and colours. */
@Composable
private fun StatsHeader(initiallyOpen: Boolean) {
    var open by remember { mutableStateOf(initiallyOpen) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel("Stats", Modifier.weight(1f))
        Text(
            if (open) "Sluiten" else "Wat betekent dit?",
            style = MaterialTheme.typography.labelMedium,
            color = CoachColors.Pitch,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(percent = 50))
                .clickable { open = !open }
                .minimumInteractiveComponentSize()
                .padding(horizontal = 8.dp),
        )
    }
    if (open) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CoachColors.Card)
                .padding(14.dp),
        ) {
            Text(
                "Elke stat is een cijfer van 1 tot 20. Het komt uit de oefeningen die je noteert: hoe vaker iets lukt, " +
                    "hoe hoger het cijfer. Het totaal (1 tot 99) is het gemiddelde van de stats, waarbij per positie " +
                    "andere stats zwaarder tellen. Een streepje betekent dat er nog geen oefening voor die stat is.",
                style = MaterialTheme.typography.bodyMedium,
                color = CoachColors.Ink,
            )
            Spacer(Modifier.height(10.dp))
            tierRanges.forEach { (tier, range) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(tier.argb)))
                    Spacer(Modifier.width(8.dp))
                    Text(range, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CoachColors.Ink, modifier = Modifier.width(44.dp))
                    Text(tier.label, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
                }
            }
        }
    }
}

/** One stat with its number from 1 to 20 and a bar in the colour of its level; a dash while there is no result. */
@Composable
private fun StatRow(type: StatType, rating: Int?) {
    val color = rating?.let { Color(RatingColors.tierForStat(it).argb) } ?: CoachColors.Track
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                type.label,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = CoachColors.Ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                rating?.toString() ?: "–",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = if (rating == null) CoachColors.Muted else CoachColors.Ink,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CoachColors.Chip),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(((rating ?: 0) / MaxStat).coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun DrillRow(drill: DrillResult) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(drill.stat.label, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = CoachColors.Ink)
            Text(formatShortDate(drill.recordedOn), style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        }
        Text(
            "${drill.successes} van ${drill.attempts}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = CoachColors.Pitch,
        )
    }
}

@Composable
private fun MatchRow(match: MatchAppearance) {
    val goalsAndAssists = listOfNotNull(
        match.goals.takeIf { it > 0 }?.let { "$it goal${if (it == 1) "" else "s"}" },
        match.assists.takeIf { it > 0 }?.let { "$it assist${if (it == 1) "" else "s"}" },
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${if (match.home) "Thuis" else "Uit"} tegen ${match.opponent}",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = CoachColors.Ink,
            )
            Text(
                (listOf("${formatShortDate(match.playedOn)}", "${match.minutes} min") + goalsAndAssists).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = CoachColors.Muted,
            )
        }
        Text(
            "${match.goalsFor}–${match.goalsAgainst}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = CoachColors.Ink,
        )
    }
}
