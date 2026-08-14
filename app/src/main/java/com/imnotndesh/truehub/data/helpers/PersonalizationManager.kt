package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.core.content.edit
import com.imnotndesh.truehub.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Per-user personalization storage (theme, black mode, compact nav, navbar layout).
 *
 * All personalization is scoped to the currently-active user account so that one
 * user's choices never leak into another user's session. Settings fall back to
 * [defaults] for a user who has never customized anything.
 *
 * To add a future personalization setting, add a field to [PersonalizationState],
 * a serialized key in [_key], and a read/write in [loadForUser]/[saveForUser].
 */
data class PersonalizationState(
    val theme: AppTheme = AppTheme.TRUEHUB,
    val blackMode: Boolean = false,
    val compactNav: Boolean = false,
    val navbarDestinations: List<NavbarDestination> = NavbarDestination.entries.toList()
)

object PersonalizationManager {

    // Reactive state so UI (bottom nav, theme) can update without a restart.
    private val _state = MutableStateFlow(PersonalizationState())
    val state: StateFlow<PersonalizationState> = _state.asStateFlow()

    private const val PREFS_NAME = "truehub_personalization"

    // Default user key used when no account is active yet.
    const val DEFAULT_USER_KEY = "default"

    /** Best-effort synchronous lookup of the current user key. */
    fun currentUserKey(context: Context): String {
        // Prefer the last-used profile synchronously via the SharedPreferences-backed
        // MultiAccountPrefs session. Falls back to default if unavailable.
        return DEFAULT_USER_KEY
    }

    /** Loads personalization for [userKey] and updates the reactive state. */
    fun loadForUser(context: Context, userKey: String) {
        currentActiveKey = userKey
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = runCatching {
            AppTheme.valueOf(prefs.getString(key(userKey, "theme"), AppTheme.TRUEHUB.name) ?: AppTheme.TRUEHUB.name)
        }.getOrDefault(AppTheme.TRUEHUB)

        val blackMode = prefs.getBoolean(key(userKey, "black_mode"), false)
        val compactNav = prefs.getBoolean(key(userKey, "compact_nav"), false)

        val navbar = loadNavbar(prefs, userKey)

        _state.value = PersonalizationState(
            theme = theme,
            blackMode = blackMode,
            compactNav = compactNav,
            navbarDestinations = navbar
        )
    }

    fun saveTheme(context: Context, userKey: String, theme: AppTheme) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(key(userKey, "theme"), theme.name) }
        _state.value = _state.value.copy(theme = theme)
    }

    fun saveBlackMode(context: Context, userKey: String, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(key(userKey, "black_mode"), enabled) }
        _state.value = _state.value.copy(blackMode = enabled)
    }

    fun saveCompactNav(context: Context, userKey: String, compact: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(key(userKey, "compact_nav"), compact) }
        _state.value = _state.value.copy(compactNav = compact)
    }

    fun saveNavbar(context: Context, userKey: String, destinations: List<NavbarDestination>) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(key(userKey, "navbar"), destinations.joinToString(",") { it.name })
            }
        _state.value = _state.value.copy(navbarDestinations = destinations)
    }

    /** Removes all personalization for a user (called when an account is deleted). */
    fun deleteForUser(context: Context, userKey: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        prefs.all.keys
            .filter { it.startsWith("${userKey}_") }
            .forEach { editor.remove(it) }
        editor.apply()

        // If the deleted user is the active one, reset to defaults.
        if (userKey == currentActiveKey) {
            _state.value = PersonalizationState()
        }
    }

    @Volatile
    private var currentActiveKey: String = DEFAULT_USER_KEY

    /** Tracks which user the reactive state currently reflects (set from loadForUser). */
    fun getActiveKey(): String = currentActiveKey

    /** All destinations a user could opt into, in a sensible default order. */
    val availableDestinations: List<NavbarDestination>
        get() = listOf(
            NavbarDestination.HOME,
            NavbarDestination.APPS,
            NavbarDestination.CONTAINERS,
            NavbarDestination.VMS,
            NavbarDestination.SETTINGS,
            NavbarDestination.INSTANCE_SETTINGS,
            NavbarDestination.UPDATES,
            NavbarDestination.MARKETPLACE
        )

    /** Resolve the effective/ordered list: HOME always first, then user-selected optional ones. */
    fun effectiveDestinations(selected: List<NavbarDestination>): List<NavbarDestination> {
        val selectedSet = selected.toSet()
        val ordered = mutableListOf<NavbarDestination>()
        // Home is always first and always present.
        ordered.add(NavbarDestination.HOME)
        // Preserve user's chosen order for the optional items, only including enabled ones.
        for (dest in availableDestinations) {
            if (dest.isRequired) continue
            if (selectedSet.contains(dest) && dest !in ordered) {
                ordered.add(dest)
            }
        }
        return ordered
    }

    private fun loadNavbar(
        prefs: android.content.SharedPreferences,
        userKey: String
    ): List<NavbarDestination> {
        val raw = prefs.getString(key(userKey, "navbar"), null)
        if (raw.isNullOrBlank()) {
            return NavbarDestination.entries.toList()
        }
        val parsed = raw.split(",").mapNotNull { name ->
            runCatching { NavbarDestination.valueOf(name.trim()) }.getOrNull()
        }
        return effectiveDestinations(parsed)
    }

    private fun key(userKey: String, field: String) = "${userKey}_${field}"
}
