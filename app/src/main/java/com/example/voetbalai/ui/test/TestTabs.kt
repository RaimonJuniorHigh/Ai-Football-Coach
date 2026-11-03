package com.example.voetbalai.ui.test

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.voetbalai.domain.chat.ChatRole
import com.example.voetbalai.domain.model.Attendance
import com.example.voetbalai.domain.model.Day
import com.example.voetbalai.domain.model.Position
import com.example.voetbalai.domain.rules.TeamRules
import com.example.voetbalai.domain.stats.RatingColors
import com.example.voetbalai.domain.stats.StatType
import com.example.voetbalai.viewmodel.chat.ChatUiState
import com.example.voetbalai.viewmodel.chat.ChatViewModel
import com.example.voetbalai.viewmodel.onboarding.OnboardingUiState
import com.example.voetbalai.viewmodel.onboarding.OnboardingViewModel
import com.example.voetbalai.viewmodel.stats.PlayerStatsUiState
import com.example.voetbalai.viewmodel.stats.PlayerStatsViewModel
import com.example.voetbalai.viewmodel.today.TodayUiState
import com.example.voetbalai.viewmodel.today.TodayViewModel

// UITLEG: Dit zijn de vijf tabbladen van het testscherm. Elk tabblad is een eigen functie:
// TeamTab (team opzetten), PlayersTab (spelers beheren), AttendanceTab (wie is er),
// ChatTab (chat met de AI) en DebugTab (database resetten en ruwe data bekijken).
// Elke knop roept een functie van een ViewModel aan (bijvoorbeeld chat.onMessageSent(...)) en zet
// met report(...) een korte tekst in de statusregel. De knoppen veranderen zelf niets in de lijst:
// de ViewModel past de data aan en het scherm tekent daarna vanzelf opnieuw.

/** `report` zet de statusregel bovenaan het scherm. */
typealias Report = (String) -> Unit

// Dit is een rijtje keuzerondjes (radio buttons) voor een lijst opties. T betekent dat het voor elk type werkt
// (bijvoorbeeld getallen of posities). Het gekozen rondje staat aan.
@Composable
private fun <T> Choice(options: List<T>, chosen: T, label: (T) -> String, onChoose: (T) -> Unit) {
    options.forEach { option ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = option == chosen, onClick = { onChoose(option) })
            Text(label(option))
        }
    }
}

@Composable
fun TeamTab(state: OnboardingUiState, onboarding: OnboardingViewModel, report: Report) {
    Section("Teamnaam") {
        TextField(state.teamName, { onboarding.onTeamNameChanged(it) }, label = { Text("Teamnaam") })
    }
    Section("Trainingsdagen") {
        Day.entries.forEach { day ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = day in state.trainingDays, onCheckedChange = { onboarding.onTrainingDayToggled(day) })
                Text(day.name)
            }
        }
    }
    Section("Sessieduur") {
        Choice(listOf(60, 75, 90), state.sessionMinutes, { "$it minuten" }) {
            onboarding.onSessionMinutesChosen(it)
        }
    }
    Section("Opslaan") {
        Text("Kan opslaan: ${state.canSave}  (${state.players.size} van minimaal ${TeamRules.MIN_PLAYERS} spelers in concept)")
        state.squadProblems.forEach { Text("- $it") }
        Button({
            report("Team opslaan...")
            onboarding.onFinish { report("Team opgeslagen: ${state.teamName}") }
        }, enabled = !state.isSaving) { Text("Team opslaan") }
        Button({
            onboarding.onEditTeam()
            report("Opgeslagen team in concept geladen (tenzij er een fout staat)")
        }) { Text("Opgeslagen team laden in concept") }
        Text("Uitnodigingslink: ${state.inviteLink}")
    }
}

