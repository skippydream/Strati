package com.skippydream.strati.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Il mazzo e' stato utente su disco: se il formato si rompe, si perde la partita. */
class DeckStateTest {

    @Test
    fun `encode and decode round trip`() {
        val state = DeckState(seed = -8_123_456_789L, drawn = 12, skipsLeft = 1, total = 30)
        assertEquals(state, decodeDeck(state.encode()))
    }

    @Test
    fun `decode rejects malformed values`() {
        assertNull(decodeDeck(null))
        assertNull(decodeDeck(""))
        assertNull(decodeDeck("12|3"))
        assertNull(decodeDeck("1|2|3|4|5"))
        assertNull(decodeDeck("non-un-numero|2|3|4"))
        assertNull(decodeDeck("1|2|3|"))
    }

    @Test
    fun `encode keeps the field order`() {
        assertEquals("7|1|2|29", DeckState(seed = 7, drawn = 1, skipsLeft = 2, total = 29).encode())
    }
}
