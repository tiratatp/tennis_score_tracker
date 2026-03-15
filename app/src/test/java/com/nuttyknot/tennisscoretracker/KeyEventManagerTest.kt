package com.nuttyknot.tennisscoretracker

import android.view.KeyEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeyEventManagerTest {
    private lateinit var testScope: TestScope
    private lateinit var manager: KeyEventManager

    private var singleClickCount = 0
    private var doubleClickCount = 0
    private var longPressCount = 0
    private var currentTime = 1000L

    private val targetKey = KeyEvent.KEYCODE_VOLUME_UP

    @Before
    fun setup() {
        singleClickCount = 0
        doubleClickCount = 0
        longPressCount = 0
        currentTime = 1000L
        testScope = TestScope(StandardTestDispatcher())
        manager =
            KeyEventManager(
                scope = testScope,
                onSingleClick = { singleClickCount++ },
                onDoubleClick = { doubleClickCount++ },
                onLongPress = { longPressCount++ },
                timeProvider = { currentTime },
            )
    }

    private fun advanceTime(ms: Long) {
        currentTime += ms
        testScope.advanceTimeBy(ms)
        testScope.runCurrent()
    }

    private fun keyDown(keyCode: Int = targetKey) = manager.onKeyDown(keyCode, null)

    private fun keyUp(keyCode: Int = targetKey) = manager.onKeyUp(keyCode)

    @Test
    fun `test non-target key down returns false`() {
        assertFalse(keyDown(KeyEvent.KEYCODE_VOLUME_DOWN))
    }

    @Test
    fun `test non-target key up returns false`() {
        assertFalse(keyUp(KeyEvent.KEYCODE_VOLUME_DOWN))
    }

    @Test
    fun `test target key down returns true`() {
        assertTrue(keyDown())
    }

    @Test
    fun `test target key up returns true`() {
        keyDown()
        assertTrue(keyUp())
    }

    @Test
    fun `test enter key accepted when target is volume up`() {
        assertTrue(keyDown(KeyEvent.KEYCODE_ENTER))
    }

    @Test
    fun `test enter key not accepted when target is not volume up`() {
        manager.targetKeyCode = KeyEvent.KEYCODE_A
        assertFalse(keyDown(KeyEvent.KEYCODE_ENTER))
    }

    @Test
    fun `test custom target key code`() {
        manager.targetKeyCode = KeyEvent.KEYCODE_SPACE
        assertTrue(keyDown(KeyEvent.KEYCODE_SPACE))
        assertFalse(keyDown(KeyEvent.KEYCODE_VOLUME_UP))
    }

    @Test
    fun `test single click fires after double click latency`() {
        keyDown()
        advanceTime(100)
        keyUp()
        advanceTime(manager.doubleClickLatency)

        assertEquals(1, singleClickCount)
        assertEquals(0, doubleClickCount)
        assertEquals(0, longPressCount)
    }

    @Test
    fun `test single click does not fire before latency expires`() {
        keyDown()
        advanceTime(100)
        keyUp()
        advanceTime(manager.doubleClickLatency - 1)

        assertEquals(0, singleClickCount)

        advanceTime(1)
        assertEquals(1, singleClickCount)
    }

    @Test
    fun `test double click fires immediately on second key up`() {
        keyDown()
        advanceTime(100)
        keyUp()

        advanceTime(100)

        keyDown()
        advanceTime(100)
        keyUp()

        assertEquals(1, doubleClickCount)
        assertEquals(0, singleClickCount)
    }

    @Test
    fun `test double click cancels pending single click`() {
        keyDown()
        advanceTime(100)
        keyUp()

        advanceTime(100)

        keyDown()
        advanceTime(100)
        keyUp()

        advanceTime(manager.doubleClickLatency)

        assertEquals(1, doubleClickCount)
        assertEquals(0, singleClickCount)
    }

    @Test
    fun `test two separate single clicks when gap exceeds latency`() {
        keyDown()
        advanceTime(100)
        keyUp()
        advanceTime(manager.doubleClickLatency)
        assertEquals(1, singleClickCount)

        keyDown()
        advanceTime(100)
        keyUp()
        advanceTime(manager.doubleClickLatency)
        assertEquals(2, singleClickCount)
        assertEquals(0, doubleClickCount)
    }

    @Test
    fun `test long press fires after long press latency`() {
        keyDown()
        advanceTime(manager.longPressLatency)

        assertEquals(1, longPressCount)
        assertEquals(0, singleClickCount)
        assertEquals(0, doubleClickCount)
    }

    @Test
    fun `test long press does not fire if key released early`() {
        keyDown()
        advanceTime(manager.longPressLatency / 2)
        keyUp()
        advanceTime(manager.longPressLatency)

        assertEquals(0, longPressCount)
    }

    @Test
    fun `test key up after long press does not trigger click`() {
        keyDown()
        advanceTime(manager.longPressLatency)
        assertEquals(1, longPressCount)

        keyUp()
        advanceTime(manager.doubleClickLatency)

        assertEquals(1, longPressCount)
        assertEquals(0, singleClickCount)
        assertEquals(0, doubleClickCount)
    }

    @Test
    fun `test long press resets for next interaction`() {
        keyDown()
        advanceTime(manager.longPressLatency)
        keyUp()
        assertEquals(1, longPressCount)

        advanceTime(100)

        keyDown()
        advanceTime(100)
        keyUp()
        advanceTime(manager.doubleClickLatency)

        assertEquals(1, longPressCount)
        assertEquals(1, singleClickCount)
    }

    @Test
    fun `test debounced key down is ignored`() {
        keyDown()
        advanceTime(KeyEventManager.DEBOUNCE_DELAY_MS - 1)
        keyDown()
        keyUp()
        advanceTime(manager.doubleClickLatency)

        assertEquals(1, singleClickCount)
        assertEquals(0, longPressCount)
    }

    @Test
    fun `test custom double click latency`() {
        manager.doubleClickLatency = 500L

        keyDown()
        advanceTime(100)
        keyUp()

        advanceTime(300)

        keyDown()
        advanceTime(100)
        keyUp()

        assertEquals(1, doubleClickCount)
        assertEquals(0, singleClickCount)
    }

    @Test
    fun `test custom long press latency`() {
        manager.longPressLatency = 2000L

        keyDown()
        advanceTime(1500)
        keyUp()
        assertEquals(0, longPressCount)

        advanceTime(manager.doubleClickLatency)

        keyDown()
        advanceTime(2000)
        assertEquals(1, longPressCount)
    }
}