@Composable
fun PlayersTab(
    state: OnboardingUiState,
    todayState: TodayUiState,
    onboarding: OnboardingViewModel,
    today: TodayViewModel,
    report: Report
) {
    var name by rememberSaveable { mutableStateOf("") }
    var position by rememberSaveable { mutableStateOf(Position.MIDFIELD) }

    Section("Nieuwe speler") {
        TextField(name, { name = it }, label = { Text("Naam") })
        Choice(Position.entries, position, { it.name }) { position = it }
        Button({
            if (name.isBlank()) report("Vul een naam in") else {
                onboarding.onPlayerAdded(name, position)
                report("Concept-speler toegevoegd: ${name.trim()} ($position); sla het team op om te bewaren")
                name = ""
            }
        }) { Text("Toevoegen aan concept") }
        Button({
            if (name.isBlank()) report("Vul een naam in") else {
                today.onPlayerAdded(name, position)
                report("Speler direct aan opgeslagen team toegevoegd: ${name.trim()} ($position)")
                name = ""
            }
        }) { Text("Direct toevoegen aan opgeslagen team") }
    }
    Section("Concept-spelers (${state.players.size} van minimaal ${TeamRules.MIN_PLAYERS})") {
        Text(Position.entries.joinToString("  ") { p ->
            "$p ${state.players.count { it.position == p }}/${TeamRules.MIN_PER_POSITION.getValue(p)}"
        })
        state.players.forEach { player ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${player.name} - ${player.position}")
                Button({
                    onboarding.onPlayerRemoved(player.id)
                    report("Concept-speler verwijderd: ${player.name}")
                }) { Text("Verwijderen") }
            }
        }
    }
    Section("Clublijst") {
        state.clubChoices.forEach { choice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${choice.name} - ${choice.position}")
                Button({
                    onboarding.onClubPlayersImported(setOf(choice.name))
                    report("Clubspeler geïmporteerd: ${choice.name}")
                }, enabled = !choice.alreadyAdded) { Text(if (choice.alreadyAdded) "Al toegevoegd" else "Importeren") }
            }
        }
    }
    Section("Opgeslagen spelers (${todayState.players.size})") {
        todayState.players.forEach { Text("${it.name} - ${it.position}") }
    }
}

@Composable
fun AttendanceTab(state: TodayUiState, today: TodayViewModel, report: Report) {
    Section("Samenvatting") {
        Text("${state.presentCount} van ${state.players.size} aanwezig, ${state.injuredCount} geblesseerd")
    }
    Section("Spelers") {
        if (state.players.isEmpty()) Text("Geen spelers")
        state.players.forEach { player ->
            Text("${player.name} (${player.position}): ${player.attendance}")
            Row {
                listOf(
                    Attendance.PRESENT to "Aanwezig",
                    Attendance.ABSENT to "Afwezig",
                    Attendance.INJURED to "Geblesseerd"
                ).forEach { (status, text) ->
                    Button({
                        today.onAttendanceChanged(player.id, status)
                        report("${player.name}: $text")
                    }, enabled = player.attendance != status) { Text(text) }
                }
            }
        }
    }
}

