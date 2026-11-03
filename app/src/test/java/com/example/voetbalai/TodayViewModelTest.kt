package com.example.voetbalai

import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.viewmodel.today.TodayViewModel
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    private val repository = FakeTeamRepository()

    // Maandag 26 oktober 2026, 14:00
    private val mondayAfternoon = Calendar.getInstance().apply { clear(); set(2026, Calendar.OCTOBER, 26, 14, 0) }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun withViewModel(block: suspend (TodayViewModel) -> Unit) = runTest {
        val vm = TodayViewModel(repository, now = { mondayAfternoon })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { } }
        block(vm)
    }

    @Test fun initialStateShowsTeamAndNextTraining() = withViewModel { vm ->
        val state = vm.uiState.value
        assertEquals("Goedemiddag, trainer", state.greeting)
        assertEquals("JO17-1", state.teamName)
        assertEquals("Dinsdag 27 oktober", state.upcomingTraining?.dateText)
        assertEquals("morgen", state.upcomingTraining?.whenText)
        assertEquals(90, state.upcomingTraining?.durationMinutes)
        assertEquals(6, state.presentCount)
    }

    @Test fun squadIsGroupedByPosition() = withViewModel { vm ->
        val groups = vm.uiState.value.squad
        assertEquals(listOf(Position.KEEPER, Position.DEFENCE, Position.MIDFIELD, Position.ATTACK), groups.map { it.position })
        assertEquals(2, groups.first { it.position == Position.DEFENCE }.players.size)
    }

    @Test fun changingAttendanceUpdatesSummary() = withViewModel { vm ->
        vm.onAttendanceChanged(2, Attendance.INJURED)
        vm.onAttendanceChanged(3, Attendance.ABSENT)
        assertEquals(4, vm.uiState.value.presentCount)
        assertEquals(1, vm.uiState.value.injuredCount)
    }

    @Test fun addingPlayerAddsToSquad() = withViewModel { vm ->
        vm.onPlayerAdded("  Bram Claes ", Position.ATTACK)
        val aanvallers = vm.uiState.value.squad.first { it.position == Position.ATTACK }.players
        assertEquals(listOf("Lars de Wit", "Bram Claes"), aanvallers.map { it.name })
    }

    @Test fun blankNameIsNotAdded() = withViewModel { vm ->
        vm.onPlayerAdded("   ", Position.KEEPER)
        assertEquals(6, vm.uiState.value.players.size)
    }

    @Test fun resetDatabaseEmptiesTeamAndPlayers() = withViewModel { vm ->
        vm.onDatabaseReset()
        assertEquals("", vm.uiState.value.teamName)
        assertTrue(vm.uiState.value.players.isEmpty())
    }

    @Test fun creatingDemoTeamRestoresEverything() = withViewModel { vm ->
        vm.onDatabaseReset()
        vm.onDemoTeamCreated()
        assertEquals("JO17-1", vm.uiState.value.teamName)
        assertEquals(16, vm.uiState.value.players.size)
    }
}
