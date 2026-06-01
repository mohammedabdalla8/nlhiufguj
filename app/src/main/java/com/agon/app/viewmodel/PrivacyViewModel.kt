package com.agon.app.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.FilterColor
import com.agon.app.data.PrivacyLevel
import com.agon.app.data.PrivacySettings
import com.agon.app.data.SettingsRepository
import com.agon.app.service.PrivacyOverlayService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MVVM ViewModel — mediates between the UI and the repository/service.
 * Holds no Android View references; exposes immutable state + intent methods.
 */
class PrivacyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository.get(app)

    val state: StateFlow<PrivacySettings> = repo.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrivacySettings(),
    )

    fun hasOverlayPermission(): Boolean =
        Settings.canDrawOverlays(getApplication<Application>())

    fun toggle(enabled: Boolean) {
        val ctx: Context = getApplication()
        viewModelScope.launch {
            repo.setEnabled(enabled)
            if (enabled) PrivacyOverlayService.start(ctx)
            else PrivacyOverlayService.stop(ctx)
        }
    }

    fun setLevel(level: PrivacyLevel) = viewModelScope.launch { repo.setLevel(level) }
    fun setDim(value: Float) = viewModelScope.launch { repo.setDim(value) }
    fun setDensity(value: Float) = viewModelScope.launch { repo.setDensity(value) }
    fun setColor(c: FilterColor) = viewModelScope.launch { repo.setColor(c) }
    fun setLouver(v: Boolean) = viewModelScope.launch { repo.setLouver(v) }
    fun setAdaptive(v: Boolean) = viewModelScope.launch { repo.setAdaptive(v) }
    fun setAutoStart(v: Boolean) = viewModelScope.launch { repo.setAutoStart(v) }
}
