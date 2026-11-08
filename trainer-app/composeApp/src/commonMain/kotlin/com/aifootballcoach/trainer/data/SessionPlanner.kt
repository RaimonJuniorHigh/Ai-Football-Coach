package com.aifootballcoach.trainer.data

/**
 * Maakt de blokken van een concepttraining: een warming-up, één oefening per gekozen thema,
 * een partij en een cooling-down. Er zit nog geen assistent achter; de oefeningen komen uit
 * een vaste lijst per stat. Een eigen thema van de trainer krijgt een partijvorm met dat thema.
 */
object SessionPlanner {
    private const val WarmUpMinutes = 12
    private const val CoolDownMinutes = 10
    private const val GameMinutes = 18
    private const val MinDrillMinutes = 10

    /** Hoogstens drie thema's, anders worden de oefeningen te kort. */
    private const val MaxThemes = 3

    private data class Drill(val name: String, val summary: String, val howItWorks: String)

    private val drills = mapOf(
        StatType.FINISHING to Drill(
            "Afwerken na een kaats",
            "3 tegen 2 op doel",
            "Speel de bal in op de spits, die kaatst terug. Binnen zes seconden afronden, daarna wisselen.",
        ),
        StatType.PASSING to Drill(
            "Rondo 5 tegen 2",
            "Drie vakken, twee keer raken",
            "Vijf spelers houden de bal rond, twee jagen. Wie de bal verliest, gaat erin.",
        ),
        StatType.DRIBBLING to Drill(
            "1 tegen 1 op pionnendoeltjes",
            "Aanvaller tegen verdediger, twee doeltjes",
            "De aanvaller probeert door een van de twee pionnendoeltjes te dribbelen. Na elke poging wisselen.",
        ),
        StatType.DEFENDING to Drill(
            "1 tegen 1 verdedigen in een strook",
            "Smalle strook van 10 meter breed",
            "De aanvaller start met de bal, de verdediger stuurt naar buiten. Bal veroveren of de lijn houden is een punt.",
        ),
        StatType.PACE to Drill(
            "Sprintduels naar de bal",
            "Twee spelers, één bal",
            "De trainer speelt de bal weg, twee spelers sprinten erop af. Wie hem eerst heeft, mag op doel.",
        ),
        StatType.STAMINA to Drill(
            "Intervalpartij 4 tegen 4",
            "Vier rondes van drie minuten",
            "Drie minuten volle bak spelen, één minuut rust. Vier rondes, daarna drinken.",
        ),
        StatType.STRENGTH to Drill(
            "Duels in de cirkel",
            "Twee spelers, één bal, kleine cirkel",
            "Wie de bal tien seconden in de cirkel houdt, scoort. Schouder tegen schouder, geen duwen met de handen.",
        ),
        StatType.GOALKEEPING to Drill(
            "Schoten vanaf de zestien",
            "Keepers om de beurt, spelers schieten",
            "Spelers schieten om de beurt vanaf de rand van de zestien. De keeper vangt of stompt weg en zet snel uit.",
        ),
    )

    /**
     * @param available hoeveel spelers er komen.
     * @param reasons per thema waarom het gekozen is, zoals bij de voorstellen in stap 2.
     */
    fun blocksFor(brief: SessionBrief, available: Int, reasons: Map<String, String>): List<SessionBlock> {
        val themes = brief.focus.take(MaxThemes)
        val drillMinutes = if (themes.isEmpty()) 0 else
            ((brief.session.minutes - WarmUpMinutes - GameMinutes - CoolDownMinutes) / themes.size).coerceAtLeast(MinDrillMinutes)

        val warmUp = SessionBlock(
            id = "warm-up",
            drill = "Warming-up met passen",
            summary = "Losmaken en passen in viertallen",
            minutes = WarmUpMinutes,
            intensity = Intensity.Light,
            space = brief.space,
            players = available,
            howItWorks = "Rustig inlopen, daarna passen in viertallen met twee keer raken. Langzaam opbouwen.",
            why = listOf("Elke training begint rustig"),
        )

        val themeBlocks = themes.mapIndexed { index, theme ->
            val drill = StatType.entries.firstOrNull { it.label == theme }?.let { drills[it] }
                ?: Drill("Partijvorm: $theme", "Klein veld, thema van de trainer", "Speel een partij waarin je let op: $theme. Stil leggen en uitleggen mag.")
            SessionBlock(
                id = "theme-$index",
                drill = drill.name,
                summary = drill.summary,
                minutes = drillMinutes,
                intensity = brief.intensity,
                space = brief.space,
                players = available,
                howItWorks = drill.howItWorks,
                why = listOfNotNull(reasons[theme] ?: "Thema dat je zelf koos", brief.notes.trim().takeIf { it.isNotEmpty() }),
            )
        }

        val game = SessionBlock(
            id = "game",
            drill = "Vrije partij",
            summary = if (brief.space == PitchSpace.Half) "Partij op een half veld" else "Partij op een heel veld",
            minutes = GameMinutes,
            intensity = brief.intensity,
            space = brief.space,
            players = available,
            howItWorks = "Vrij spelen. De trainer zegt de eerste minuten niks en kijkt of de thema's terugkomen.",
            why = listOf("Laat zien of de thema's in een wedstrijd terugkomen"),
        )

        val coolDown = SessionBlock(
            id = "cool-down",
            drill = "Cooling-down en drie vragen",
            summary = "Uitlopen en kort nabespreken",
            minutes = CoolDownMinutes,
            intensity = Intensity.Light,
            space = brief.space,
            players = available,
            howItWorks = "Rustig uitlopen. Daarna drie vragen aan de groep: wat ging goed, wat was lastig, wat nemen we mee?",
            why = listOf("Elke training eindigt rustig"),
        )

        return listOf(warmUp) + themeBlocks + game + coolDown
    }
}
