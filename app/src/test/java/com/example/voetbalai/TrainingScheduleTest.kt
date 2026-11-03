package com.example.voetbalai

import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.schedule.daysAheadLabel
import com.example.voetbalai.domain.schedule.greeting
import com.example.voetbalai.domain.schedule.nextTraining
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingScheduleTest {
    private fun date(y: Int, m: Int, d: Int) = Calendar.getInstance().apply { clear(); set(y, m, d) }
    private val days = listOf(Day.TUE, Day.THU)

    @Test fun mondayToTuesdayIsTomorrow() {
        val next = nextTraining(days, date(2026, Calendar.OCTOBER, 26))!!
        assertEquals(1, next.daysAhead)
        assertEquals(Calendar.TUESDAY, next.date.get(Calendar.DAY_OF_WEEK))
    }

    @Test fun trainingDayItselfIsToday() =
        assertEquals(0, nextTraining(days, date(2026, Calendar.OCTOBER, 27))!!.daysAhead)

    @Test fun fridayWrapsToTuesday() =
        assertEquals(4, nextTraining(days, date(2026, Calendar.OCTOBER, 30))!!.daysAhead)

    @Test fun noDaysGivesNull() = assertNull(nextTraining(emptyList(), date(2026, Calendar.OCTOBER, 26)))

    @Test fun labels() {
        assertEquals("vandaag", daysAheadLabel(0))
        assertEquals("morgen", daysAheadLabel(1))
        assertEquals("over 2 dagen", daysAheadLabel(2))
    }

    @Test fun greetings() {
        assertEquals("Goedemorgen", greeting(8))
        assertEquals("Goedemiddag", greeting(14))
        assertEquals("Goedenavond", greeting(20))
        assertEquals("Goedenavond", greeting(2))
    }
}
