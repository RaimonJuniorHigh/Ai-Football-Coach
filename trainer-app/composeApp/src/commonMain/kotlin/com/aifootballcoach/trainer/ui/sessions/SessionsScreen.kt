package com.aifootballcoach.trainer.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.dayMonth
import com.aifootballcoach.trainer.data.dayShort
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons

/** Design screen 22 — "Sessions": the coming trainings and the ones that were already sent. */
@Composable
fun SessionsScreen(
    sessions: SessionList,
    squadSize: Int,
    onNewSession: () -> Unit,
    onOpenUpcoming: (UpcomingSession) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Trainingen", style = MaterialTheme.typography.headlineLarge, color = CoachColors.Ink, modifier = Modifier.weight(1f))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CoachColors.Pitch)
                        .clickable(onClick = onNewSession),
                ) {
                    Icon(CoachIcons.Plus, contentDescription = "Nieuwe training", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            SectionLabel("Komend")
        }

        if (sessions.upcoming.isEmpty()) {
            item { EmptyCard("Kies eerst trainingsdagen bij je team, dan zie je hier je komende trainingen.") }
        }
        items(sessions.upcoming, key = { it.date.toString() }) { session ->
            UpcomingCard(session, onClick = { onOpenUpcoming(session) })
            Spacer(Modifier.height(8.dp))
        }

        item {
            Spacer(Modifier.height(12.dp))
            SectionLabel("Eerder")
            if (sessions.past.isEmpty()) {
                EmptyCard("Nog geen trainingen gehad. Trainingen die je verstuurt, komen hier te staan.")
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CoachColors.Card),
                ) {
                    sessions.past.forEachIndexed { index, session ->
                        if (index > 0) HorizontalDivider(color = CoachColors.Paper)
                        PastRow(session, squadSize)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = CoachColors.Muted,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun EmptyCard(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = CoachColors.Muted,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .padding(14.dp),
    )
}

@Composable
private fun UpcomingCard(session: UpcomingSession, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CoachColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(Modifier.width(36.dp)) {
            Text(session.date.dayShort(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = CoachColors.Muted)
            Text(
                session.date.day.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
                color = CoachColors.Ink,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                session.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = CoachColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("${session.minutes} min", style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        }
        Spacer(Modifier.width(8.dp))
        StatusTag(session.status)
    }
}

@Composable
private fun StatusTag(status: SessionStatus) {
    val (background, text) = when (status) {
        SessionStatus.Draft -> CoachColors.AlertSoft to CoachColors.Alert
        SessionStatus.Sent -> CoachColors.PitchSoft to CoachColors.Pitch
        SessionStatus.Empty -> CoachColors.Chip to CoachColors.Muted
    }
    Text(
        status.label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        color = text,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun PastRow(session: PastSession, squadSize: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Text(
            session.date.dayMonth(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = CoachColors.Ink,
            modifier = Modifier.width(60.dp),
        )
        Text(
            session.title,
            style = MaterialTheme.typography.bodyMedium,
            color = CoachColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        session.players?.let {
            Text("$it van $squadSize", style = MaterialTheme.typography.labelSmall, color = CoachColors.Muted)
        }
    }
}
