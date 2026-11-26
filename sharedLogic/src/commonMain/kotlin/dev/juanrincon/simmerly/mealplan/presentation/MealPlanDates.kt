package dev.juanrincon.simmerly.mealplan.presentation

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun LocalDate.startOfWeek(firstDay: DayOfWeek): LocalDate {
    val offset = (dayOfWeek.isoDayNumber - firstDay.isoDayNumber + 7) % 7
    return minus(offset, DateTimeUnit.DAY)
}

fun LocalDate.plusDays(days: Int): LocalDate = plus(days, DateTimeUnit.DAY)

/** The seven days of the week containing this date. */
fun LocalDate.weekDays(firstDay: DayOfWeek): List<LocalDate> {
    val start = startOfWeek(firstDay)
    return List(7) { start.plusDays(it) }
}

/** First day shown by a six-row month grid for this date's month. */
fun LocalDate.monthGridStart(firstDay: DayOfWeek): LocalDate =
    LocalDate(year, month, 1).startOfWeek(firstDay)

const val MONTH_GRID_DAYS = 42

private val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

fun DayOfWeek.displayName(): String = dayNames[ordinal]
