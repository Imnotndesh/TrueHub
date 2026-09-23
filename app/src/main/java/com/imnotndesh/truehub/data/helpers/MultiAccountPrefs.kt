package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first

private const val MULTI_ACCOUNT_DATASTORE = "multi_account_preferences"

val Context.multiAccountDataStore: DataStore<Preferences> by preferencesDataStore(
    name = MULTI_ACCOUNT_DATASTORE
)

object MultiAccountPrefs {
    /** Fallback TTL used when a token was stored before metadata tracking existed. */
    const val DEFAULT_TOKEN_TTL_SECONDS = 600

    /** Generous TTL requested for interactive sessions; the server caps it as it sees fit. */
    const val LONG_TOKEN_TTL_SECONDS = 31_536_000

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val serversAdapter = moshi.adapter<List<SavedServer>>(
        Types.newParameterizedType(List::class.java, SavedServer::class.java)
    )
    private val accountsAdapter = moshi.adapter<List<SavedAccount>>(
        Types.newParameterizedType(List::class.java, SavedAccount::class.java)
    )

    private val SERVERS_KEY = stringPreferencesKey("saved_servers")
    private val ACCOUNTS_KEY = stringPreferencesKey("saved_accounts")
    private val LAST_USED_PROFILE_KEY = stringPreferencesKey("last_used_profile")

    // Server Management
    suspend fun saveServer(context: Context, server: SavedServer) {
        val servers = getServers(context).toMutableList()
        val existingIndex = servers.indexOfFirst { it.id == server.id }

        if (existingIndex >= 0) {
            servers[existingIndex] = server
        } else {
            servers.add(server)
        }

        context.multiAccountDataStore.edit { prefs ->
            prefs[SERVERS_KEY] = serversAdapter.toJson(servers)
        }
    }

    suspend fun getServers(context: Context): List<SavedServer> {
        val prefs = context.multiAccountDataStore.data.first()
        val json = prefs[SERVERS_KEY] ?: return emptyList()
        return serversAdapter.fromJson(json) ?: emptyList()
    }

    suspend fun getServer(context: Context, serverId: String): SavedServer? {
        return getServers(context).find { it.id == serverId }
    }

    suspend fun deleteServer(context: Context, serverId: String) {
        val servers = getServers(context).filterNot { it.id == serverId }
        val allAccounts = getAccounts(context)
        val deletedAccounts = allAccounts.filter { it.serverId == serverId }
        val accounts = allAccounts.filterNot { it.serverId == serverId }
        val currentSession = getCurrentSession(context)

        context.multiAccountDataStore.edit { prefs ->
            prefs[SERVERS_KEY] = serversAdapter.toJson(servers)
            prefs[ACCOUNTS_KEY] = accountsAdapter.toJson(accounts)
        }

        // Clear credentials for deleted accounts
        deletedAccounts.forEach { account ->
            clearAccountCredentials(context, account.id)
            removeAccountToken(context, account.id)
            clearLastUsedProfile(context, account.id)
        }
        if (currentSession?.second?.let { it in deletedAccounts.map { account -> account.id } } == true) {
            clearCurrentSession(context)
        }
    }

    // Account Management
    suspend fun saveAccount(context: Context, account: SavedAccount) {
        val accounts = getAccounts(context).toMutableList()
        val existingIndex = accounts.indexOfFirst { it.id == account.id }

        if (existingIndex >= 0) {
            accounts[existingIndex] = account
        } else {
            accounts.add(account)
        }

        context.multiAccountDataStore.edit { prefs ->
            prefs[ACCOUNTS_KEY] = accountsAdapter.toJson(accounts)
        }
    }

    suspend fun getAccounts(context: Context): List<SavedAccount> {
        val prefs = context.multiAccountDataStore.data.first()
        val json = prefs[ACCOUNTS_KEY] ?: return emptyList()
        return accountsAdapter.fromJson(json) ?: emptyList()
    }

    suspend fun getAccount(context: Context, accountId: String): SavedAccount? {
        return getAccounts(context).find { it.id == accountId }
    }

