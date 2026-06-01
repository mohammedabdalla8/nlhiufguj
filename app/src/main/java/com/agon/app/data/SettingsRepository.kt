package com.agon.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "privacy_settings")

/**
 * Single source of truth for persisted privacy settings, backed by DataStore.
 * Exposes a reactive [Flow] and suspend mutators. Manual singleton via [get].
 */
class SettingsRepository private constructor(private val appContext: Context) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val LEVEL = stringPreferencesKey("level")
        val DIM = floatPreferencesKey("dim_intensity")
        val DENSITY = floatPreferencesKey("filter_density")
        val COLOR = stringPreferencesKey("filter_color")
        val LOUVER = booleanPreferencesKey("louver")
        val ADAPTIVE = booleanPreferencesKey("adaptive")
        val AUTO_START = booleanPreferencesKey("auto_start")
    }

    val settings: Flow<PrivacySettings> = appContext.dataStore.data.map { p ->
        PrivacySettings(
            enabled = p[Keys.ENABLED] ?: false,
            level = PrivacyLevel.fromName(p[Keys.LEVEL]),
            dimIntensity = p[Keys.DIM] ?: PrivacyLevel.MEDIUM.baseDim,
            filterDensity = p[Keys.DENSITY] ?: PrivacyLevel.MEDIUM.baseDensity,
            filterColor = FilterColor.fromName(p[Keys.COLOR]),
            louverPattern = p[Keys.LOUVER] ?: true,
            adaptiveMasking = p[Keys.ADAPTIVE] ?: false,
            autoStart = p[Keys.AUTO_START] ?: false,
        )
    }

    suspend fun setEnabled(value: Boolean) = appContext.dataStore.edit { it[Keys.ENABLED] = value }

    suspend fun setLevel(level: PrivacyLevel) = appContext.dataStore.edit {
        it[Keys.LEVEL] = level.name
        it[Keys.DIM] = level.baseDim
        it[Keys.DENSITY] = level.baseDensity
    }

    suspend fun setDim(value: Float) = appContext.dataStore.edit { it[Keys.DIM] = value }
    suspend fun setDensity(value: Float) = appContext.dataStore.edit { it[Keys.DENSITY] = value }
    suspend fun setColor(c: FilterColor) = appContext.dataStore.edit { it[Keys.COLOR] = c.name }
    suspend fun setLouver(value: Boolean) = appContext.dataStore.edit { it[Keys.LOUVER] = value }
    suspend fun setAdaptive(value: Boolean) = appContext.dataStore.edit { it[Keys.ADAPTIVE] = value }
    suspend fun setAutoStart(value: Boolean) = appContext.dataStore.edit { it[Keys.AUTO_START] = value }

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
