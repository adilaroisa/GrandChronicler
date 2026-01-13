package com.example.grandchroniclerapp.data


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_sessions")

class UserPreferences(private val context: Context) {
    private val USER_ID_KEY = intPreferencesKey("user_id")

    suspend fun saveUserId(userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    val getUserId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY] ?: -1
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}