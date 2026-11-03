package com.aifootballcoach.trainer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.Attention
import com.aifootballcoach.trainer.data.SessionDraft
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons
import com.aifootballcoach.trainer.ui.components.InitialsAvatar
import com.aifootballcoach.trainer.ui.components.PillButton
import com.aifootballcoach.trainer.ui.components.SectionLabel
import com.aifootballcoach.trainer.ui.components.StatCard

data class SquadStats(val available: Int, val squad: Int, val needsAttention: Int)

/** Design screen 02 — "Today", the trainer dashboard. */
@Composable
fun TodayScreen(
    dateLabel: String,
    greeting: String,
    draft: SessionDraft?,
    stats: SquadStats,
    needsYou: List<Attention>,
    onReviewDraft: () -> Unit,
    onOpenAttention: (Attention) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(dateLabel, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
                Text(
                    greeting,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp, lineHeight = 26.sp),
                    color = CoachColors.Ink,
                )
            }
        }

        if (draft != null) {
            Spacer(Modifier.height(16.dp))
            DraftCard(draft, onReviewDraft)
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(stats.available.toString(), "beschikbaar", Modifier.weight(1f))
            StatCard(stats.squad.toString(), "selectie", Modifier.weight(1f))
            StatCard(stats.needsAttention.toString(), "aandacht", Modifier.weight(1f), valueColor = CoachColors.Alert)
        }

        if (needsYou.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("Vraagt aandacht")
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                needsYou.forEach { item -> AttentionRow(item) { onOpenAttention(item) } }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DraftCard(draft: SessionDraft, onReview: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CoachColors.Pitch)
            .padding(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(CoachColors.Lime)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Icon(CoachIcons.Plus, contentDescription = null, tint = CoachColors.Ink, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "CONCEPT KLAAR",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.1.em,
                ),
                color = CoachColors.Ink,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "${draft.day}\n${draft.dateLabel}",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 28.sp),
            color = Color.White,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${draft.theme} · ${draft.minutes} min",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            color = CoachColors.OnPitchMuted,
        )
        Spacer(Modifier.height(16.dp))
        PillButton(
            text = "Concept bekijken",
            onClick = onReview,
            containerColor = CoachColors.Card,
            contentColor = CoachColors.Ink,
            height = 46.dp,
        )
    }
}

@Composable
private fun AttentionRow(item: Attention, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        InitialsAvatar(item.player.initials, size = 32.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.player.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = CoachColors.Ink)
            Text(item.detail, style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        }
        Icon(CoachIcons.ChevronRight, contentDescription = null, tint = CoachColors.Muted, modifier = Modifier.size(16.dp))
    }
}
