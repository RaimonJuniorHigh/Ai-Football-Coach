package com.aifootballcoach.trainer.data

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val dayNames = listOf("Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag", "Zondag")
private val dayNamesShort = listOf("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo")
private val monthNames = listOf(
    "januari", "februari", "maart", "april", "mei", "juni",
    "juli", "augustus", "september", "oktober", "november", "december",
)
private val monthNamesShort = listOf("jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec")

/** The current local date and time. */
fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.dayName(): String = dayNames[dayOfWeek.ordinal]

/** "Do 24 sep" */
fun LocalDate.shortLabel(): String = "${dayNamesShort[dayOfWeek.ordinal]} $day ${monthNamesShort[month.ordinal]}"

/** "Dinsdag 22 september" */
fun LocalDate.longLabel(): String = "${dayName()} $day ${monthNames[month.ordinal]}"

/** "3 nov" from a date like "2026-11-03"; an unreadable date is shown as it came. */
fun formatShortDate(iso: String): String =
    runCatching { LocalDate.parse(iso.take(10)).let { "${it.day} ${monthNamesShort[it.month.ordinal]}" } }.getOrDefault(iso)

fun greetingFor(hour: Int): String = when {
    hour < 6 -> "Goedennacht"
    hour < 12 -> "Goedemorgen"
    hour < 18 -> "Goedemiddag"
    else -> "Goedenavond"
}
