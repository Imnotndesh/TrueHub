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
private const val USERNAME_PREF = "username"
private const val AUTH_TOKEN_PREF = "auth_token"
private const val LOGIN_METHOD_PREF = "login_method"
private const val AUTO_LOGIN_PREF = "auto_login"
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
    suspend fun clearApiKey(context: Context){
        val apiKeyPref = stringPreferencesKey(API_KEY_PREF)
        context.dataStore.edit {
            it.remove(apiKeyPref)
        }
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
    suspend fun getIsLoggedIn(context: Context): Boolean{
        val isLoggedInPref = booleanPreferencesKey(IS_LOGGED_IN)
        val prefs = context.dataStore.data.first()
        return prefs[isLoggedInPref] == true
    }
    suspend fun clearIsLoggedIn(context: Context){
        val isLoggedInPref = booleanPreferencesKey(IS_LOGGED_IN)
        context.dataStore.edit {
            it.remove(isLoggedInPref)
        }
    }

    suspend fun clear(context: Context){
        context.dataStore.edit {
            it.clear()
        }
    }

    suspend fun saveAuthToken(context: Context,token:String){
        val tokenPref = stringPreferencesKey(AUTH_TOKEN_PREF)
        context.dataStore.edit {
            it[tokenPref] = token
        }
    }
    suspend fun getAuthToken(context: Context) : String?{
        val tokenPref = stringPreferencesKey(AUTH_TOKEN_PREF)
        val preferences = context.dataStore.data.first()
        return preferences[tokenPref]
    }
    suspend fun clearAuthToken(context: Context){
        val tokenPref = stringPreferencesKey(AUTH_TOKEN_PREF)
        context.dataStore.edit {
            it.remove(tokenPref)
        }
    }

    suspend fun saveLoginMethod(context: Context,method: String){
        val loginMethodPref = stringPreferencesKey(LOGIN_METHOD_PREF)
        context.dataStore.edit {
            it[loginMethodPref] = method
        }
    }
    suspend fun getLoginMethod(context: Context) : String?{
        val loginMethodPref = stringPreferencesKey(LOGIN_METHOD_PREF)
        val preferences = context.dataStore.data.first()
        return preferences[loginMethodPref]
    }
    suspend fun getUseAutoLogin(context: Context): Boolean{
        val autoLoginPref = booleanPreferencesKey(AUTO_LOGIN_PREF)
        val prefs =  context.dataStore.data.first()
        return prefs[autoLoginPref] == true
    }
    suspend fun saveUseAutoLogin(context: Context){
        val autoLoginPref = booleanPreferencesKey(AUTO_LOGIN_PREF)
        context.dataStore.edit {
            it[autoLoginPref] = true
        }
    }
    suspend fun revokeUseAutoLogin(context: Context){
        val autoLoginPref = booleanPreferencesKey(AUTO_LOGIN_PREF)
        context.dataStore.edit {
            it[autoLoginPref] = true
        }
    }
}