package com.example.voetbalai

import com.example.voetbalai.data.repository.SupabaseStatsRepository
import com.example.voetbalai.domain.stats.StatType
import com.example.voetbalai.viewmodel.stats.PlayerStatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerStatsViewModelTest {
    private val remote = FakeStatsRemote()
    private fun viewModel() = PlayerStatsViewModel(SupabaseStatsRepository(remote))

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun cardsRequestedFillsTheOverview() = runTest {
        val vm = viewModel()
        vm.onCardsRequested()
        assertEquals(4, vm.uiState.value.cards.size)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun selectingAPlayerShowsTheProfile() = runTest {
        val vm = viewModel()
        vm.onPlayerSelected(1)
        assertEquals("Lars de Wit", vm.uiState.value.selected?.card?.name)
        assertEquals(2, vm.uiState.value.selected?.stats?.size)
    }

    @Test fun selectingUnknownPlayerGivesAnError() = runTest {
        val vm = viewModel()
        vm.onPlayerSelected(999)
        assertNull(vm.uiState.value.selected)
        assertTrue(vm.uiState.value.errorMessage!!.contains("niet gevonden"))
    }

    @Test fun clearingTheSelectionClosesTheProfile() = runTest {
        val vm = viewModel()
        vm.onPlayerSelected(1)
        vm.onSelectionCleared()
        assertNull(vm.uiState.value.selected)
    }

    @Test fun recordingADrillRefreshesTheProfile() = runTest {
        val vm = viewModel()
        vm.onPlayerSelected(1)
        vm.onDrillRecorded(1, StatType.FINISHING, 100, 100)
        assertEquals(1, vm.uiState.value.selected?.drills?.size)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test fun invalidDrillGivesAnErrorMessage() = runTest {
        val vm = viewModel()
        vm.onDrillRecorded(1, StatType.FINISHING, 10, 20)
        assertTrue(vm.uiState.value.errorMessage!!.contains("mislukt"))
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun databaseErrorShowsInTheState() = runTest {
        remote.failWith = IllegalStateException("geen internet")
        val vm = viewModel()
        vm.onCardsRequested()
        assertTrue(vm.uiState.value.errorMessage!!.contains("geen internet"))
    }
}
