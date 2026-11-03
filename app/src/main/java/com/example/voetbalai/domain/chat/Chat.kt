package com.example.voetbalai.domain.chat

import com.example.voetbalai.domain.model.Player
import kotlinx.coroutines.delay

// UITLEG: Alles rond de chat met de AI. Bovenaan staan de gegevens (wat stuur ik, wat komt terug),
// daaronder de interface ChatApi (de belofte) en FakeChatApi (een tijdelijk nepantwoord).
// De echte API is er nog niet. Straks maak ik een nieuwe klasse die ChatApi uitvoert, en wissel ik die
// om in AppContainer.kt. De rest van de app hoeft dan niet te veranderen.

// Wie schrijft het bericht: de trainer (USER) of de AI (ASSISTANT).
enum class ChatRole { USER, ASSISTANT }

// Eén bericht in het gesprek.
data class ChatMessage(val role: ChatRole, val text: String)

// Dit stuur ik naar de API: het hele gesprek tot nu toe, plus gegevens van het team,
// zodat de AI weet over welk team het gaat en wie er aanwezig is.
data class ChatRequest(
    val teamName: String,
    val sessionMinutes: Int,
    val players: List<Player>,
    val messages: List<ChatMessage>
)

/**
 * Koppelpunt voor de chat. Vervang [FakeChatApi] door een klasse die de echte API aanroept;
 * de ViewModel en het scherm hoeven dan niet te wijzigen.
 */
interface ChatApi {
    // Geeft de tekst van het antwoord terug. Een fout (bijvoorbeeld geen internet) mag als Exception omhoog komen.
    suspend fun reply(request: ChatRequest): String
}

/** Tijdelijk antwoord zolang de echte API er niet is. */
// Dit is GEEN echte AI. Hij wacht even en zegt dat de API nog niet gekoppeld is.
class FakeChatApi(private val latencyMs: Long = 1_000) : ChatApi {
    override suspend fun reply(request: ChatRequest): String {
        // Even wachten, zodat je het "bezig"-moment in het scherm kunt zien.
        delay(latencyMs)
        val lastMessage = request.messages.lastOrNull { it.role == ChatRole.USER }?.text.orEmpty()
        return "De chat-API is nog niet gekoppeld. Je vroeg: \"$lastMessage\""
    }
}
