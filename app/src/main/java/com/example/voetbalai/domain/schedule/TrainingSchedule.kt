package com.example.voetbalai.domain.schedule

import com.example.voetbalai.domain.model.Day
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// UITLEG: Dit zijn kleine hulpfuncties rond tijd: wanneer is de volgende training en welke begroeting past
// bij het tijdstip. De ViewModel gebruikt ze om "Dinsdag 27 oktober, over 2 dagen" te maken.

// Nederlandse taal, zodat dagen en maanden in het Nederlands staan.
private val Dutch = Locale.forLanguageTag("nl-NL")

// Dit is de datum van de volgende training en over hoeveel dagen dat is.
data class NextTraining(val date: Calendar, val daysAhead: Int)

/** Eerstvolgende trainingsdag vanaf [from] (vandaag telt mee). */
fun nextTraining(days: Collection<Day>, from: Calendar): NextTraining? {
    // Zonder trainingsdagen is er geen volgende training.
    if (days.isEmpty()) return null
    val wanted = days.map { it.calendarDay }.toSet()
    // Ik loop vandaag + de 6 dagen erna af en pak de eerste dag die een trainingsdag is.
    for (ahead in 0..6) {
        val date = (from.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, ahead) }
        if (date.get(Calendar.DAY_OF_WEEK) in wanted) return NextTraining(date, ahead)
    }
    return null
}

// 0 is vandaag, 1 is morgen, anders "over N dagen".
fun daysAheadLabel(daysAhead: Int) = when (daysAhead) {
    0 -> "vandaag"
    1 -> "morgen"
    else -> "over $daysAhead dagen"
}

// Hier maak ik van een datum tekst zoals "Dinsdag 27 oktober" (met een hoofdletter vooraan).
fun formatTrainingDate(date: Calendar): String =
    SimpleDateFormat("EEEE d MMMM", Dutch).format(date.time).replaceFirstChar { it.uppercase() }

// Begroeting op basis van het uur: ochtend 6-11, middag 12-17, anders avond.
fun greeting(hour: Int) = when (hour) {
    in 6..11 -> "Goedemorgen"
    in 12..17 -> "Goedemiddag"
    else -> "Goedenavond"
}
