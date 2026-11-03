package com.example.voetbalai

import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.rules.TeamRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamRulesTest {
    @Test fun fullSquadIsValid() {
        assertTrue(TeamRules.isValid(fullSquad))
    }

    @Test fun twelvePlayersIsTooFew() {
        val twelve = fullSquad.take(12)
        assertFalse(TeamRules.isValid(twelve))
        assertTrue(TeamRules.problems(twelve).any { it.contains("13") })
    }

    @Test fun thirteenPlayersWithKeeperIsValid() {
        val thirteen = listOf(Position.KEEPER to 1, Position.DEFENCE to 4, Position.MIDFIELD to 4, Position.ATTACK to 4)
            .flatMap { (pos, n) -> (1..n).map { Player(0, "$pos $it", pos) } }
        assertEquals(13, thirteen.size)
        assertTrue(TeamRules.isValid(thirteen))
    }

    @Test fun withoutKeeperIsInvalidEvenWithEnoughPlayers() {
        val fieldPlayers = fullSquad.filterNot { it.position == Position.KEEPER } +
            listOf(Player(0, "A", Position.DEFENCE), Player(0, "B", Position.MIDFIELD))
        assertTrue(fieldPlayers.size >= 13)
        assertEquals(1, TeamRules.problems(fieldPlayers).size)
        assertTrue(TeamRules.problems(fieldPlayers).single().contains("keeper"))
    }

    @Test fun emptySquadReportsEverythingMissing() {
        assertEquals(1 + TeamRules.MIN_PER_POSITION.size, TeamRules.problems(emptyList()).size)
    }

    @Test fun onlyKeepersIsInvalid() {
        val keepers = (1..13).map { Player(0, "K$it", Position.KEEPER) }
        assertFalse(TeamRules.isValid(keepers))
    }
}
