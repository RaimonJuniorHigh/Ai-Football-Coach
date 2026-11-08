package com.aifootballcoach.trainer.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.aifootballcoach.trainer.data.SessionBlock
import com.aifootballcoach.trainer.theme.CoachColors
import com.aifootballcoach.trainer.ui.components.CoachIcons

/** What the trainer did with a block while going through the draft. */
enum class BlockStatus { Accepted, Skipped }

/**
 * Design screen 07 — the draft, one block at a time. The trainer accepts, edits or skips
 * each block; the bar at the top shows what's done.
 */
@Composable
fun ReviewBlocksScreen(
    blocks: List<SessionBlock>,
    index: Int,
    statuses: Map<String, BlockStatus>,
    onAccept: () -> Unit,
    onEdit: () -> Unit,
    onSkip: () -> Unit,
    onClose: () -> Unit,
) {
    val block = blocks[index]
    val accepted = blocks.filter { statuses[it.id] == BlockStatus.Accepted }

    Column(
        Modifier
            .fillMaxSize()
            .background(CoachColors.Ink)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            RoundIconButton(CoachIcons.Close, "Sluiten", size = 32.dp, onClick = onClose)
            Spacer(Modifier.width(10.dp))
            Text(
                "Blok ${index + 1} van ${blocks.size}",
                style = MaterialTheme.typography.labelSmall,
                color = CoachColors.OnPitchMuted,
            )
            Spacer(Modifier.width(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                blocks.forEachIndexed { i, b ->
                    val color = when {
                        statuses[b.id] == BlockStatus.Accepted -> CoachColors.Lime
                        statuses[b.id] == BlockStatus.Skipped -> Color.White.copy(alpha = 0.25f)
                        i == index -> Color.White
                        else -> Color.White.copy(alpha = 0.12f)
                    }
                    Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(color))
                }
            }
            Spacer(Modifier.width(10.dp))
            Text("${accepted.sumOf { it.minutes }}'", style = MaterialTheme.typography.labelSmall, color = CoachColors.OnPitchMuted)
        }

        Spacer(Modifier.height(16.dp))
        // Card plus status line take the free space; the card scrolls if the text is long.
        Column(Modifier.weight(1f)) {
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CoachColors.Card)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
            ) {
                Text(
                    "VOORSTEL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.1.em),
                    color = CoachColors.Lime,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(CoachColors.Pitch)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    block.drill,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 29.sp),
                    color = CoachColors.Ink,
                )
                Text(block.summary, style = MaterialTheme.typography.bodyMedium, color = CoachColors.Muted)

                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("${block.minutes} min", block.space.label, block.intensity.label).forEach { InfoChip(it) }
                }

                Spacer(Modifier.height(14.dp))
                Text(block.howItWorks, style = MaterialTheme.typography.bodyMedium, color = CoachColors.Ink)

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = CoachColors.Chip)
                Spacer(Modifier.height(12.dp))
                Text(
                    "WAAROM",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.12.em),
                    color = CoachColors.Muted,
                )
                Spacer(Modifier.height(6.dp))
                block.why.forEach { reason ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(CoachColors.Pitch))
                        Spacer(Modifier.width(8.dp))
                        Text(reason, style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp), color = CoachColors.Ink)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                Icon(CoachIcons.Check, contentDescription = null, tint = CoachColors.Lime, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(6.dp))
                val toGo = blocks.size - statuses.size - 1
                Text(
                    "${accepted.size} geaccepteerd · ${if (toGo == 0) "dit is de laatste" else "nog $toGo te gaan"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoachColors.OnPitchMuted,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
        ) {
            RoundIconButton(CoachIcons.Close, "Overslaan", onClick = onSkip)
            RoundIconButton(CoachIcons.Pencil, "Aanpassen", onClick = onEdit)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(CoachColors.Lime)
                    .clickable(onClick = onAccept),
            ) {
                Icon(CoachIcons.Check, contentDescription = null, tint = CoachColors.Ink, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Accepteren", style = MaterialTheme.typography.labelLarge, color = CoachColors.Ink)
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = CoachColors.Ink,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(CoachColors.Chip)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun RoundIconButton(icon: ImageVector, description: String, size: Dp = 52.dp, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(size * 0.36f))
    }
}
