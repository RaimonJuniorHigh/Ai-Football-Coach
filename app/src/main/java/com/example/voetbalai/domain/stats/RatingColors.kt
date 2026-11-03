package com.example.voetbalai.domain.stats

// UITLEG: De kleuren voor de cijfers, zodat je in één oogopslag ziet wie sterk of zwak is (net als in een voetbalmanager).
// Dit is alleen de LOGICA: welk cijfer hoort bij welke kleur. Het scherm gebruikt die kleur later om te tekenen.
// De kleuren staan als ARGB-getal (bijvoorbeeld 0xFF2E7D32), zodat dit bestand niets van Android hoeft te weten.

/** Vijf niveaus, van laag naar hoog. */
enum class RatingTier(val argb: Long, val label: String) {
    VERY_LOW(0xFFE53935, "Zwak"),
    LOW(0xFFFB8C00, "Onder gemiddeld"),
    AVERAGE(0xFFFDD835, "Gemiddeld"),
    GOOD(0xFF7CB342, "Goed"),
    EXCELLENT(0xFF2E7D32, "Uitstekend")
}

object RatingColors {
    /** Stat van 1 tot 20: 1-4 rood, 5-8 oranje, 9-12 geel, 13-16 lichtgroen, 17-20 donkergroen. */
    fun tierForStat(rating: Int): RatingTier = when {
        rating <= 4 -> RatingTier.VERY_LOW
        rating <= 8 -> RatingTier.LOW
        rating <= 12 -> RatingTier.AVERAGE
        rating <= 16 -> RatingTier.GOOD
        else -> RatingTier.EXCELLENT
    }

    /** Totaalcijfer van 1 tot 99: ik reken het om naar de schaal van 1 tot 20 en gebruik dezelfde kleuren. */
    fun tierForOverall(overall: Int): RatingTier = tierForStat(Math.ceil(overall * 20.0 / 99).toInt())
}
