package com.sortify.app.ui

import org.junit.Assert.*
import org.junit.Test

class PlayerGesturePolicyTest {
    @Test fun `intentional swipe up opens collapsed player`() {
        assertTrue(settlePlayerExpanded(false, .85f, .07f, 0f, 900f))
    }
    @Test fun `small upward movement returns to mini player`() {
        assertFalse(settlePlayerExpanded(false, .98f, .07f, 0f, 900f))
    }
    @Test fun `intentional swipe down closes expanded player`() {
        assertFalse(settlePlayerExpanded(true, .15f, .07f, 0f, 900f))
    }
    @Test fun `small downward movement keeps player expanded`() {
        assertTrue(settlePlayerExpanded(true, .02f, .07f, 0f, 900f))
    }
    @Test fun `fling direction overrides distance for quick gestures and reversals`() {
        assertTrue(settlePlayerExpanded(false, .98f, .07f, -1200f, 900f))
        assertFalse(settlePlayerExpanded(true, .02f, .07f, 1200f, 900f))
        assertTrue(settlePlayerExpanded(true, .2f, .07f, -1200f, 900f))
    }
}
