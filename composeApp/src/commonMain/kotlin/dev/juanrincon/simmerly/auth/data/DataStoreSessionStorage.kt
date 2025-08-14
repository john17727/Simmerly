package dev.juanrincon.simmerly.auth.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.auth.domain.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreSessionStorage(private val preferences: DataStore<Preferences>) : SessionStorage {
    override suspend fun getToken(): String? =
        preferences.data.first()[stringPreferencesKey(TOKEN_KEY)]

    override suspend fun setToken(token: String) {
        preferences.edit { store -> store[stringPreferencesKey(TOKEN_KEY)] = token }
    }

    override fun isAuthenticated(): Flow<AuthState> = preferences.data.map { preferences ->
        if (preferences[stringPreferencesKey(TOKEN_KEY)].isNullOrBlank()) {
            AuthState.Unauthenticated
        } else {
            AuthState.Authenticated
        }
    }

    override suspend fun clear() {
        preferences.edit { store ->
            store[stringPreferencesKey(TOKEN_KEY)] = ""
        }
    }

    companion object {
        const val TOKEN_KEY = "token"
    }
}