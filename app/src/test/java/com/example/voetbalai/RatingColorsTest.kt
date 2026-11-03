package com.example.voetbalai

import com.example.voetbalai.domain.stats.RatingColors
import com.example.voetbalai.domain.stats.RatingTier
import org.junit.Assert.assertEquals
import org.junit.Test

class RatingColorsTest {
    @Test fun statTiersFollowTheFiveBands() {
        assertEquals(RatingTier.VERY_LOW, RatingColors.tierForStat(1))
        assertEquals(RatingTier.VERY_LOW, RatingColors.tierForStat(4))
        assertEquals(RatingTier.LOW, RatingColors.tierForStat(5))
        assertEquals(RatingTier.LOW, RatingColors.tierForStat(8))
        assertEquals(RatingTier.AVERAGE, RatingColors.tierForStat(9))
        assertEquals(RatingTier.AVERAGE, RatingColors.tierForStat(12))
        assertEquals(RatingTier.GOOD, RatingColors.tierForStat(13))
        assertEquals(RatingTier.GOOD, RatingColors.tierForStat(16))
        assertEquals(RatingTier.EXCELLENT, RatingColors.tierForStat(17))
        assertEquals(RatingTier.EXCELLENT, RatingColors.tierForStat(20))
    }

    @Test fun everyRatingFrom1To20HasATier() {
        (1..20).forEach { RatingColors.tierForStat(it) }
    }

    @Test fun overallIsConvertedToTheSameScale() {
        assertEquals(RatingTier.VERY_LOW, RatingColors.tierForOverall(1))
        assertEquals(RatingTier.EXCELLENT, RatingColors.tierForOverall(99))
        assertEquals(RatingTier.AVERAGE, RatingColors.tierForOverall(50))
        assertEquals(RatingTier.GOOD, RatingColors.tierForOverall(75))
    }

    @Test fun tiersHaveDifferentColors() {
        assertEquals(5, RatingTier.entries.map { it.argb }.toSet().size)
    }
}
