package com.example.voetbalai.data.remote

import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.ClubPlayer
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team

// UITLEG: Dit is de lijst met dingen die de app aan de online database kan vragen. Het is een interface,
// dus alleen de beloftes. De echte uitvoering (met Supabase) staat in SupabaseRemote.kt. In de tests gebruik ik
// een nepversie die alles in het geheugen houdt, zodat ik de repository kan testen zonder internet.
interface TeamRemoteDataSource {
    /** Het team van de ingelogde trainer, of null als er nog geen is. */
    suspend fun fetchTeam(): Team?

    suspend fun fetchPlayers(teamId: Long): List<Player>

    /** Vervangt het team van de trainer door dit team en deze spelers, in één keer (alles of niets). */
    suspend fun saveTeam(team: Team, players: List<Player>)

    suspend fun updateAttendance(playerId: Long, attendance: Attendance)

    suspend fun insertPlayer(teamId: Long, name: String, position: Position)

    /** Verwijdert alle teams (en daarmee de spelers) van de ingelogde trainer. */
    suspend fun deleteAllTeams()

    /** Laat de database het demo-team maken. */
    suspend fun createDemoTeam()

    suspend fun fetchClubPlayers(): List<ClubPlayer>
}
