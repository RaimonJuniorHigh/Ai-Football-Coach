package com.example.voetbalai

import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.viewmodel.onboarding.OnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val repository = FakeTeamRepository()

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = OnboardingViewModel(repository)

    @Test fun initialStateComesFromRepository() {
        val state = viewModel().uiState.value
        assertEquals("JO17-1", state.teamName)
        assertEquals(setOf(Day.TUE, Day.THU), state.trainingDays)
        assertEquals(6, state.players.size)
        assertTrue(state.inviteLink.startsWith("voetbalai.app/join/"))
    }

    @Test fun step1CannotContinueWithoutNameOrDays() {
        val vm = viewModel()
        vm.onTeamNameChanged("  ")
        assertFalse(vm.uiState.value.canContinueStep1)
        vm.onTeamNameChanged("JO15-2")
        vm.onTrainingDayToggled(Day.TUE)
        vm.onTrainingDayToggled(Day.THU)
        assertTrue(vm.uiState.value.trainingDays.isEmpty())
        assertFalse(vm.uiState.value.canContinueStep1)
        vm.onTrainingDayToggled(Day.WED)
        assertTrue(vm.uiState.value.canContinueStep1)
    }

    @Test fun addAndRemovePlayer() {
        val vm = viewModel()
        vm.onPlayerAdded("Bram Claes", Position.ATTACK)
        val added = vm.uiState.value.players.last()
        assertEquals("Bram Claes", added.name)
        vm.onPlayerRemoved(added.id)
        assertEquals(6, vm.uiState.value.players.size)
    }

    @Test fun two_new_players_have_different_ids() {
        val vm = viewModel()
        vm.onPlayerAdded("A", Position.KEEPER)
        vm.onPlayerAdded("B", Position.KEEPER)
        val ids = vm.uiState.value.players.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun clubImportSkipsExistingPlayers() {
        val vm = viewModel()
        // Noah Peeters staat al in de selectie.
        assertTrue(vm.uiState.value.clubChoices.first { it.name == "Noah Peeters" }.alreadyAdded)
        vm.onClubPlayersImported(setOf("Finn Smeets", "Noah Peeters"))
        val names = vm.uiState.value.players.map { it.name }
        assertEquals(7, names.size)
        assertEquals(1, names.count { it == "Noah Peeters" })
        assertTrue("Finn Smeets" in names)
    }

    @Test fun finishSavesAndCallsCallback() {
        val repository = FakeTeamRepository(players = fullSquad)
        val vm = OnboardingViewModel(repository)
        var done = false
        vm.onTeamNameChanged("JO19-1")
        vm.onSessionMinutesChosen(75)
        vm.onFinish { done = true }
        assertTrue(done)
        assertTrue(repository.onboarded)
        assertEquals("JO19-1", repository.savedTeam?.name)
        assertEquals(75, repository.savedTeam?.sessionMinutes)
        assertEquals(16, repository.savedPlayers.size)
    }

    @Test fun editTeamWithoutTeamGivesErrorAndDoesNotBlock() = runTest {
        val vm = viewModel()
        repository.resetDatabase()
        vm.onEditTeam()
        assertTrue(vm.uiState.value.errorMessage!!.contains("Geen opgeslagen team"))
        assertEquals(6, vm.uiState.value.players.size) // concept blijft ongemoeid
    }

    @Test fun editTeamLoadsItAndClearsError() = runTest {
        val vm = viewModel()
        repository.resetDatabase()
        vm.onEditTeam()
        repository.createDemoTeam()
        vm.onEditTeam()
        assertEquals(null, vm.uiState.value.errorMessage)
        assertEquals(16, vm.uiState.value.players.size)
    }

    @Test fun finishRejectsTooFewPlayers() {
        val vm = viewModel() // 6 spelers
        var done = false
        vm.onFinish { done = true }
        assertFalse(done)
        assertFalse(vm.uiState.value.canSave)
        assertTrue(vm.uiState.value.errorMessage!!.contains("Minimaal 13 spelers"))
        assertEquals(null, repository.savedTeam)
    }

    @Test fun finishRejectsSquadWithoutKeeper() {
        val withoutKeeper = fullSquad.filterNot { it.position == Position.KEEPER } +
            listOf(Player(90, "Extra 1", Position.DEFENCE), Player(91, "Extra 2", Position.DEFENCE))
        val repo = FakeTeamRepository(players = withoutKeeper)
        val vm = OnboardingViewModel(repo)
        var done = false
        vm.onFinish { done = true }
        assertFalse(done)
        assertTrue(vm.uiState.value.errorMessage!!.contains("keeper"))
    }

    @Test fun canSaveWhenSquadIsValid() {
        val vm = OnboardingViewModel(FakeTeamRepository(players = fullSquad))
        assertTrue(vm.uiState.value.canSave)
        assertTrue(vm.uiState.value.squadProblems.isEmpty())
    }
}
