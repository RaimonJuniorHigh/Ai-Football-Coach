package com.aifootballcoach.trainer

import com.aifootballcoach.trainer.data.SessionDraft
import com.aifootballcoach.trainer.data.Team
import com.aifootballcoach.trainer.data.Weekday
import com.aifootballcoach.trainer.ui.sessions.SessionLists
import com.aifootballcoach.trainer.ui.sessions.SessionStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionListTest {
    // Woensdag 18 november 2026, het team traint op dinsdag en donderdag.
    private val today = LocalDate(2026, 11, 18)
    private val team = Team("JO17-1", setOf(Weekday.Tue, Weekday.Thu), 90)

    private fun session(date: LocalDate, theme: String, sentTo: Int? = null) =
        SessionDraft(day = "", dateLabel = "", theme = theme, minutes = 75, sentTo = sentTo, date = date)

    @Test
    fun upcomingAreTheNextTrainingDays() {
        val list = SessionLists.build(today, team, draft = null, sent = emptyList())
        assertEquals(listOf(LocalDate(2026, 11, 19), LocalDate(2026, 11, 24)), list.upcoming.map { it.date })
        assertTrue(list.upcoming.all { it.status == SessionStatus.Empty && it.minutes == 90 })
        assertTrue(list.past.isEmpty())
    }

    @Test
    fun draftAndSentSessionsLandOnTheirDay() {
        val draft = session(LocalDate(2026, 11, 24), "Passen")
        val sent = listOf(session(LocalDate(2026, 11, 19), "Verdedigen", sentTo = 14))
        val upcoming = SessionLists.build(today, team, draft, sent).upcoming
        assertEquals(SessionStatus.Sent, upcoming[0].status)
        assertEquals("Verdedigen", upcoming[0].title)
        assertEquals(SessionStatus.Draft, upcoming[1].status)
        assertEquals(75, upcoming[1].minutes)
    }

    @Test
    fun pastShowsSentSessionsNewestFirst() {
        val sent = listOf(
            session(LocalDate(2026, 11, 10), "Rondo", sentTo = 16),
            session(LocalDate(2026, 11, 17), "Afwerken", sentTo = 14),
            session(LocalDate(2026, 11, 19), "Verdedigen", sentTo = 15),
        )
        val past = SessionLists.build(today, team, draft = null, sent = sent).past
        assertEquals(listOf("Afwerken", "Rondo"), past.map { it.title })
        assertEquals(14, past.first().players)
    }

    @Test
    fun noTrainingDaysMeansNoUpcoming() {
        val list = SessionLists.build(today, team.copy(trainingDays = emptySet()), draft = null, sent = emptyList())
        assertTrue(list.upcoming.isEmpty())
    }
}
