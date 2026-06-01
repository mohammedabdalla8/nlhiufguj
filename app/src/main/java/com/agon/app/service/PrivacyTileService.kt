package com.agon.app.service

import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.agon.app.R
import com.agon.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Quick Settings tile to toggle the privacy filter without opening the app.
 */
@RequiresApi(Build.VERSION_CODES.N)
class PrivacyTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartListening() {
        super.onStartListening()
        refresh()
    }

    private fun refresh() {
        val repo = SettingsRepository.get(this)
        scope.launch {
            val s = repo.settings.first()
            qsTile?.apply {
                state = if (s.enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                label = "Private Screen"
                icon = Icon.createWithResource(this@PrivacyTileService, R.drawable.ic_privacy_tile)
                updateTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()
        val repo = SettingsRepository.get(this)
        scope.launch {
            val current = repo.settings.first()
            val target = !current.enabled
            if (target && !Settings.canDrawOverlays(this@PrivacyTileService)) {
                // No overlay permission — send user to grant it.
                qsTile?.apply {
                    state = Tile.STATE_UNAVAILABLE
                    updateTile()
                }
                return@launch
            }
            repo.setEnabled(target)
            if (target) {
                PrivacyOverlayService.start(this@PrivacyTileService)
            } else {
                PrivacyOverlayService.stop(this@PrivacyTileService)
            }
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
