package dev.juanrincon.simmerly.timeline.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.timeline.aTimelineEvent
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TimelineDateFormatTest {
    private val noon = Instant.parse("2026-11-22T12:00:00Z")

    @Test
    fun fullDateIncludesTheWeekday() {
        assertThat(formatTimelineDate(noon, TimeZone.UTC)).isEqualTo("Sun, Nov 22, 2026")
    }

    @Test
    fun shortDateOmitsTheYear() {
        assertThat(formatMonthDay(noon, TimeZone.UTC)).isEqualTo("Nov 22")
        assertThat(formatShortDate(noon, TimeZone.UTC)).isEqualTo("Nov 22, 2026")
    }

    @Test
    fun theLocalDayFollowsTheTimeZone() {
        val lateUtc = Instant.parse("2026-11-22T23:30:00Z")

        assertThat(formatMonthDay(lateUtc, TimeZone.of("Asia/Tokyo"))).isEqualTo("Nov 23")
    }

    @Test
    fun uiModelUsesTwoInitialsAndDropsABlankMessage() {
        val ui = aTimelineEvent(authorName = "Juan Carlos Rincon", message = "  ").toUi()

        assertThat(ui.authorInitials).isEqualTo("JC")
        assertThat(ui.message).isEqualTo(null)
    }
}
