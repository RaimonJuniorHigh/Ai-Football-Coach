package com.aifootballcoach.trainer

import com.aifootballcoach.trainer.data.Player
import com.aifootballcoach.trainer.data.PlayerStat
import com.aifootballcoach.trainer.data.Position
import com.aifootballcoach.trainer.data.RatingColors
import com.aifootballcoach.trainer.data.RatingTier
import com.aifootballcoach.trainer.data.SquadInsights
import com.aifootballcoach.trainer.data.StatType
import com.aifootballcoach.trainer.data.Team
import com.aifootballcoach.trainer.data.Weekday
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SquadInsightsTest {
    private val sem = Player("1", "Sem Vossen", Position.Defence)
    private val jesse = Player("2", "Jesse Daniëls", Position.Midfield)
    private val stats = mapOf(
        "1" to listOf(PlayerStat(StatType.DEFENDING, 3), PlayerStat(StatType.PACE, 15)),
        "2" to listOf(PlayerStat(StatType.PASSING, 7), PlayerStat(StatType.STAMINA, 14)),
    )

    @Test
    fun tiersFollowTheStatScale() {
        assertEquals(RatingTier.VeryLow, RatingColors.tierForStat(1))
        assertEquals(RatingTier.VeryLow, RatingColors.tierForStat(4))
        assertEquals(RatingTier.Low, RatingColors.tierForStat(5))
        assertEquals(RatingTier.Average, RatingColors.tierForStat(12))
        assertEquals(RatingTier.Good, RatingColors.tierForStat(16))
        assertEquals(RatingTier.Excellent, RatingColors.tierForStat(20))
    }

    @Test
    fun overallIsConvertedToTheSameColours() {
        assertEquals(RatingTier.VeryLow, RatingColors.tierForOverall(19))
        assertEquals(RatingTier.Average, RatingColors.tierForOverall(50))
        assertEquals(RatingTier.Excellent, RatingColors.tierForOverall(99))
    }

    @Test
    fun needsYouListsOnlyPlayersWithAVeryLowStat() {
        val result = SquadInsights.needsYou(listOf(sem, jesse), stats)
        assertEquals(listOf(sem), result.map { it.player })
        assertEquals("Verdedigen: 3 van 20 — zwakste punt", result.single().detail)
    }

    @Test
    fun focusSuggestionsCountPlayersWithALowStat() {
        val result = SquadInsights.focusSuggestions(stats)
        assertEquals(listOf("Passen", "Verdedigen"), result.map { it.title })
        assertEquals("1 speler onder de 9 van 20", result.first().reason)
    }

    @Test
    fun noStatsGiveNoSuggestions() {
        assertEquals(emptyList(), SquadInsights.focusSuggestions(emptyMap()))
        assertEquals(emptyList(), SquadInsights.needsYou(listOf(sem), emptyMap()))
    }

    @Test
    fun nextTrainingDayIsTodayWhenTodayIsATrainingDay() {
        val tuesday = LocalDate(2026, 11, 3)
        assertEquals(tuesday, SquadInsights.nextTrainingDay(tuesday, setOf(Weekday.Tue, Weekday.Thu)))
    }

    @Test
    fun nextTrainingDaySkipsTheWeekend() {
        val saturday = LocalDate(2026, 11, 7)
        assertEquals(LocalDate(2026, 11, 9), SquadInsights.nextTrainingDay(saturday, setOf(Weekday.Mon)))
    }

    @Test
    fun noTrainingDaysGiveNoNextDay() {
        assertNull(SquadInsights.nextTrainingDay(LocalDate(2026, 11, 3), emptySet()))
    }

    @Test
    fun nextSessionHasDutchLabels() {
        val team = Team("JO17-1", setOf(Weekday.Thu), 90)
        val session = SquadInsights.nextSession(LocalDate(2026, 11, 3), team)
        assertEquals("Donderdag", session.day)
        assertEquals("Do 5 nov", session.dateLabel)
        assertEquals(90, session.minutes)
    }
}
