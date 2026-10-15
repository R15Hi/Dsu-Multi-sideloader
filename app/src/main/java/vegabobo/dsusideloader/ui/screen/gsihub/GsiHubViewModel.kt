package vegabobo.dsusideloader.ui.screen.gsihub

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vegabobo.dsusideloader.core.gsirepo.GsiDownloader
import vegabobo.dsusideloader.core.gsirepo.GsiRepoFetcher
import vegabobo.dsusideloader.model.Session
import vegabobo.dsusideloader.model.gsirepo.GsiAsset
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadProgress
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadState
import vegabobo.dsusideloader.model.gsirepo.GsiRelease
import vegabobo.dsusideloader.model.gsirepo.GsiRepoSource

data class GsiHubUiState(
    val gsiRepos: List<Pair<GsiRepoSource, List<GsiRelease>>> = emptyList(),
    val isGsiReposLoading: Boolean = false,
    val gsiDownloads: Map<String, GsiDownloadProgress> = emptyMap(),
)

@HiltViewModel
class GsiHubViewModel @Inject constructor(
    val application: Application,
    val session: Session,
) : ViewModel() {

    private val tag = "GsiHubViewModel"
    private val _uiState = MutableStateFlow(GsiHubUiState())
    val uiState: StateFlow<GsiHubUiState> = _uiState.asStateFlow()

    private val repoFetcher = GsiRepoFetcher(application)
    private val gsiDownloader = GsiDownloader(application)
    private val activeDownloadJobs = ConcurrentHashMap<String, Job>()

    init {
        // Load offline cache on start - NO AUTO NETWORK REFRESH
        loadCachedGsiRepos()
    }

    /**
     * Loads GSI repo data from offline cache without making network requests.
     */
    private fun loadCachedGsiRepos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var cached = repoFetcher.loadCache()
                if (cached.isEmpty()) {
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
            val startTime = System.currentTimeMillis()
            try {
                val repos = repoFetcher.fetchAllSources()
                val downloads = scanExistingDownloads(repos)

                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 1200) {
                    kotlinx.coroutines.delay(1200 - elapsed)
                }

                _uiState.update {
                    it.copy(
                        gsiRepos = repos,
                        isGsiReposLoading = false,
                        gsiDownloads = it.gsiDownloads + downloads,
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch GSI repos", e)
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 1200) {
                    kotlinx.coroutines.delay(1200 - elapsed)
                }
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

    fun removeRepo(source: GsiRepoSource) {
        viewModelScope.launch(Dispatchers.IO) {
            val removed = repoFetcher.removeRepo(source)
            if (removed) {
                _uiState.update { state ->
                    val updatedRepos = state.gsiRepos.filterNot { (src, _) ->
                        src.id == source.id || src.url == source.url
                    }
                    state.copy(gsiRepos = updatedRepos)
                }
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

        viewModelScope.launch(Dispatchers.Main) {
            onNavigateToHome()
        }
    }
}