    suspend fun getAccountsForServer(context: Context, serverId: String): List<SavedAccount> {
        return getAccounts(context).filter { it.serverId == serverId }
    }

    suspend fun deleteAccount(context: Context, accountId: String) {
        val accounts = getAccounts(context).filterNot { it.id == accountId }
        val currentSession = getCurrentSession(context)

        context.multiAccountDataStore.edit { prefs ->
            prefs[ACCOUNTS_KEY] = accountsAdapter.toJson(accounts)
        }

        clearAccountCredentials(context, accountId)
        removeAccountToken(context, accountId)
        clearLastUsedProfile(context, accountId)
        if (currentSession?.second == accountId) {
            clearCurrentSession(context)
        }
    }

    // Credential Storage (encrypted)
    private fun getCredentialKey(accountId: String, type: String) =
        stringPreferencesKey("cred_${accountId}_$type")

    suspend fun saveAccountCredentials(
        context: Context,
        accountId: String,
        loginMethod: LoginMethod,
        apiKey: String? = null,
        username: String? = null,
        password: String? = null
    ) {
        context.dataStore.edit { prefs ->
            when (loginMethod) {
                LoginMethod.API_KEY -> {
                    apiKey?.let { prefs[getCredentialKey(accountId, "api_key")] = it }
                }
                LoginMethod.PASSWORD, LoginMethod.TOTP -> {
                    username?.let { prefs[getCredentialKey(accountId, "username")] = it }
                    password?.let { prefs[getCredentialKey(accountId, "password")] = it }
                }
            }
        }
    }

    suspend fun getAccountCredentials(
        context: Context,
        accountId: String,
        loginMethod: LoginMethod
    ): Pair<String?, String?> {
        val prefs = context.dataStore.data.first()

        return when (loginMethod) {
            LoginMethod.API_KEY -> {
                val apiKey = prefs[getCredentialKey(accountId, "api_key")]
                apiKey to null
            }
            LoginMethod.PASSWORD, LoginMethod.TOTP -> {
                val username = prefs[getCredentialKey(accountId, "username")]
                val password = prefs[getCredentialKey(accountId, "password")]
                username to password
            }
        }
    }

