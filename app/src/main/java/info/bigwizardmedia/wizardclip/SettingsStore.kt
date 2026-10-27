package info.bigwizardmedia.wizardclip

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tiny SharedPreferences-backed settings store. No DataStore dependency,
 * no network, nothing leaves the device.
 */
class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _capturePaused = MutableStateFlow(prefs.getBoolean(KEY_PAUSE_CAPTURE, false))
    val capturePaused: StateFlow<Boolean> = _capturePaused.asStateFlow()

    fun isCapturePaused(): Boolean = prefs.getBoolean(KEY_PAUSE_CAPTURE, false)

    fun setCapturePaused(paused: Boolean) {
        prefs.edit().putBoolean(KEY_PAUSE_CAPTURE, paused).apply()
        _capturePaused.value = paused
    }

    fun isSeedDone(): Boolean = prefs.getBoolean(KEY_SEED_DONE, false)

    fun setSeedDone() {
        prefs.edit().putBoolean(KEY_SEED_DONE, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "wizardclip_settings"
        private const val KEY_PAUSE_CAPTURE = "capture_paused"
        private const val KEY_SEED_DONE = "seed_done"
    }
}
