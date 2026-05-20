package dev.juanrincon.simmerly.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun dateStringToInstant(dateString: String): Instant {
    // Step 1: Parse the string into a date-only object.
    // The format "YYYY-MM-DD" is the ISO standard and is parsed by default.
    val localDate: LocalDate = LocalDate.parse(dateString)

    // Step 2: Add a time to it. We'll use the beginning of the day (midnight).
    // This creates a "local" date-time, which is not yet tied to a timezone.
    val localDateTime = localDate.atTime(hour = 0, minute = 0)

    // Step 3: Convert it to a unique Instant by specifying the time zone.
    // TimeZone.UTC is the safest and most common choice for this.
    return localDateTime.toInstant(TimeZone.UTC)
}