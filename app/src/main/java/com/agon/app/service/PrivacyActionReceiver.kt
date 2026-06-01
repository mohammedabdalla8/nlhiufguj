package com.agon.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.agon.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Handles the notification quick-action toggle.
 */
class PrivacyActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE) return
        val repo = SettingsRepository.get(context)
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            try {
                val current = repo.settings.first()
                val target = !current.enabled
                if (target && !Settings.canDrawOverlays(context)) return@launch
                repo.setEnabled(target)
                if (target) PrivacyOverlayService.start(context)
                else PrivacyOverlayService.stop(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.agon.app.action.TOGGLE_PRIVACY"
    }
}
