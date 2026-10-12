package vegabobo.dsusideloader.ui.screen.settings

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vegabobo.dsusideloader.core.BaseViewModel
import vegabobo.dsusideloader.model.Session
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.util.OperationMode
import vegabobo.dsusideloader.util.OperationModeUtils

@HiltViewModel
class SettingsViewModel @Inject constructor(
    override val dataStore: DataStore<Preferences>,
    private val session: Session,
    val application: Application,
) : BaseViewModel(dataStore) {

    private val tag = this.javaClass.simpleName

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun reloadPreferences() {
        uiState.value.preferences.forEach { entry ->
            viewModelScope.launch {
                val isEnabled = readBoolPref(entry.key, entry.value)
                togglePreference(entry.key, isEnabled)
            }
        }

        if (session.isRoot()) {
            _uiState.update { it.copy(isRoot = true) }
        }

        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val themeStyle = preferences[androidx.datastore.preferences.core.stringPreferencesKey(AppPrefs.THEME_STYLE)] ?: "dynamic"
                _uiState.update { it.copy(themeStyle = themeStyle) }
            }
        }
    }

    init {
        reloadPreferences()
    }

    fun togglePreference(preference: String, value: Boolean) {
        viewModelScope.launch {
            updateBoolPref(preference, value) {
                _uiState.update {
                    val cloneMap = hashMapOf<String, Boolean>()
                    cloneMap.putAll(uiState.value.preferences)
                    cloneMap[preference] = value
                    Log.d(tag, "preference: $preference, isEnabled: $value")
                    it.copy(preferences = cloneMap)
                }
            }
        }
    }

    fun updateThemeStyle(themeStyle: String) {
        viewModelScope.launch {
            updateStringPref(AppPrefs.THEME_STYLE, themeStyle) {
                _uiState.update {
                    it.copy(themeStyle = themeStyle)
                }
            }
        }
    }

    fun isAndroidQ(): Boolean = Build.VERSION.SDK_INT == 29

    fun updateSheetDisplay(sheet: DialogSheetState) {
        _uiState.update { it.copy(dialogSheetDisplay = sheet) }
    }

    fun checkOperationMode(): String {
        return OperationModeUtils.getOperationModeAsString(session.getOperationMode())
    }

    fun getOperationMode(): OperationMode {
        return session.getOperationMode()
    }

    fun checkDevOpt() {
        viewModelScope.launch {
            val isDevOptEnabled = readBoolPref(AppPrefs.DEVELOPER_OPTIONS)
            _uiState.update { it.copy(isDevOptEnabled = isDevOptEnabled) }
            if (isDevOptEnabled) {
                reloadPreferences()
            }
        }
    }

    fun cleanupDsu() {
        viewModelScope.launch {
            if (vegabobo.dsusideloader.installer.multidsu.MultiDsuManager.hasRoot()) {
                _uiState.update { it.copy(isCleaning = true) }
                com.topjohnwu.superuser.Shell.cmd(
                    "gsi_tool wipe", // Wipe before deleting to clear internal HAL state
                    "gsi_tool disable",
                    "find /data/gsi -mindepth 1 -maxdepth 1 ! -name dsu_backups -exec rm -rf {} +",
                    "find /metadata/gsi -mindepth 1 -maxdepth 1 ! -name dsu_backups -exec rm -rf {} +",
                    "rm -rf /data/gsid",
                    "rm -rf /data/local/tmp/gsid",
                    "rm -rf /data/local/tmp/dsu",
                    "pm clear com.android.dynsystem",
                    "stop dsu",
                    "start dsu",
                ).exec()

                kotlinx.coroutines.delay(2000) // Show progress bar for 2 seconds
                _uiState.update { it.copy(isCleaning = false) }

                // Restart app to refresh state and reconnect dead binder to gsid
                val intent = application.packageManager.getLaunchIntentForPackage(application.packageName)
                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                application.startActivity(intent)
                Runtime.getRuntime().exit(0)
            }
        }
    }
}
