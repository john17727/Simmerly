package dev.juanrincon.simmerly.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Records the app's startup and recipe-list scrolling so the hot paths are precompiled on install.
 *
 * The app must already be signed in on the benchmark build (`dev.juanrincon.simmerly.benchmark`):
 * install it, log in once by hand, then run the generator. No credentials live in this module.
 */
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // Passed in by the baselineprofile Gradle plugin.
        val packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: error("targetAppId missing; run through the baselineprofile Gradle plugin")

        rule.collect(packageName, includeInStartupProfile = true) {
            pressHome()
            startActivityAndWait()

            // Wait for the recipe list's title (the phone layout's top bar) before interacting.
            device.wait(Until.hasObject(By.text("Recipes")), 20_000)
            device.waitForIdle()

            scrollList(device)
            openFirstRecipeAndBack(device)
        }
    }

    private fun scrollList(device: UiDevice) {
        val width = device.displayWidth
        val height = device.displayHeight
        // Fling down the list a few times, then back up; each swipe pages in more items.
        repeat(5) {
            device.swipe(width / 2, height * 3 / 4, width / 2, height / 4, 20)
            device.waitForIdle()
        }
        repeat(3) {
            device.swipe(width / 2, height / 4, width / 2, height * 3 / 4, 20)
            device.waitForIdle()
        }
    }

    private fun openFirstRecipeAndBack(device: UiDevice) {
        // Tap roughly where the first card sits on a phone, then come back.
        device.click(device.displayWidth / 2, device.displayHeight * 3 / 10)
        device.waitForIdle(2_000)
        device.pressBack()
        device.waitForIdle()
    }
}
