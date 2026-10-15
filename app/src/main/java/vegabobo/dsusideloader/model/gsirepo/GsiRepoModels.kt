package vegabobo.dsusideloader.model.gsirepo

import kotlinx.serialization.Serializable

@Serializable
enum class GsiSourceType {
    GITHUB_REPO,
    GITHUB_USER,
    GOOGLE_OFFICIAL,
    CUSTOM_URL,
}

@Serializable
data class GsiRepoSource(
    val id: String,
    val name: String,
    val url: String,
    val type: GsiSourceType = GsiSourceType.GITHUB_REPO,
    val avatarUrl: String = "",
    val isCustom: Boolean = false,
)

@Serializable
data class GsiAsset(
    val id: String,
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long = 0L,
    val formattedSize: String = "",
    val architecture: String = "arm64",
    val variant: String = "Vanilla",
    val releaseDate: String = "",
    val publishedTimestamp: Long = 0L,
    val changelog: String = "",
    val isNewOrUpdated: Boolean = false,
)

@Serializable
data class GsiRelease(
    val id: String,
    val repoId: String,
    val repoName: String,
    val romName: String = "",
    val title: String,
    val tagName: String,
    val publishedDate: String,
    val publishedTimestamp: Long = 0L,
    val androidVersion: Int = 0,
    val androidVersionLabel: String = "Generic GSI",
    val changelog: String = "",
    val isNewOrUpdated: Boolean = false,
    val assets: List<GsiAsset> = emptyList(),
)

enum class GsiDownloadState {
    IDLE,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
}

data class GsiDownloadProgress(
    val assetName: String,
    val downloadId: Long = -1L,
    val state: GsiDownloadState = GsiDownloadState.IDLE,
    val progress: Int = 0,
    val localUri: String = "",
    val localFilePath: String = "",
    val error: String = "",
)
