package dev.juanrincon.simmerly.recipes.domain

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationParserTest {

    @Test
    fun parsesMinutes() {
        val result = parseDurations("Cook for 8 minutes, until crisp.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(8.minutes)
    }

    @Test
    fun parsesAbbreviatedMinutes() {
        val result = parseDurations("Rest for 5 min before serving.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(5.minutes)
    }

    @Test
    fun parsesSeconds() {
        val result = parseDurations("Blitz for 30 seconds.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(30.seconds)
    }

    @Test
    fun parsesHours() {
        val result = parseDurations("Braise for 2 hours on low heat.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(2.hours)
    }

    @Test
    fun parsesAbbreviatedHours() {
        val result = parseDurations("Marinate for 1 hr.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(1.hours)
    }

    @Test
    fun rangeResolvesToLowerBound() {
        val result = parseDurations("Simmer for 8-10 minutes, stirring occasionally.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(8.minutes)
    }

    @Test
    fun rangeWithEnDashResolvesToLowerBound() {
        val result = parseDurations("Bake for 20–25 minutes.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(20.minutes)
    }

    @Test
    fun handlesSingularUnit() {
        val result = parseDurations("Wait 1 minute before flipping.")

        assertThat(result).hasSize(1)
        assertThat(result.first().duration).isEqualTo(1.minutes)
    }

    @Test
    fun findsMultipleDurationsInOneStep() {
        val result = parseDurations(
            "Cook the guanciale for 8 minutes, then let it rest for 1 minute off heat."
        )

        assertThat(result).hasSize(2)
        assertThat(result[0].duration).isEqualTo(8.minutes)
        assertThat(result[1].duration).isEqualTo(1.minutes)
    }

    @Test
    fun ignoresQuantitiesWithoutTimeUnits() {
        val result = parseDurations("Add 200 g spaghetti and 2 large eggs.")

        assertThat(result).isEmpty()
    }

    @Test
    fun ignoresTextWithNoNumbers() {
        val result = parseDurations("Whisk until smooth and glossy.")

        assertThat(result).isEmpty()
    }

    @Test
    fun sourceTextCapturesTheOriginalMatch() {
        val result = parseDurations("Rest for 10 minutes.")

        assertThat(result.first().sourceText).isEqualTo("10 minutes")
    }
}
