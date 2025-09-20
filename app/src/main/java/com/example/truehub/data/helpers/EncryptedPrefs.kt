package com.example.truehub.data.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val API_KEY_PREF = "api_key"
private const val IS_LOGGED_IN = "is_logged_in"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name ="secure_preferences")
object EncryptedPrefs {
    suspend fun saveApiKey(context: Context,apiKey : String){
        val apiKeyPref = stringPreferencesKey(API_KEY_PREF)
        context.dataStore.edit {
            it[apiKeyPref] = apiKey
        }
    }

    suspend fun getApiKey(context: Context) : String?{
        val apiKeyPref = stringPreferencesKey(API_KEY_PREF)
        val preferences = context.dataStore.data.first()
        return preferences[apiKeyPref]
    }

    fun isLoggedInFlow(context: Context): Flow<Boolean> {
        val isLoggedInPref = booleanPreferencesKey(IS_LOGGED_IN)
        return context.dataStore.data.map { prefs ->
            prefs[isLoggedInPref] ?: false
        }
    }

    suspend fun saveIsLoggedIn(context: Context,isLoggedIn : Boolean){
        val isLoggedInPref = booleanPreferencesKey(IS_LOGGED_IN)
        context.dataStore.edit {
            it[isLoggedInPref] = isLoggedIn
        }
    }
}