package dev.juanrincon.simmerly.mealplan.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek

/** Caches the household's `firstDayOfWeek` so the week strip is right offline. */
class HouseholdPreferencesStore(private val preferences: DataStore<Preferences>) {
    private val key = intPreferencesKey("household_first_day_of_week")

    fun observeFirstDayOfWeek(): Flow<DayOfWeek> =
        preferences.data.map { it[key]?.toDayOfWeek() ?: DayOfWeek.MONDAY }.distinctUntilChanged()

    suspend fun setFirstDayOfWeek(value: Int) {
        preferences.edit { it[key] = value }
    }
}