    suspend fun clearAccountCredentials(context: Context, accountId: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(getCredentialKey(accountId, "api_key"))
            prefs.remove(getCredentialKey(accountId, "username"))
            prefs.remove(getCredentialKey(accountId, "password"))
        }
    }

    // Last Used Profile
    suspend fun saveLastUsedProfile(context: Context, serverId: String, accountId: String) {
        context.multiAccountDataStore.edit { prefs ->
            prefs[LAST_USED_PROFILE_KEY] = "$serverId:$accountId"
        }

        // Update last used timestamps
        getServer(context, serverId)?.let { server ->
            saveServer(context, server.copy(lastUsed = System.currentTimeMillis()))
        }
        getAccount(context, accountId)?.let { account ->
            saveAccount(context, account.copy(lastUsed = System.currentTimeMillis()))
        }
    }

    suspend fun activateLastUsedProfileThenSaveCurrentSession(
        context: Context,
        serverId: String,
        accountId: String,
        token: String,
        ttlSeconds: Int = LONG_TOKEN_TTL_SECONDS
    ) {
        saveLastUsedProfile(context, serverId, accountId)
        saveCurrentSession(context, serverId, accountId, token, ttlSeconds)
    }

    suspend fun getLastUsedProfile(context: Context): Pair<String, String>? {
        val prefs = context.multiAccountDataStore.data.first()
        val value = prefs[LAST_USED_PROFILE_KEY] ?: return null
        val parts = value.split(":")
        if (parts.size != 2) return null
        return parts[0] to parts[1]
    }

    suspend fun clearLastUsedProfile(context: Context, accountId: String) {
        context.multiAccountDataStore.edit { prefs ->
            val value = prefs[LAST_USED_PROFILE_KEY] ?: return@edit
            val parts = value.split(":")
            if (parts.size == 2 && parts[1] == accountId) {
                prefs.remove(LAST_USED_PROFILE_KEY)
            }
        }
    }

    suspend fun getTokenForLastUsed(context: Context): String? {
        val (lastServerId, lastAccountId) = getLastUsedProfile(context) ?: return null

        val (currentServerId, currentAccountId, token) = getCurrentSession(context) ?: return null

        // Ensure the active session matches the last used profile
        return if (lastServerId == currentServerId && lastAccountId == currentAccountId) {
            token
        } else {
            null
        }
    }

    /**
     * Saves a new authentication token for the most recently used account.
     * This updates the active session keys (CURRENT_SESSION_KEY and CURRENT_TOKEN_KEY).
     */
    suspend fun saveTokenForLastUsed(context: Context, token: String, ttlSeconds: Int = DEFAULT_TOKEN_TTL_SECONDS) {
        val (serverId, accountId) = getLastUsedProfile(context) ?: return
        saveCurrentSession(context, serverId, accountId, token, ttlSeconds)
    }

    // Session Management (current active session)
    private val CURRENT_SESSION_KEY = stringPreferencesKey("current_session")
    private val CURRENT_TOKEN_KEY = stringPreferencesKey("current_token")
    private val TOKEN_ACQUIRED_AT_KEY = stringPreferencesKey("current_token_acquired_at")
    private val TOKEN_TTL_KEY = stringPreferencesKey("current_token_ttl")

    private fun getAccountTokenKey(accountId: String, type: String) =
        stringPreferencesKey("account_token_${accountId}_$type")

    private fun getAccountTokenEntries(prefs: Preferences, accountId: String): Triple<String, Long, Int>? {
        val token = prefs[getAccountTokenKey(accountId, "token")] ?: return null
        val acquiredAt = prefs[getAccountTokenKey(accountId, "acquired_at")]?.toLongOrNull() ?: 0L
        val ttl = prefs[getAccountTokenKey(accountId, "ttl")]?.toIntOrNull() ?: DEFAULT_TOKEN_TTL_SECONDS
        return Triple(token, acquiredAt, ttl)
    }

    private fun writeAccountToken(
        prefs: MutablePreferences,
        accountId: String,
        token: String,
        acquiredAtEpochMs: Long,
        ttlSeconds: Int
    ) {
        prefs[getAccountTokenKey(accountId, "token")] = token
        prefs[getAccountTokenKey(accountId, "acquired_at")] = acquiredAtEpochMs.toString()
        prefs[getAccountTokenKey(accountId, "ttl")] = ttlSeconds.toString()
    }

    private fun tokenElapsedFraction(acquiredAtEpochMs: Long, ttlSeconds: Int, nowMs: Long): Float {
        if (ttlSeconds <= 0) return 1f
        val lifetimeMs = ttlSeconds * 1000L
        return ((nowMs - acquiredAtEpochMs).toFloat() / lifetimeMs).coerceIn(0f, 2f)
    }

    data class AccountTokenSnapshot(
        val accountId: String,
        val token: String,
        val acquiredAtEpochMs: Long,
        val ttlSeconds: Int
    ) {
        fun elapsedFraction(nowMs: Long = System.currentTimeMillis()): Float {
            return tokenElapsedFraction(acquiredAtEpochMs, ttlSeconds, nowMs)
        }

        fun isExpiringSoon(nowMs: Long = System.currentTimeMillis(), safetyFraction: Float = 0.8f): Boolean =
            elapsedFraction(nowMs) >= safetyFraction
    }

    /** A persisted session plus the metadata we need to reason about token freshness. */
    data class SessionSnapshot(
        val serverId: String,
        val accountId: String,
        val token: String,
        val acquiredAtEpochMs: Long,
        val ttlSeconds: Int
    ) {
        /** Fraction of the token lifetime already elapsed (1.0 == expired). */
        fun elapsedFraction(nowMs: Long = System.currentTimeMillis()): Float {
            return tokenElapsedFraction(acquiredAtEpochMs, ttlSeconds, nowMs)
        }

        /** True once the token is close enough to expiry that we should refresh proactively. */
        fun isExpiringSoon(nowMs: Long = System.currentTimeMillis(), safetyFraction: Float = 0.8f): Boolean =
            elapsedFraction(nowMs) >= safetyFraction
    }

    suspend fun saveAccountToken(
        context: Context,
        accountId: String,
        token: String,
        ttlSeconds: Int = DEFAULT_TOKEN_TTL_SECONDS,
        acquiredAtEpochMs: Long = System.currentTimeMillis()
    ) {
        context.dataStore.edit { prefs ->
            writeAccountToken(prefs, accountId, token, acquiredAtEpochMs, ttlSeconds)
        }
    }

    suspend fun getAccountToken(context: Context, accountId: String): AccountTokenSnapshot? {
        val prefs = context.dataStore.data.first()
        val (token, acquiredAt, ttl) = getAccountTokenEntries(prefs, accountId) ?: return null
        return AccountTokenSnapshot(accountId, token, acquiredAt, ttl)
    }

    suspend fun getAccountTokenSnapshot(context: Context, accountId: String): AccountTokenSnapshot? {
        return getAccountToken(context, accountId)
    }

    suspend fun resolveAccountTokenSnapshot(
        context: Context,
        serverId: String,
        accountId: String
    ): AccountTokenSnapshot? {
        val account = getAccount(context, accountId) ?: return null
        if (account.serverId != serverId) return null
        return getAccountToken(context, accountId)
    }

    private suspend fun removeAccountTokenEntries(context: Context, accountId: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(getAccountTokenKey(accountId, "token"))
            prefs.remove(getAccountTokenKey(accountId, "acquired_at"))
            prefs.remove(getAccountTokenKey(accountId, "ttl"))
        }
    }

    suspend fun clearAccountToken(context: Context, accountId: String) {
        removeAccountTokenEntries(context, accountId)
    }

    suspend fun removeAccountToken(context: Context, accountId: String) {
        removeAccountTokenEntries(context, accountId)
    }

    suspend fun saveCurrentSession(
        context: Context,
        serverId: String,
        accountId: String,
        token: String,
        ttlSeconds: Int = DEFAULT_TOKEN_TTL_SECONDS
    ) {
        val acquiredAtEpochMs = System.currentTimeMillis()
        context.dataStore.edit { prefs ->
            prefs[CURRENT_SESSION_KEY] = "$serverId:$accountId"
            prefs[CURRENT_TOKEN_KEY] = token
            prefs[TOKEN_ACQUIRED_AT_KEY] = acquiredAtEpochMs.toString()
            prefs[TOKEN_TTL_KEY] = ttlSeconds.toString()
            writeAccountToken(prefs, accountId, token, acquiredAtEpochMs, ttlSeconds)
        }
    }

    /** Full session snapshot with freshness metadata, or null when nothing is stored. */
    suspend fun getSessionSnapshot(context: Context): SessionSnapshot? {
        val prefs = context.dataStore.data.first()
        val session = prefs[CURRENT_SESSION_KEY] ?: return null
        val token = prefs[CURRENT_TOKEN_KEY] ?: return null
        val parts = session.split(":")
        if (parts.size != 2) return null
        val acquiredAt = prefs[TOKEN_ACQUIRED_AT_KEY]?.toLongOrNull() ?: 0L
        val ttl = prefs[TOKEN_TTL_KEY]?.toIntOrNull() ?: DEFAULT_TOKEN_TTL_SECONDS
        return SessionSnapshot(parts[0], parts[1], token, acquiredAt, ttl)
    }

    suspend fun getCurrentSession(context: Context): Triple<String, String, String>? {
        val prefs = context.dataStore.data.first()
        val session = prefs[CURRENT_SESSION_KEY] ?: return null
        val token = prefs[CURRENT_TOKEN_KEY] ?: return null
        val parts = session.split(":")
        if (parts.size != 2) return null
        return Triple(parts[0], parts[1], token)
    }

    suspend fun clearCurrentSession(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(CURRENT_SESSION_KEY)
            prefs.remove(CURRENT_TOKEN_KEY)
            prefs.remove(TOKEN_ACQUIRED_AT_KEY)
            prefs.remove(TOKEN_TTL_KEY)
        }
    }

    // Clear all data
    suspend fun clearAll(context: Context) {
        context.multiAccountDataStore.edit { it.clear() }
        context.dataStore.edit { it.clear() }
    }
}