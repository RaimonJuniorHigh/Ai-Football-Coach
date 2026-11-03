package com.example.voetbalai.domain.repository

import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.ClubPlayer
import com.example.voetbalai.domain.model.Player
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.model.Team
import com.example.voetbalai.domain.model.TeamWithPlayers
import kotlinx.coroutines.flow.Flow

// UITLEG: Dit is een interface: alleen een lijst met beloftes, zonder echte code.
// Ik zeg hier dus alleen WAT de repository kan, niet HOE. Het "hoe" staat in
// data/repository/SupabaseTeamRepository.kt (met de Supabase-database). In de tests gebruik ik een nepversie.
// Daardoor weet een ViewModel niet of er een echte database of een nepje achter zit.
//
// Twee woorden die je hier vaak ziet:
// - Flow: een stroom waarden. Verandert het team of de spelerslijst, dan komt er vanzelf een nieuwe waarde door.
// - suspend: de functie mag even duren (bijvoorbeeld een aanvraag naar de database) zonder dat de app vastloopt.

/**
 * Enige toegangspoort tot team- en spelersgegevens. ViewModels kennen alleen deze interface;
 * de Supabase-implementatie staat in `data/`.
 */
interface TeamRepository {
    /** Het huidige team, of null zolang er nog geen team is. */
    val team: Flow<Team?>

    /** Spelers van het huidige team; wordt bijgewerkt na elke wijziging. */
    val players: Flow<List<Player>>

    /** Haalt team en spelers opnieuw op uit de database. Roep dit aan als het scherm opent. */
    suspend fun refresh()

    /** Het huidige team met spelers (vers uit de database), of null als er geen team is. */
    suspend fun currentTeam(): TeamWithPlayers?

    /** Vervangt het bestaande team en zijn spelers door dit team. Aanwezigheid blijft behouden. */
    suspend fun saveTeam(team: Team, players: List<Player>)

    // Hiermee zet ik een speler op aanwezig, afwezig of geblesseerd.
    suspend fun setAttendance(playerId: Long, attendance: Attendance)

    // Hiermee voeg ik een nieuwe speler toe aan het opgeslagen team.
    suspend fun addPlayer(name: String, position: Position)

    /** Verwijdert alle teams en spelers van deze trainer en zet de onboarding terug op "niet afgerond". */
    suspend fun resetDatabase()

    /** Laat de database een demo-team maken (JO17-1 met 16 verzonnen spelers). De data staat dus in de database, niet in de app. */
    suspend fun createDemoTeam()

    /** Fictieve clublijst voor "Importeer uit clublijst", uit de database. */
    suspend fun clubList(): List<ClubPlayer>

    // Onboarding is het eerste keer het team opzetten. Deze twee functies onthouden of dat klaar is.
    fun isOnboarded(): Boolean

    fun markOnboarded()
}
