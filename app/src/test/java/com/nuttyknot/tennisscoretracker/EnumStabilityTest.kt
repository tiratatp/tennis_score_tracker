package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnumStabilityTest {
    @Test
    fun `MatchFormat names match DataStore persisted values`() {
        assertEquals("STANDARD", MatchFormat.STANDARD.name)
        assertEquals("LEAGUE", MatchFormat.LEAGUE.name)
        assertEquals("FAST", MatchFormat.FAST.name)
    }

    @Test
    fun `AppTheme names match DataStore persisted values`() {
        assertEquals("GRAND_SLAM", AppTheme.GRAND_SLAM.name)
        assertEquals("MIAMI_NIGHT", AppTheme.MIAMI_NIGHT.name)
        assertEquals("COLORBLIND_SAFE", AppTheme.COLORBLIND_SAFE.name)
        assertEquals("SKY_BLUE", AppTheme.SKY_BLUE.name)
    }

    @Test
    fun `AppTheme aliasNames match manifest activity-alias names`() {
        assertEquals(".MainActivityGrandSlam", AppTheme.GRAND_SLAM.aliasName)
        assertEquals(".MainActivityMiamiNight", AppTheme.MIAMI_NIGHT.aliasName)
        assertEquals(".MainActivityColorblindSafe", AppTheme.COLORBLIND_SAFE.aliasName)
        assertEquals(".MainActivitySkyBlue", AppTheme.SKY_BLUE.aliasName)
    }

    @Test
    fun `MatchFormat round-trips through DataStore name lookup`() {
        for (format in MatchFormat.entries) {
            val name = format.name
            val restored = MatchFormat.entries.find { it.name == name }
            assertEquals(format, restored)
        }
    }

    @Test
    fun `AppTheme round-trips through DataStore name lookup`() {
        for (theme in AppTheme.entries) {
            val name = theme.name
            val restored = AppTheme.entries.find { it.name == name }
            assertEquals(theme, restored)
        }
    }

    @Test
    fun `unknown MatchFormat name returns null for graceful fallback`() {
        val result = MatchFormat.entries.find { it.name == "DELETED_VALUE" }
        assertNull(result)
    }

    @Test
    fun `unknown AppTheme name returns null for graceful fallback`() {
        val result = AppTheme.entries.find { it.name == "DELETED_VALUE" }
        assertNull(result)
    }
}
