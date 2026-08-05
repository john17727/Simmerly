package dev.juanrincon.simmerly.recipes.domain

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isLessThanOrEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
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

    @Test
    fun rangeKeepsBothBounds() {
        val result = parseDurations("Cook for 15–17 minutes, until al dente.").first()

        assertThat(result.duration).isEqualTo(15.minutes)
        assertThat(result.upperBound).isEqualTo(17.minutes)
        assertThat(result.isRange).isTrue()
    }

    @Test
    fun parsesWordyRange() {
        val result = parseDurations("Bake for 45 to 60 minutes.").first()

        assertThat(result.duration).isEqualTo(45.minutes)
        assertThat(result.upperBound).isEqualTo(60.minutes)
    }

    @Test
    fun singleDurationIsNotARange() {
        val result = parseDurations("Rest for 10 minutes.").first()

        assertThat(result.upperBound).isNull()
        assertThat(result.isRange).isFalse()
    }

    @Test
    fun singleDurationSuggestsOnlyItself() {
        val options = parseDurations("Rest for 10 minutes.").first().suggestedTimerOptions()

        assertThat(options).isEqualTo(listOf(10.minutes))
    }

    @Test
    fun narrowRangeSuggestsEveryMinute() {
        val options = parseDurations("Cook for 15–17 minutes.").first().suggestedTimerOptions()

        assertThat(options).isEqualTo(listOf(15.minutes, 16.minutes, 17.minutes))
    }

    @Test
    fun wideRangeCoarsensToRoundSteps() {
        val options = parseDurations("Bake for 45–60 minutes.").first().suggestedTimerOptions()

        assertThat(options).isEqualTo(listOf(45.minutes, 50.minutes, 55.minutes, 60.minutes))
    }

    @Test
    fun hourRangeStaysReasonable() {
        val options = parseDurations("Braise for 1–2 hours.").first().suggestedTimerOptions()

        assertThat(options).isEqualTo(
            listOf(60.minutes, 75.minutes, 90.minutes, 105.minutes, 120.minutes)
        )
    }

    @Test
    fun subMinuteRangeStepsInSeconds() {
        val options = parseDurations("Whisk for 30–45 seconds.").first().suggestedTimerOptions()

        assertThat(options).isEqualTo(
            listOf(30.seconds, 35.seconds, 40.seconds, 45.seconds)
        )
    }

    @Test
    fun rangeCrossingAMinuteStillStepsInSeconds() {
        // The lower bound is sub-minute, so counting in whole minutes would round it to zero.
        val options = parseDurations("Sear for 45–90 seconds.").first().suggestedTimerOptions()

        assertThat(options).isEqualTo(
            listOf(45.seconds, 55.seconds, 65.seconds, 75.seconds, 85.seconds, 90.seconds)
        )
    }

    @Test
    fun mixedUnitRangeParsesAsTwoSeparateDurations() {
        // A range is only a range when both bounds share one trailing unit. "45 seconds to
        // 2 minutes" has a unit on each side, so it reads as two independent detections rather
        // than one span — which is also the more useful reading for a timer list.
        val result = parseDurations("Sear for 45 seconds to 2 minutes.")

        assertThat(result).hasSize(2)
        assertThat(result[0].duration).isEqualTo(45.seconds)
        assertThat(result[0].isRange).isFalse()
        assertThat(result[1].duration).isEqualTo(2.minutes)
    }

    @Test
    fun suggestionsNeverExceedTheOptionCap() {
        val options = parseDurations("Cure for 10–180 minutes.").first().suggestedTimerOptions()

        assertThat(options.size).isLessThanOrEqualTo(7) // cap, plus the always-included upper bound
        assertThat(options.first()).isEqualTo(10.minutes)
        assertThat(options.last()).isEqualTo(180.minutes)
    }

    @Test
    fun suggestionsAlwaysIncludeBothBounds() {
        // 13 is not reachable from 4 by any ladder step, so the upper bound must be appended.
        val options = parseDurations("Proof for 4–13 minutes.").first().suggestedTimerOptions()

        assertThat(options.first()).isEqualTo(4.minutes)
        assertThat(options.last()).isEqualTo(13.minutes)
    }
}
