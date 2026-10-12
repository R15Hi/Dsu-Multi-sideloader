package vegabobo.dsusideloader.ui.screen.multidsu

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vegabobo.dsusideloader.installer.multidsu.BackedUpDsu
import vegabobo.dsusideloader.installer.multidsu.MultiDsuManager

data class MultiDsuUiState(
    val hasRoot: Boolean = false,
    val hasActiveDsu: Boolean = false,
    val activeDsuName: String = "",
    val backedUpDsus: List<BackedUpDsu> = emptyList(),
    val isRefreshing: Boolean = false,
    val progress: Float = 0f,
    val progressAction: String = "",
    val isInspecting: Boolean = false,
    val inspectingDsu: vegabobo.dsusideloader.installer.multidsu.DsuMetadata? = null,
)

@HiltViewModel
class MultiDsuViewModel @Inject constructor(
    val application: Application,
    val session: vegabobo.dsusideloader.model.Session,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiDsuUiState(hasRoot = session.isRoot()))
    val uiState: StateFlow<MultiDsuUiState> = _uiState.asStateFlow()

    init {
        refreshState(showLoading = false)
    }

    fun refreshState(showLoading: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (showLoading) {
                    _uiState.update { it.copy(isRefreshing = true) }
                }
                val hasRoot = MultiDsuManager.hasRoot()
                if (hasRoot) {
                    val hasActive = MultiDsuManager.hasActiveDsu()
                    val activeName = if (hasActive) MultiDsuManager.getActiveDsuName() else ""
                    val backups = MultiDsuManager.listBackups()

                    _uiState.update {
                        it.copy(
                            hasRoot = true,
                            hasActiveDsu = hasActive,
                            activeDsuName = activeName,
                            backedUpDsus = backups,
                            isRefreshing = false,
                            progressAction = "",
                        )
                    }
                } else {
                    _uiState.update { it.copy(hasRoot = false, isRefreshing = false) }
                }
            } catch (e: Exception) {
                android.util.Log.e("MultiDsu", "Failed to refresh state", e)
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun restartApp() {
        val intent = application.packageManager.getLaunchIntentForPackage(application.packageName)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    fun inspectDsu(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isInspecting = true, inspectingDsu = null) }
            val metadata = MultiDsuManager.getBackupMetadata(name)
            if (metadata == null) {
                _uiState.update { it.copy(isInspecting = false, inspectingDsu = null) }
            } else {
                _uiState.update { it.copy(inspectingDsu = metadata) }
            }
        }
    }

    fun dismissInspector() {
        _uiState.update { it.copy(isInspecting = false, inspectingDsu = null) }
    }

    fun backupActiveDsu(name: String) {
        val safeName = name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true, progress = 0f, progressAction = "Backing up & Moving block map...") }
            for (i in 1..10) {
                kotlinx.coroutines.delay(50)
                _uiState.update { it.copy(progress = i / 10f) }
            }
            MultiDsuManager.backupCurrentDsu(safeName)
            _uiState.update { it.copy(isRefreshing = false, progressAction = "") }

            // Restart the app so HomeViewModel resets its "Installed" state properly
            restartApp()
        }
    }

    fun restoreDsu(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true, progress = 0f, progressAction = "Restoring & Updating boot state...") }
            for (i in 1..10) {
                kotlinx.coroutines.delay(50)
                _uiState.update { it.copy(progress = i / 10f) }
            }
            MultiDsuManager.restoreDsu(name)
            _uiState.update { it.copy(isRefreshing = false, progressAction = "") }

            // Restart the app so HomeViewModel detects the restored DSU, though usually they reboot immediately.
            restartApp()
        }
    }

    fun deleteDsu(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true, progress = 0f, progressAction = "Deleting $name (This may take a moment)...") }
            for (i in 1..10) {
                kotlinx.coroutines.delay(50)
                _uiState.update { it.copy(progress = i / 10f) }
            }
            MultiDsuManager.deleteBackedUpDsu(name)
            _uiState.update { it.copy(isRefreshing = false, progressAction = "") }
            refreshState()
        }
    }
}
