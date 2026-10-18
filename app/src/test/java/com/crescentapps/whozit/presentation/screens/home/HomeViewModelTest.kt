package com.crescentapps.whozit.presentation.screens.home

import com.crescentapps.whozit.core.model.OccurrenceStatus
import com.crescentapps.whozit.core.model.Participant
import com.crescentapps.whozit.core.model.ResolvedTurn
import com.crescentapps.whozit.core.model.Schedule
import com.crescentapps.whozit.core.model.ScheduleType
import org.junit.Assert.*
import org.junit.Test

class HomeViewModelTest {

    private fun createSampleTurn(id: Long, name: String, isShared: Boolean, status: OccurrenceStatus): ResolvedTurn {
        val schedule = Schedule(id = id, name = name, type = ScheduleType.TASK, startDate = "2026-10-18")
        val participant = Participant(id = 100L + id, name = "Participant $id")
        return ResolvedTurn(
            schedule = schedule,
            participant = participant,
            date = "2026-10-18",
            status = status,
            isShared = isShared
        )
    }

    @Test
    fun testHomeFilter_All_ReturnsAllTurns() {
        val turns = listOf(
            createSampleTurn(1L, "Turn 1", isShared = false, status = OccurrenceStatus.PENDING),
            createSampleTurn(2L, "Turn 2", isShared = true, status = OccurrenceStatus.COMPLETED)
        )

        val state = HomeUiState(
            todayTurns = turns,
            filteredTurns = turns,
            currentFilter = HomeFilter.ALL,
            totalPending = 1,
            totalCompleted = 1,
            isLoading = false
        )

        assertEquals(2, state.filteredTurns.size)
        assertEquals(1, state.totalPending)
        assertEquals(1, state.totalCompleted)
    }

    @Test
    fun testHomeFilter_Local_FiltersOutSharedTurns() {
        val turns = listOf(
            createSampleTurn(1L, "Local Turn", isShared = false, status = OccurrenceStatus.PENDING),
            createSampleTurn(2L, "Shared Turn", isShared = true, status = OccurrenceStatus.PENDING)
        )

        val localOnly = turns.filter { !it.isShared }
        val state = HomeUiState(
            todayTurns = turns,
            filteredTurns = localOnly,
            currentFilter = HomeFilter.LOCAL
        )

        assertEquals(1, state.filteredTurns.size)
        assertEquals("Local Turn", state.filteredTurns.first().schedule.name)
        assertFalse(state.filteredTurns.first().isShared)
    }

    @Test
    fun testHomeFilter_Shared_FiltersOutLocalTurns() {
        val turns = listOf(
            createSampleTurn(1L, "Local Turn", isShared = false, status = OccurrenceStatus.PENDING),
            createSampleTurn(2L, "Shared Turn", isShared = true, status = OccurrenceStatus.PENDING)
        )

        val sharedOnly = turns.filter { it.isShared }
        val state = HomeUiState(
            todayTurns = turns,
            filteredTurns = sharedOnly,
            currentFilter = HomeFilter.SHARED
        )

        assertEquals(1, state.filteredTurns.size)
        assertEquals("Shared Turn", state.filteredTurns.first().schedule.name)
        assertTrue(state.filteredTurns.first().isShared)
    }

    @Test
    fun testTurnCounts_Calculation() {
        val turns = listOf(
            createSampleTurn(1L, "Pending 1", isShared = false, status = OccurrenceStatus.PENDING),
            createSampleTurn(2L, "Pending 2", isShared = false, status = OccurrenceStatus.PENDING),
            createSampleTurn(3L, "Completed 1", isShared = false, status = OccurrenceStatus.COMPLETED)
        )

        val pendingCount = turns.count { it.status == OccurrenceStatus.PENDING }
        val completedCount = turns.count { it.status == OccurrenceStatus.COMPLETED }

        assertEquals(2, pendingCount)
        assertEquals(1, completedCount)
    }
}