// Dit tabblad toont de spelersstatistieken. Bovenaan de kaartjes van alle spelers (totaalcijfer 1-99, met kleur).
// Klik ik op een speler, dan zie ik zijn profiel: stats van 1 tot 20 met kleur, zijn laatste oefeningen en wedstrijden.
// De kleuren komen uit RatingColors (domain); het scherm tekent alleen. Met de knop onderaan leg ik een test-oefening
// vast, zodat ik zie dat het cijfer verandert. De cijfers rekent de database uit, niet de app.
@Composable
fun StatsTab(state: PlayerStatsUiState, stats: PlayerStatsViewModel, report: Report) {
    Section("Kaartjes (totaalcijfer 1-99)") {
        Button({
            stats.onCardsRequested()
            report("Kaartjes laden...")
        }) { Text("Kaartjes laden") }
        if (state.isLoading) Text("Bezig...")
        if (state.cards.isEmpty()) Text("Nog geen kaartjes. Maak eerst een demo-team (tabblad Debug) en druk op Kaartjes laden.")
        state.cards.forEach { card ->
            val color = card.overall?.let { Color(RatingColors.tierForOverall(it).argb) } ?: Color.Gray
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${card.overall ?: "-"}", color = color)
                Button({ stats.onPlayerSelected(card.playerId) }) { Text("${card.name} (${card.position})") }
            }
        }
    }
    state.selected?.let { profile ->
        val card = profile.card
        Section("Profiel: ${card.name}") {
            val overallColor = card.overall?.let { Color(RatingColors.tierForOverall(it).argb) } ?: Color.Gray
            Text("${card.position}  Totaalcijfer: ${card.overall ?: "-"} (van 99)", color = overallColor)
            if (profile.stats.isEmpty()) Text("Nog geen stats: er zijn geen oefeningen vastgelegd.")
            profile.stats.forEach { stat ->
                val tier = RatingColors.tierForStat(stat.rating)
                Text("${stat.stat.label}: ${stat.rating} (van 20) - ${tier.label}", color = Color(tier.argb))
            }
            Button({
                // Een test-oefening: een keeper krijgt een keepen-oefening, iedereen anders een pass-oefening.
                val type = if (card.position == Position.KEEPER) StatType.GOALKEEPING else StatType.PASSING
                stats.onDrillRecorded(card.playerId, type, attempts = 20, successes = 18)
                report("Test-oefening vastgelegd: ${type.label} 18 van 20")
            }) { Text("Test-oefening toevoegen (18 van 20)") }
            Button({ stats.onSelectionCleared() }) { Text("Profiel sluiten") }
        }
        Section("Laatste oefeningen") {
            if (profile.drills.isEmpty()) Text("Geen oefeningen")
            profile.drills.forEach { Text("${it.recordedOn}: ${it.stat.label}, ${it.successes} van ${it.attempts}") }
        }
        Section("Wedstrijden") {
            if (profile.matches.isEmpty()) Text("Geen wedstrijden")
            profile.matches.forEach {
                Text(
                    "${it.playedOn}: ${if (it.home) "thuis" else "uit"} tegen ${it.opponent} (${it.goalsFor}-${it.goalsAgainst}), " +
                        "${it.minutes} min, ${it.goals} goals, ${it.assists} assists"
                )
            }
        }
    }
}

// Dit tabblad is de chat. Ik typ een bericht in het tekstveld en druk op Verstuur; de ViewModel stuurt het
// naar de API. Het antwoord komt onder het gesprek te staan. De tekst die ik net aan het typen ben, bewaar ik
// in het scherm zelf (rememberSaveable), want die hoort niet in de ViewModel zolang hij niet verstuurd is.
@Composable
fun ChatTab(state: ChatUiState, chat: ChatViewModel, report: Report) {
    var input by rememberSaveable { mutableStateOf("") }

    Section("Gesprek") {
        if (state.messages.isEmpty()) Text("Nog geen berichten. Typ hieronder een vraag.")
        state.messages.forEach { message ->
            Text((if (message.role == ChatRole.USER) "Jij: " else "AI: ") + message.text)
        }
        if (state.isBusy) Text("AI is aan het typen...")
    }
    Section("Nieuw bericht") {
        TextField(input, { input = it }, label = { Text("Typ je bericht") }, modifier = Modifier.fillMaxWidth())
        Button({
            chat.onMessageSent(input)
            report("Bericht verstuurd")
            input = ""
        }, enabled = state.canSend && input.isNotBlank()) { Text("Verstuur") }
        Button({
            chat.onChatCleared()
            report("Chat leeggemaakt")
        }) { Text("Chat leegmaken") }
    }
}

@Composable
fun DebugTab(onboardingState: OnboardingUiState, todayState: TodayUiState, today: TodayViewModel, report: Report) {
    Section("Database") {
        Button({
            today.onDatabaseReset()
            report("Database gereset")
        }) { Text("Database resetten") }
        Button({
            today.onDemoTeamCreated()
            report("Demo-team gemaakt")
        }) { Text("Demo-team maken (uit de database)") }
    }
    Section("Foutmeldingen") {
        Text("Today: ${todayState.errorMessage ?: "-"}")
        Text("Onboarding: ${onboardingState.errorMessage ?: "-"}")
    }
    Section("Ruwe UiState: Today") { Text(todayState.toString()) }
    Section("Ruwe UiState: Onboarding") { Text(onboardingState.toString()) }
}
