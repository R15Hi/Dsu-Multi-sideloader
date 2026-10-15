package vegabobo.dsusideloader.ui.screen.multidsu

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vegabobo.dsusideloader.core.gsirepo.GsiDownloader
import vegabobo.dsusideloader.core.gsirepo.GsiRepoFetcher
import vegabobo.dsusideloader.installer.multidsu.BackedUpDsu
import vegabobo.dsusideloader.installer.multidsu.MultiDsuManager
import vegabobo.dsusideloader.model.gsirepo.GsiAsset
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadProgress
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadState
import vegabobo.dsusideloader.model.gsirepo.GsiRelease
import vegabobo.dsusideloader.model.gsirepo.GsiRepoSource

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
    // Active GSI Repos
    val gsiRepos: List<Pair<GsiRepoSource, List<GsiRelease>>> = emptyList(),
    val isGsiReposLoading: Boolean = false,
    val gsiDownloads: Map<String, GsiDownloadProgress> = emptyMap(),
)

@HiltViewModel
class MultiDsuViewModel @Inject constructor(
    val application: Application,
    val session: vegabobo.dsusideloader.model.Session,
) : ViewModel() {

    private val tag = "MultiDsuViewModel"
    private val _uiState = MutableStateFlow(MultiDsuUiState(hasRoot = session.isRoot()))
    val uiState: StateFlow<MultiDsuUiState> = _uiState.asStateFlow()

    private val repoFetcher = GsiRepoFetcher(application)
    private val gsiDownloader = GsiDownloader(application)

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
                Log.e("MultiDsu", "Failed to refresh state", e)
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * Loads GSI repo data from offline cache without making network requests.
     */
    private fun loadCachedGsiRepos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var cached = repoFetcher.loadCache()
                if (cached.isEmpty()) {
                    // Populate default sources with empty releases so the user can tap Refresh Builds
                    cached = repoFetcher.getSources().map { Pair(it, emptyList()) }
                }

                val downloads = scanExistingDownloads(cached)
                _uiState.update {
                    it.copy(
                        gsiRepos = cached,
                        gsiDownloads = it.gsiDownloads + downloads,
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load cached GSI repos", e)
            }
        }
    }

    /**
     * Explicit network refresh triggered ONLY by user tapping "Refresh Builds".
     */
    fun fetchGsiRepos() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isGsiReposLoading = true) }
            try {
                val repos = repoFetcher.fetchAllSources()
                val downloads = scanExistingDownloads(repos)

                _uiState.update {
                    it.copy(
                        gsiRepos = repos,
                        isGsiReposLoading = false,
                        gsiDownloads = it.gsiDownloads + downloads,
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch GSI repos", e)
                _uiState.update { it.copy(isGsiReposLoading = false) }
            }
        }
    }

    private fun scanExistingDownloads(repos: List<Pair<GsiRepoSource, List<GsiRelease>>>): Map<String, GsiDownloadProgress> {
        val downloads = mutableMapOf<String, GsiDownloadProgress>()
        repos.forEach { (_, releases) ->
            releases.forEach { release ->
                release.assets.forEach { asset ->
                    if (gsiDownloader.isAssetFullyDownloaded(asset.name)) {
                        val file = gsiDownloader.getLocalFileForAsset(asset.name)
                        downloads[asset.name] = GsiDownloadProgress(
                            assetName = asset.name,
                            state = GsiDownloadState.DOWNLOADED,
                            progress = 100,
                            localUri = Uri.fromFile(file).toString(),
                            localFilePath = file.absolutePath,
                        )
                    }
                }
            }
        }
        return downloads
    }

    fun addCustomRepo(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val added = repoFetcher.addCustomRepo(url)
            if (added) {
                fetchGsiRepos()
            }
        }
    }

    fun removeCustomRepo(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val removed = repoFetcher.removeCustomRepo(url)
            if (removed) {
                fetchGsiRepos()
            }
        }
    }

    private val activeDownloadJobs = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.Job>()

    fun cancelDownload(asset: GsiAsset) {
        activeDownloadJobs.remove(asset.name)?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value.gsiDownloads[asset.name]
            val downloadId = current?.downloadId ?: -1L
            gsiDownloader.cancelDownload(downloadId, asset.name)

            _uiState.update { state ->
                val map = state.gsiDownloads.toMutableMap()
                map[asset.name] = GsiDownloadProgress(
                    assetName = asset.name,
                    state = GsiDownloadState.IDLE,
                    progress = 0,
                )
                state.copy(gsiDownloads = map)
            }
        }
    }

    fun downloadGsiAsset(asset: GsiAsset, onDownloaded: ((File) -> Unit)? = null) {
        val job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadId = gsiDownloader.enqueueDownload(asset)
                _uiState.update {
                    val map = it.gsiDownloads.toMutableMap()
                    map[asset.name] = GsiDownloadProgress(
                        assetName = asset.name,
                        downloadId = downloadId,
                        state = GsiDownloadState.DOWNLOADING,
                        progress = 0,
                    )
                    it.copy(gsiDownloads = map)
                }

                gsiDownloader.monitorDownload(downloadId, asset.name).collect { progress ->
                    _uiState.update { state ->
                        val map = state.gsiDownloads.toMutableMap()
                        map[asset.name] = progress
                        state.copy(gsiDownloads = map)
                    }

                    // Only invoke callback if download completed successfully and is fully verified
                    if (progress.state == GsiDownloadState.DOWNLOADED && gsiDownloader.isAssetFullyDownloaded(asset.name)) {
                        val file = gsiDownloader.getLocalFileForAsset(asset.name)
                        onDownloaded?.invoke(file)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to start download for ${asset.name}", e)
                _uiState.update {
                    val map = it.gsiDownloads.toMutableMap()
                    map[asset.name] = GsiDownloadProgress(
                        assetName = asset.name,
                        state = GsiDownloadState.FAILED,
                        error = e.localizedMessage ?: "Download failed",
                    )
                    it.copy(gsiDownloads = map)
                }
            } finally {
                activeDownloadJobs.remove(asset.name)
            }
        }
        activeDownloadJobs[asset.name] = job
    }

    fun installGsiAsset(asset: GsiAsset, onNavigateToHome: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (gsiDownloader.isAssetFullyDownloaded(asset.name)) {
                val file = gsiDownloader.getLocalFileForAsset(asset.name)
                applyGsiForInstallation(file, onNavigateToHome)
            } else {
                // Not downloaded yet: start download and only upon full completion install
                downloadGsiAsset(asset) { downloadedFile ->
                    if (gsiDownloader.isAssetFullyDownloaded(asset.name) && downloadedFile.exists()) {
                        applyGsiForInstallation(downloadedFile, onNavigateToHome)
                    }
                }
            }
        }
    }

    private fun applyGsiForInstallation(file: File, onNavigateToHome: () -> Unit) {
        if (!file.exists() || !gsiDownloader.isAssetFullyDownloaded(file.name)) {
            Log.w(tag, "Ignored incomplete/cancelled GSI installation: ${file.name}")
            return
        }

        val fileUri = Uri.fromFile(file)
        session.userSelection.selectedFileName = file.name
        session.userSelection.selectedFileUri = fileUri
        Log.d(tag, "Configured complete GSI for install: ${file.name}, Uri: $fileUri")

        // Switch to main thread and trigger navigation
        viewModelScope.launch(Dispatchers.Main) {
            onNavigateToHome()
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
