package com.example.voetbalai.data.repository

import com.example.voetbalai.data.prefs.OnboardingStore
import com.example.voetbalai.data.remote.TeamRemoteDataSource
import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.ClubPlayer
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team
import com.example.voetbalai.domain.model.TeamWithPlayers
import com.example.voetbalai.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// UITLEG: Dit is de echte uitvoering van de beloftes uit TeamRepository. Het is de brug tussen de app
// en de online database: elke functie vraagt iets aan de database (via TeamRemoteDataSource) en haalt daarna
// team en spelers opnieuw op. Die verse waarden zet ik in twee MutableStateFlow's (_team en _players),
// en daardoor ververst het scherm vanzelf. De ViewModels zien nooit iets van Supabase.
// Bij elke wijziging haal ik alles opnieuw op in plaats van zelf bij te houden wat er veranderd is.
// Dat is een paar extra aanvragen, maar de data is dan altijd hetzelfde als in de database.
class SupabaseTeamRepository(
    private val remote: TeamRemoteDataSource,
    private val onboarding: OnboardingStore
) : TeamRepository {

    private val _team = MutableStateFlow<Team?>(null)
    private val _players = MutableStateFlow<List<Player>>(emptyList())

    override val team: Flow<Team?> = _team.asStateFlow()
    override val players: Flow<List<Player>> = _players.asStateFlow()

    // Haalt team en spelers opnieuw op en zet ze in de twee stromen. Is er geen team, dan een lege lijst.
    override suspend fun refresh() {
        val team = remote.fetchTeam()
        _players.value = if (team == null) emptyList() else remote.fetchPlayers(team.id)
        _team.value = team
    }

    override suspend fun currentTeam(): TeamWithPlayers? {
        refresh()
        val team = _team.value ?: return null
        return TeamWithPlayers(team, _players.value)
    }

    override suspend fun saveTeam(team: Team, players: List<Player>) {
        remote.saveTeam(team, players)
        refresh()
    }

    override suspend fun setAttendance(playerId: Long, attendance: Attendance) {
        remote.updateAttendance(playerId, attendance)
        refresh()
    }

    // Een nieuwe speler komt in het huidige team. Is er geen team, dan gebeurt er niets.
    // trim() haalt spaties voor en achter de naam weg.
    override suspend fun addPlayer(name: String, position: Position) {
        val team = _team.value ?: remote.fetchTeam() ?: return
        remote.insertPlayer(team.id, name.trim(), position)
        refresh()
    }

    override suspend fun resetDatabase() {
        remote.deleteAllTeams()
        onboarding.setOnboarded(false)
        refresh()
    }

    override suspend fun createDemoTeam() {
        remote.createDemoTeam()
        refresh()
    }

    override suspend fun clubList(): List<ClubPlayer> = remote.fetchClubPlayers()

    override fun isOnboarded() = onboarding.isOnboarded()

    override fun markOnboarded() = onboarding.setOnboarded(true)
}
