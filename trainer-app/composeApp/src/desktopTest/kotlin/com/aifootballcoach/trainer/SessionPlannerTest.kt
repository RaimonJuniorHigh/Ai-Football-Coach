package com.aifootballcoach.trainer

import com.aifootballcoach.trainer.data.Intensity
import com.aifootballcoach.trainer.data.PitchSpace
import com.aifootballcoach.trainer.data.PlannedSession
import com.aifootballcoach.trainer.data.SessionBrief
import com.aifootballcoach.trainer.data.SessionPlanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionPlannerTest {
    private fun brief(focus: List<String>, minutes: Int = 90) = SessionBrief(
        session = PlannedSession("Donderdag", "Do 5 nov", minutes),
        absentPlayerIds = emptySet(),
        focus = focus,
        space = PitchSpace.Half,
        intensity = Intensity.High,
        notes = "",
    )

    @Test
    fun oneBlockPerThemeBetweenWarmUpAndCoolDown() {
        val blocks = SessionPlanner.blocksFor(brief(listOf("Verdedigen", "Passen")), available = 14, reasons = emptyMap())
        assertEquals(listOf("warm-up", "theme-0", "theme-1", "game", "cool-down"), blocks.map { it.id })
        assertEquals("1 tegen 1 verdedigen in een strook", blocks[1].drill)
        assertTrue(blocks.all { it.players == 14 })
    }

    @Test
    fun blocksFillTheSession() {
        val blocks = SessionPlanner.blocksFor(brief(listOf("Verdedigen", "Passen")), available = 14, reasons = emptyMap())
        assertEquals(90, blocks.sumOf { it.minutes })
    }

    @Test
    fun warmUpAndCoolDownStayLight() {
        val blocks = SessionPlanner.blocksFor(brief(listOf("Afwerken")), available = 10, reasons = emptyMap())
        assertEquals(Intensity.Light, blocks.first().intensity)
        assertEquals(Intensity.Light, blocks.last().intensity)
        assertEquals(Intensity.High, blocks[1].intensity)
    }

    @Test
    fun ownThemeGetsAGameWithThatTheme() {
        val blocks = SessionPlanner.blocksFor(brief(listOf("Afronden na een voorzet")), available = 10, reasons = emptyMap())
        assertEquals("Partijvorm: Afronden na een voorzet", blocks[1].drill)
        assertEquals(listOf("Thema dat je zelf koos"), blocks[1].why)
    }

    @Test
    fun reasonFromStep2IsShownAsWhy() {
        val blocks = SessionPlanner.blocksFor(
            brief(listOf("Passen")),
            available = 10,
            reasons = mapOf("Passen" to "3 spelers onder de 9 van 20"),
        )
        assertEquals(listOf("3 spelers onder de 9 van 20"), blocks[1].why)
    }

    @Test
    fun atMostThreeThemes() {
        val blocks = SessionPlanner.blocksFor(
            brief(listOf("Afwerken", "Passen", "Dribbelen", "Verdedigen")),
            available = 10,
            reasons = emptyMap(),
        )
        assertEquals(6, blocks.size)
    }
}
