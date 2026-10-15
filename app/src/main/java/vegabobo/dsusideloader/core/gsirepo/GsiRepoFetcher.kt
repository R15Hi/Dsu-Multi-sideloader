package vegabobo.dsusideloader.core.gsirepo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import vegabobo.dsusideloader.model.gsirepo.GsiAsset
import vegabobo.dsusideloader.model.gsirepo.GsiRelease
import vegabobo.dsusideloader.model.gsirepo.GsiRepoSource
import vegabobo.dsusideloader.model.gsirepo.GsiSourceType

class GsiRepoFetcher(private val context: Context) {

    private val tag = "GsiRepoFetcher"
    private val prefs: SharedPreferences =
        context.getSharedPreferences("active_gsi_repos_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_LAST_CHECK = "last_gsi_check_timestamp"
        const val PREF_SEEN_RELEASES = "seen_release_ids"
        const val PREF_CUSTOM_REPOS = "custom_gsi_repos_json"
        const val PREF_REMOVED_REPOS = "removed_gsi_repo_ids"
        const val CACHE_FILE_NAME = "gsi_repos_cache.json"

        val KNOWN_USER_GSI_REPOS = mapOf(
            "Doze-off" to listOf(
                "Doze-off/crdroid_gsi_treble",
                "Doze-off/lineage_treble",
                "Doze-off/PixelOS_GSI_treble",
                "Doze-off/Rising-revived_treble",
                "Doze-off/ProjectInfinity-X_gsi",
                "Doze-off/Derpfest_treble",
                "Doze-off/EvoX_treble",
                "Doze-off/Mist-OS_gsi",
            ),
        )

        val DEFAULT_REPOS = listOf(
            GsiRepoSource(
                id = "cawilliamson/treble_restlessos",
                name = "cawilliamson (RestlessOS)",
                url = "https://github.com/cawilliamson/treble_restlessos",
                type = GsiSourceType.GITHUB_REPO,
                avatarUrl = "https://github.com/cawilliamson.png?size=128",
                isCustom = false,
            ),
            GsiRepoSource(
                id = "Doze-off",
                name = "Doze-off",
                url = "https://github.com/Doze-off",
                type = GsiSourceType.GITHUB_USER,
                avatarUrl = "https://github.com/Doze-off.png?size=128",
                isCustom = false,
            ),
            GsiRepoSource(
                id = "google_official_gsi",
                name = "Google Official AOSP / GSI",
                url = "https://developer.android.com/topic/generic-system-image",
                type = GsiSourceType.GOOGLE_OFFICIAL,
                avatarUrl = "https://avatars.githubusercontent.com/u/1342004",
                isCustom = false,
            ),
        )
    }

    private fun getHttpRequest(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 12000
        conn.readTimeout = 12000
        conn.setRequestProperty("User-Agent", "Multi-DSU-Sideloader")
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")

        val responseCode = conn.responseCode
        if (responseCode in 200..299) {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            throw Exception("HTTP $responseCode: $error")
        }
    }

    /**
     * Loads previously saved cache from local storage without making network calls.
     */
    fun loadCache(): List<Pair<GsiRepoSource, List<GsiRelease>>> {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (!file.exists()) return emptyList()
        val removedSet = prefs.getStringSet(PREF_REMOVED_REPOS, emptySet()) ?: emptySet()
        return try {
            val json = file.readText()
            val root = JSONArray(json)
            val list = mutableListOf<Pair<GsiRepoSource, List<GsiRelease>>>()
            for (i in 0 until root.length()) {
                val item = root.getJSONObject(i)
                val srcObj = item.getJSONObject("source")
                val source = GsiRepoSource(
                    id = srcObj.getString("id"),
                    name = srcObj.getString("name"),
                    url = srcObj.getString("url"),
                    type = GsiSourceType.valueOf(srcObj.getString("type")),
                    avatarUrl = srcObj.optString("avatarUrl", ""),
                    isCustom = srcObj.optBoolean("isCustom", false),
                )

                if (removedSet.contains(source.id) || removedSet.contains(source.url)) {
                    continue
                }

                val relArr = item.getJSONArray("releases")
                val releases = mutableListOf<GsiRelease>()
                for (j in 0 until relArr.length()) {
                    val relObj = relArr.getJSONObject(j)
                    val astArr = relObj.getJSONArray("assets")
                    val assets = mutableListOf<GsiAsset>()
                    for (k in 0 until astArr.length()) {
                        val astObj = astArr.getJSONObject(k)
                        assets.add(
                            GsiAsset(
                                id = astObj.getString("id"),
                                name = astObj.getString("name"),
                                downloadUrl = astObj.getString("downloadUrl"),
                                sizeBytes = astObj.optLong("sizeBytes", 0L),
                                formattedSize = astObj.optString("formattedSize", ""),
                                architecture = astObj.optString("architecture", "arm64"),
                                variant = astObj.optString("variant", "Vanilla"),
                                releaseDate = astObj.optString("releaseDate", ""),
                                publishedTimestamp = astObj.optLong("publishedTimestamp", 0L),
                                changelog = astObj.optString("changelog", ""),
                                isNewOrUpdated = astObj.optBoolean("isNewOrUpdated", false),
                            ),
                        )
                    }
                    releases.add(
                        GsiRelease(
                            id = relObj.getString("id"),
                            repoId = relObj.getString("repoId"),
                            repoName = relObj.getString("repoName"),
                            romName = relObj.optString("romName", ""),
                            title = relObj.getString("title"),
                            tagName = relObj.getString("tagName"),
                            publishedDate = relObj.getString("publishedDate"),
                            publishedTimestamp = relObj.optLong("publishedTimestamp", 0L),
                            androidVersion = relObj.getInt("androidVersion"),
                            androidVersionLabel = relObj.getString("androidVersionLabel"),
                            changelog = relObj.optString("changelog", ""),
                            isNewOrUpdated = relObj.optBoolean("isNewOrUpdated", false),
                            assets = assets,
                        ),
                    )
                }
                // Sort releases latest to oldest
                list.add(Pair(source, releases.sortedByDescending { it.publishedTimestamp }))
            }
            // Sort sources by latest release date
            list.sortedByDescending { (_, rels) -> rels.maxOfOrNull { it.publishedTimestamp } ?: 0L }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load cache", e)
            emptyList()
        }
    }

    private fun saveCache(data: List<Pair<GsiRepoSource, List<GsiRelease>>>) {
        try {
            val root = JSONArray()
            for ((source, releases) in data) {
                val item = JSONObject()
                val srcObj = JSONObject().apply {
                    put("id", source.id)
                    put("name", source.name)
                    put("url", source.url)
                    put("type", source.type.name)
                    put("avatarUrl", source.avatarUrl)
                    put("isCustom", source.isCustom)
                }
                item.put("source", srcObj)

                val relArr = JSONArray()
                for (rel in releases) {
                    val relObj = JSONObject().apply {
                        put("id", rel.id)
                        put("repoId", rel.repoId)
                        put("repoName", rel.repoName)
                        put("romName", rel.romName)
                        put("title", rel.title)
                        put("tagName", rel.tagName)
                        put("publishedDate", rel.publishedDate)
                        put("publishedTimestamp", rel.publishedTimestamp)
                        put("androidVersion", rel.androidVersion)
                        put("androidVersionLabel", rel.androidVersionLabel)
                        put("changelog", rel.changelog)
                        put("isNewOrUpdated", rel.isNewOrUpdated)

                        val astArr = JSONArray()
                        for (ast in rel.assets) {
                            val astObj = JSONObject().apply {
                                put("id", ast.id)
                                put("name", ast.name)
                                put("downloadUrl", ast.downloadUrl)
                                put("sizeBytes", ast.sizeBytes)
                                put("formattedSize", ast.formattedSize)
                                put("architecture", ast.architecture)
                                put("variant", ast.variant)
                                put("releaseDate", ast.releaseDate)
                                put("publishedTimestamp", ast.publishedTimestamp)
                                put("changelog", ast.changelog)
                                put("isNewOrUpdated", ast.isNewOrUpdated)
                            }
                            astArr.put(astObj)
                        }
                        put("assets", astArr)
                    }
                    relArr.put(relObj)
                }
                item.put("releases", relArr)
                root.put(item)
            }
            File(context.filesDir, CACHE_FILE_NAME).writeText(root.toString())
        } catch (e: Exception) {
            Log.e(tag, "Failed to write cache file", e)
        }
    }

    suspend fun fetchAllSources(): List<Pair<GsiRepoSource, List<GsiRelease>>> = withContext(Dispatchers.IO) {
        val allSources = getSources()
        val lastCheckTime = prefs.getLong(PREF_LAST_CHECK, 0L)
        val seenReleases = prefs.getStringSet(PREF_SEEN_RELEASES, emptySet())?.toMutableSet() ?: mutableSetOf()
        val now = System.currentTimeMillis()

        // Fallback map of previously cached releases so temporary network errors/rate limits never wipe out builds
        val cachedMap = try {
            loadCache().associate { it.first.url to it.second }
        } catch (e: Exception) {
            emptyMap()
        }

        // Fetch all sources concurrently so every repo gets full time without blocking
        val deferredList = allSources.map { source ->
            async(Dispatchers.IO) {
                try {
                    val releases = when (source.type) {
                        GsiSourceType.GITHUB_REPO -> fetchGitHubRepoReleases(source, lastCheckTime, seenReleases)
                        GsiSourceType.GITHUB_USER -> fetchGitHubUserReleases(source, lastCheckTime, seenReleases)
                        GsiSourceType.GOOGLE_OFFICIAL -> fetchGoogleOfficialGsi(source, lastCheckTime, seenReleases)
                        GsiSourceType.CUSTOM_URL -> fetchCustomSource(source, lastCheckTime, seenReleases)
                    }

                    if (releases.isNotEmpty()) {
                        val filtered = filterLatestPerRomAndVersion(releases).sortedByDescending { it.publishedTimestamp }
                        Pair(source, filtered)
                    } else {
                        Pair(source, cachedMap[source.url] ?: emptyList())
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error fetching from source ${source.name}", e)
                    Pair(source, cachedMap[source.url] ?: emptyList())
                }
            }
        }

        val results = deferredList.awaitAll().toMutableList()

        // Sort maintainer sources latest to oldest based on newest release timestamp
        results.sortByDescending { (_, rels) -> rels.maxOfOrNull { it.publishedTimestamp } ?: 0L }

        // Save to persistent cache so future launches do not auto-refresh
        saveCache(results)

        // Save updated seen releases and timestamp
        prefs.edit()
            .putLong(PREF_LAST_CHECK, now)
            .putStringSet(PREF_SEEN_RELEASES, seenReleases)
            .apply()

        results
    }

    /**
     * Filters a list of GSI releases so that for each unique ROM (e.g. crDroid, LineageOS, RestlessOS)
     * and each Android version (e.g. 15, 14, 13), ONLY the latest release is kept.
     */
    fun filterLatestPerRomAndVersion(releases: List<GsiRelease>): List<GsiRelease> {
        val map = linkedMapOf<Pair<String, Int>, GsiRelease>()
        // Sort oldest to newest so newest release overwrites previous ones for the same key
        releases.sortedBy { it.publishedTimestamp }.forEach { rel ->
            val key = Pair(rel.romName.lowercase(Locale.ROOT), rel.androidVersion)
            map[key] = rel
        }
        // Return sorted by newest release first (latest to oldest order)
        return map.values.sortedByDescending { it.publishedTimestamp }
    }

    private fun fetchGitHubRepoReleases(
        source: GsiRepoSource,
        lastCheckTime: Long,
        seenReleases: MutableSet<String>,
    ): List<GsiRelease> {
        val repoPath = extractRepoPath(source.url) ?: source.id
        try {
            val apiUrl = "https://api.github.com/repos/$repoPath/releases?per_page=30"
            val response = getHttpRequest(apiUrl)
            val jsonArray = JSONArray(response)

            val releases = mutableListOf<GsiRelease>()
            for (i in 0 until jsonArray.length()) {
                val relObj = jsonArray.getJSONObject(i)
                val release = parseGitHubRelease(source, relObj, lastCheckTime, seenReleases)
                if (release != null && release.assets.isNotEmpty()) {
                    releases.add(release)
                }
            }
            if (releases.isNotEmpty()) {
                return releases
            }
        } catch (e: Exception) {
            Log.w(tag, "GitHub API fetch failed for $repoPath (${e.message}), falling back to Atom feed...")
        }

        // 100% Rate-limit immune fallback: Atom feed + expanded_assets HTML
        return fetchReleasesViaAtomFeed(source, repoPath, lastCheckTime, seenReleases)
    }

    private fun fetchReleasesViaAtomFeed(
        source: GsiRepoSource,
        repoPath: String,
        lastCheckTime: Long,
        seenReleases: MutableSet<String>,
    ): List<GsiRelease> {
        return try {
            val atomUrl = "https://github.com/$repoPath/releases.atom"
            val atomXml = getHttpRequest(atomUrl)
            parseAtomFeedReleases(source, repoPath, atomXml, lastCheckTime, seenReleases)
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch atom feed for $repoPath: ${e.message}")
            emptyList()
        }
    }

    private fun parseAtomFeedReleases(
        source: GsiRepoSource,
        repoPath: String,
        atomXml: String,
        lastCheckTime: Long,
        seenReleases: MutableSet<String>,
    ): List<GsiRelease> {
        val releases = mutableListOf<GsiRelease>()
        val entryRegex = Regex("<entry>([\\s\\S]*?)</entry>")
        val titleRegex = Regex("<title>([\\s\\S]*?)</title>")
        val updatedRegex = Regex("<updated>([\\s\\S]*?)</updated>")
        val tagRegex = Regex("releases/tag/([^\"'\\s<]+)")
        val contentRegex = Regex("<content type=\"html\">([\\s\\S]*?)</content>")

        val entries = entryRegex.findAll(atomXml).toList()
        for (match in entries.take(4)) {
            val entryXml = match.groupValues[1]
            val title = titleRegex.find(entryXml)?.groupValues?.get(1)?.trim() ?: ""
            val updatedStr = updatedRegex.find(entryXml)?.groupValues?.get(1)?.trim() ?: ""
            val tagName = tagRegex.find(entryXml)?.groupValues?.get(1)?.trim() ?: ""
            val rawContent = contentRegex.find(entryXml)?.groupValues?.get(1)?.trim() ?: ""

            if (tagName.isBlank()) continue

            val publishedTimestamp = parseIsoTimestamp(updatedStr)
            val id = "${repoPath}_$tagName"
            val isPreviouslySeen = seenReleases.contains(id)
            val isRecentlyPublished = (System.currentTimeMillis() - publishedTimestamp) < (7L * 24 * 3600 * 1000)
            val isNewOrUpdated = (!isPreviouslySeen && lastCheckTime > 0) || (isRecentlyPublished && !isPreviouslySeen)
            seenReleases.add(id)

            val cleanChangelog = cleanHtmlContent(rawContent)
            val (androidVer, androidLabel) = GsiClassifier.parseAndroidVersion(title, tagName)
            val rom = GsiClassifier.extractRomName(source.name, title)
            val formattedDate = formatDateString(publishedTimestamp)

            val assets = fetchExpandedAssets(repoPath, tagName, formattedDate, publishedTimestamp, cleanChangelog, isNewOrUpdated)
            if (assets.isNotEmpty()) {
                releases.add(
                    GsiRelease(
                        id = id,
                        repoId = source.id,
                        repoName = source.name,
                        romName = rom,
                        title = title.ifBlank { "Release $tagName" },
                        tagName = tagName,
                        publishedDate = formattedDate,
                        publishedTimestamp = publishedTimestamp,
                        androidVersion = androidVer,
                        androidVersionLabel = androidLabel,
                        changelog = cleanChangelog,
                        isNewOrUpdated = isNewOrUpdated,
                        assets = assets,
                    ),
                )
            }
        }
        return releases
    }

    private fun fetchExpandedAssets(
        repoPath: String,
        tagName: String,
        formattedDate: String,
        publishedTimestamp: Long,
        changelog: String,
        isNewOrUpdated: Boolean,
    ): List<GsiAsset> {
        val assets = mutableListOf<GsiAsset>()
        try {
            val assetsUrl = "https://github.com/$repoPath/releases/expanded_assets/$tagName"
            val html = getHttpRequest(assetsUrl)
            val assetRegex = Regex(
                "href=\"(/[^\"\\s]+/releases/download/[^\"\\s]+/([^\"\\s]+))\"[\\s\\S]*?<span[^>]*class=\"[^\"]*color-fg-muted[^\"]*\"[^>]*>\\s*([0-9.]+\\s*[KMGT]?B)\\s*</span>",
            )
            for (match in assetRegex.findAll(html)) {
                val fullPath = match.groupValues[1]
                val name = match.groupValues[2]
                val sizeStr = match.groupValues[3].trim()

                if (!GsiClassifier.isGsiAsset(name)) continue

                val downloadUrl = if (fullPath.startsWith("http")) fullPath else "https://github.com$fullPath"
                val arch = GsiClassifier.parseArchitecture(name)
                val variant = GsiClassifier.parseVariant(name)

                assets.add(
                    GsiAsset(
                        id = "${tagName}_$name",
                        name = name,
                        downloadUrl = downloadUrl,
                        sizeBytes = parseFormattedSizeToBytes(sizeStr),
                        formattedSize = sizeStr,
                        architecture = arch,
                        variant = variant,
                        releaseDate = formattedDate,
                        publishedTimestamp = publishedTimestamp,
                        changelog = changelog,
                        isNewOrUpdated = isNewOrUpdated,
                    ),
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch expanded assets for $repoPath/$tagName: ${e.message}")
        }
        return assets
    }

    private fun cleanHtmlContent(html: String): String {
        return html
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</li>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "• ")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n### ")
            .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "")
            .trim()
    }

    private fun parseFormattedSizeToBytes(formatted: String): Long {
        return try {
            val clean = formatted.trim().uppercase(Locale.ROOT)
            val number = clean.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: return 0L
            when {
                clean.contains("GB") -> (number * 1024 * 1024 * 1024).toLong()
                clean.contains("MB") -> (number * 1024 * 1024).toLong()
                clean.contains("KB") -> (number * 1024).toLong()
                else -> number.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    private suspend fun fetchGitHubUserReleases(
        source: GsiRepoSource,
        lastCheckTime: Long,
        seenReleases: MutableSet<String>,
    ): List<GsiRelease> = coroutineScope {
        val username = extractUsername(source.url) ?: source.id
        val targetRepos = mutableSetOf<String>()

        // Add pre-configured known GSI repositories for the user
        KNOWN_USER_GSI_REPOS[username]?.let { targetRepos.addAll(it) }

        // Optionally discover additional GSI repos via GitHub API if available
        try {
            val apiUrl = "https://api.github.com/users/$username/repos?sort=updated&per_page=30"
            val response = getHttpRequest(apiUrl)
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val repoObj = jsonArray.getJSONObject(i)
                val repoName = repoObj.optString("name", "")
                val fullName = repoObj.optString("full_name", "$username/$repoName")
                val isGsiRelated = repoName.contains("treble", ignoreCase = true) ||
                    repoName.contains("gsi", ignoreCase = true) ||
                    repoName.contains("device", ignoreCase = true) ||
                    repoName.contains("lineage", ignoreCase = true) ||
                    repoName.contains("croid", ignoreCase = true) ||
                    repoName.contains("rising", ignoreCase = true) ||
                    repoName.contains("pixel", ignoreCase = true) ||
                    repoName.contains("aosp", ignoreCase = true)

                if (isGsiRelated) {
                    targetRepos.add(fullName)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "GitHub API user repos fetch skipped/rate-limited for $username: ${e.message}")
        }

        // If no repos found or known, fallback to username
        if (targetRepos.isEmpty()) {
            targetRepos.add("$username/treble")
        }

        // Fetch each repository concurrently with full rate-limit resilience
        val deferredList = targetRepos.map { repoPath ->
            async(Dispatchers.IO) {
                val subSource = source.copy(
                    id = repoPath,
                    name = repoPath.substringAfter("/"),
                    url = "https://github.com/$repoPath",
                )
                fetchGitHubRepoReleases(subSource, lastCheckTime, seenReleases)
            }
        }

        val allUserReleases = deferredList.awaitAll().flatten()
        allUserReleases
    }

    private fun parseGitHubRelease(
        source: GsiRepoSource,
        obj: JSONObject,
        lastCheckTime: Long,
        seenReleases: MutableSet<String>,
    ): GsiRelease? {
        val id = obj.optLong("id", 0L).toString()
        val title = obj.optString("name", "").ifBlank { obj.optString("tag_name", "Release") }
        val tagName = obj.optString("tag_name", "")
        val publishedAtStr = obj.optString("published_at", "")

        val publishedTimestamp = parseIsoTimestamp(publishedAtStr)
        val isPreviouslySeen = seenReleases.contains(id)
        val isRecentlyPublished = (System.currentTimeMillis() - publishedTimestamp) < (7L * 24 * 3600 * 1000)
        val isNewOrUpdated = (!isPreviouslySeen && lastCheckTime > 0) || (isRecentlyPublished && !isPreviouslySeen)

        seenReleases.add(id)

        val (androidVer, androidLabel) = GsiClassifier.parseAndroidVersion(title, tagName)
        val rom = GsiClassifier.extractRomName(source.name, title)
        val formattedDate = formatDateString(publishedTimestamp)
        val changelog = obj.optString("body", "").trim()

        val assetsArray = obj.optJSONArray("assets") ?: JSONArray()
        val assetsList = mutableListOf<GsiAsset>()

        for (k in 0 until assetsArray.length()) {
            val assetObj = assetsArray.getJSONObject(k)
            val name = assetObj.optString("name", "")
            if (!GsiClassifier.isGsiAsset(name)) continue

            val downloadUrl = assetObj.optString("browser_download_url", "")
            val size = assetObj.optLong("size", 0L)
            val arch = GsiClassifier.parseArchitecture(name)
            val variant = GsiClassifier.parseVariant(name)

            assetsList.add(
                GsiAsset(
                    id = assetObj.optLong("id", 0L).toString(),
                    name = name,
                    downloadUrl = downloadUrl,
                    sizeBytes = size,
                    formattedSize = GsiClassifier.formatFileSize(size),
                    architecture = arch,
                    variant = variant,
                    releaseDate = formattedDate,
                    publishedTimestamp = publishedTimestamp,
                    changelog = changelog,
                    isNewOrUpdated = isNewOrUpdated,
                ),
            )
        }

        return GsiRelease(
            id = id,
            repoId = source.id,
            repoName = source.name,
            romName = rom,
            title = title,
            tagName = tagName,
            publishedDate = formattedDate,
            publishedTimestamp = publishedTimestamp,
            androidVersion = androidVer,
            androidVersionLabel = androidLabel,
            changelog = changelog,
            isNewOrUpdated = isNewOrUpdated,
            assets = assetsList,
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun fetchGoogleOfficialGsi(
        source: GsiRepoSource,
        lastCheckTime: Long,
        seenReleases: MutableSet<String>,
    ): List<GsiRelease> {
        val releases = mutableListOf<GsiRelease>()
        val googleChangelog = "Official Google AOSP Generic System Image (GSI) built directly from the Android Open Source Project. Certified for CTS / VTS compliance on Treble-compliant devices."
        val timeNow = System.currentTimeMillis()
        val time14 = timeNow - (45L * 24 * 3600 * 1000)
        val time13 = timeNow - (180L * 24 * 3600 * 1000)

        val googleImages = listOf(
            Triple(
                15,
                "Android 15 (Vanilla Ice Cream) Official AOSP",
                listOf(
                    GsiAsset(
                        id = "goog_15_arm64_gms",
                        name = "gsi_gms_arm64-exp-VanillaIceCream.zip",
                        downloadUrl = "https://dl.google.com/developers/android/vic/images/gsi/gsi_gms_arm64-exp-VanillaIceCream.zip",
                        sizeBytes = 1850000000L,
                        formattedSize = "1.85 GB",
                        architecture = "ARM64",
                        variant = "GApps (Google GMS)",
                        releaseDate = formatDateString(timeNow),
                        publishedTimestamp = timeNow,
                        changelog = googleChangelog,
                        isNewOrUpdated = true,
                    ),
                    GsiAsset(
                        id = "goog_15_arm64_aosp",
                        name = "aosp_arm64-exp-VanillaIceCream.zip",
                        downloadUrl = "https://dl.google.com/developers/android/vic/images/gsi/aosp_arm64-exp-VanillaIceCream.zip",
                        sizeBytes = 1350000000L,
                        formattedSize = "1.35 GB",
                        architecture = "ARM64",
                        variant = "Vanilla (Pure AOSP)",
                        releaseDate = formatDateString(timeNow),
                        publishedTimestamp = timeNow,
                        changelog = googleChangelog,
                        isNewOrUpdated = true,
                    ),
                    GsiAsset(
                        id = "goog_15_x86_64",
                        name = "aosp_x86_64-exp-VanillaIceCream.zip",
                        downloadUrl = "https://dl.google.com/developers/android/vic/images/gsi/aosp_x86_64-exp-VanillaIceCream.zip",
                        sizeBytes = 1420000000L,
                        formattedSize = "1.42 GB",
                        architecture = "x86_64",
                        variant = "Vanilla (Pure AOSP)",
                        releaseDate = formatDateString(timeNow),
                        publishedTimestamp = timeNow,
                        changelog = googleChangelog,
                        isNewOrUpdated = true,
                    ),
                ),
            ),
            Triple(
                14,
                "Android 14 (Upside Down Cake) Official QPR GSI",
                listOf(
                    GsiAsset(
                        id = "goog_14_arm64_gms",
                        name = "gsi_gms_arm64-exp-UpsideDownCake.zip",
                        downloadUrl = "https://dl.google.com/developers/android/udc/images/gsi/gsi_gms_arm64-exp-UpsideDownCake.zip",
                        sizeBytes = 1780000000L,
                        formattedSize = "1.78 GB",
                        architecture = "ARM64",
                        variant = "GApps (Google GMS)",
                        releaseDate = formatDateString(time14),
                        publishedTimestamp = time14,
                        changelog = googleChangelog,
                        isNewOrUpdated = false,
                    ),
                    GsiAsset(
                        id = "goog_14_arm64_aosp",
                        name = "aosp_arm64-exp-UpsideDownCake.zip",
                        downloadUrl = "https://dl.google.com/developers/android/udc/images/gsi/aosp_arm64-exp-UpsideDownCake.zip",
                        sizeBytes = 1290000000L,
                        formattedSize = "1.29 GB",
                        architecture = "ARM64",
                        variant = "Vanilla (Pure AOSP)",
                        releaseDate = formatDateString(time14),
                        publishedTimestamp = time14,
                        changelog = googleChangelog,
                        isNewOrUpdated = false,
                    ),
                ),
            ),
            Triple(
                13,
                "Android 13 (Tiramisu) Official GSI",
                listOf(
                    GsiAsset(
                        id = "goog_13_arm64_gms",
                        name = "gsi_gms_arm64-exp-Tiramisu.zip",
                        downloadUrl = "https://dl.google.com/developers/android/tm/images/gsi/gsi_gms_arm64-exp-Tiramisu.zip",
                        sizeBytes = 1650000000L,
                        formattedSize = "1.65 GB",
                        architecture = "ARM64",
                        variant = "GApps (Google GMS)",
                        releaseDate = formatDateString(time13),
                        publishedTimestamp = time13,
                        changelog = googleChangelog,
                        isNewOrUpdated = false,
                    ),
                    GsiAsset(
                        id = "goog_13_arm64_aosp",
                        name = "aosp_arm64-exp-Tiramisu.zip",
                        downloadUrl = "https://dl.google.com/developers/android/tm/images/gsi/aosp_arm64-exp-Tiramisu.zip",
                        sizeBytes = 1180000000L,
                        formattedSize = "1.18 GB",
                        architecture = "ARM64",
                        variant = "Vanilla (Pure AOSP)",
                        releaseDate = formatDateString(time13),
                        publishedTimestamp = time13,
                        changelog = googleChangelog,
                        isNewOrUpdated = false,
                    ),
                ),
            ),
        )

        for (item in googleImages) {
            val relId = "google_official_${item.first}"
            val isNewOrUpdated = item.first >= 15
            releases.add(
                GsiRelease(
                    id = relId,
                    repoId = source.id,
                    repoName = source.name,
                    romName = "AOSP Official",
                    title = item.second,
                    tagName = "Android ${item.first}",
                    publishedDate = item.third.firstOrNull()?.releaseDate ?: "Official Build",
                    publishedTimestamp = item.third.firstOrNull()?.publishedTimestamp ?: System.currentTimeMillis(),
                    androidVersion = item.first,
                    androidVersionLabel = "Android ${item.first}",
                    changelog = googleChangelog,
                    isNewOrUpdated = isNewOrUpdated,
                    assets = item.third,
                ),
            )
        }
        return releases
    }

    private suspend fun fetchCustomSource(
        source: GsiRepoSource,
        lastCheckTime: Long,
        seenReleases: MutableSet<String>,
    ): List<GsiRelease> {
        return if (source.url.contains("github.com/")) {
            val parts = source.url.substringAfter("github.com/").trim('/').split('/')
            if (parts.size >= 2) {
                fetchGitHubRepoReleases(source, lastCheckTime, seenReleases)
            } else {
                fetchGitHubUserReleases(source, lastCheckTime, seenReleases)
            }
        } else {
            emptyList()
        }
    }

    fun getSources(): List<GsiRepoSource> {
        val removedSet = prefs.getStringSet(PREF_REMOVED_REPOS, emptySet()) ?: emptySet()
        val list = mutableListOf<GsiRepoSource>()
        for (repo in DEFAULT_REPOS) {
            if (!removedSet.contains(repo.id) && !removedSet.contains(repo.url)) {
                list.add(repo)
            }
        }

        val json = prefs.getString(PREF_CUSTOM_REPOS, null)
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val url = obj.getString("url")
                    if (removedSet.contains(id) || removedSet.contains(url)) continue

                    val derivedAvatar = if (url.contains("github.com/")) {
                        val owner = url.substringAfter("github.com/").trim('/').split('/')[0]
                        "https://github.com/$owner.png?size=128"
                    } else {
                        ""
                    }

                    list.add(
                        GsiRepoSource(
                            id = id,
                            name = obj.getString("name"),
                            url = url,
                            type = GsiSourceType.valueOf(obj.getString("type")),
                            avatarUrl = obj.optString("avatarUrl", derivedAvatar).ifBlank { derivedAvatar },
                            isCustom = true,
                        ),
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to parse custom repos", e)
            }
        }
        return list
    }

    fun addCustomRepo(urlInput: String): Boolean {
        val cleanUrl = urlInput.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            return false
        }

        // Un-remove if previously removed
        val removedSet = prefs.getStringSet(PREF_REMOVED_REPOS, emptySet())?.toMutableSet() ?: mutableSetOf()
        removedSet.remove(cleanUrl)
        DEFAULT_REPOS.find { it.url.equals(cleanUrl, ignoreCase = true) || it.id.equals(cleanUrl, ignoreCase = true) }?.let {
            removedSet.remove(it.id)
            removedSet.remove(it.url)
        }
        prefs.edit().putStringSet(PREF_REMOVED_REPOS, removedSet).apply()

        val type = if (cleanUrl.contains("github.com/")) {
            val path = cleanUrl.substringAfter("github.com/").trim('/')
            if (path.contains("/")) GsiSourceType.GITHUB_REPO else GsiSourceType.GITHUB_USER
        } else {
            GsiSourceType.CUSTOM_URL
        }

        val name = if (cleanUrl.contains("github.com/")) {
            cleanUrl.substringAfter("github.com/").trim('/')
        } else {
            cleanUrl.substringAfter("://").substringBefore("/")
        }

        val avatar = if (cleanUrl.contains("github.com/")) {
            val owner = cleanUrl.substringAfter("github.com/").trim('/').split('/')[0]
            "https://github.com/$owner.png?size=128"
        } else {
            ""
        }

        val source = GsiRepoSource(
            id = cleanUrl,
            name = name,
            url = cleanUrl,
            type = type,
            avatarUrl = avatar,
            isCustom = true,
        )

        val current = getSources().filter { it.isCustom }.toMutableList()
        if (current.none { it.url.equals(cleanUrl, ignoreCase = true) }) {
            current.add(source)
            saveCustomRepos(current)
            return true
        }
        return false
    }

    fun removeRepo(source: GsiRepoSource): Boolean {
        if (source.isCustom) {
            removeCustomRepo(source.url)
        }
        val removedSet = prefs.getStringSet(PREF_REMOVED_REPOS, emptySet())?.toMutableSet() ?: mutableSetOf()
        removedSet.add(source.id)
        removedSet.add(source.url)
        prefs.edit().putStringSet(PREF_REMOVED_REPOS, removedSet).apply()

        // Also update local cache file so it immediately disappears without needing network refetch
        try {
            val cached = loadCache().filterNot { (src, _) ->
                src.id == source.id || src.url == source.url
            }
            saveCache(cached)
        } catch (e: Exception) {
            Log.e(tag, "Failed to update cache on removeRepo", e)
        }
        return true
    }

    fun removeCustomRepo(url: String): Boolean {
        val current = getSources().filter { it.isCustom }.toMutableList()
        val removed = current.removeAll { it.url.equals(url, ignoreCase = true) }
        if (removed) {
            saveCustomRepos(current)
            return true
        }
        return false
    }

    private fun saveCustomRepos(list: List<GsiRepoSource>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("url", item.url)
            obj.put("type", item.type.name)
            obj.put("avatarUrl", item.avatarUrl)
            obj.put("isCustom", true)
            array.put(obj)
        }
        prefs.edit().putString(PREF_CUSTOM_REPOS, array.toString()).apply()
    }

    private fun extractRepoPath(url: String): String? {
        if (!url.contains("github.com/")) return null
        val parts = url.substringAfter("github.com/").trim('/').split('/')
        return if (parts.size >= 2) "${parts[0]}/${parts[1]}" else null
    }

    private fun extractUsername(url: String): String? {
        if (!url.contains("github.com/")) return null
        val parts = url.substringAfter("github.com/").trim('/').split('/')
        return if (parts.isNotEmpty()) parts[0] else null
    }

    private fun parseIsoTimestamp(isoString: String): Long {
        if (isoString.isBlank()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(isoString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun formatDateString(timestamp: Long): String {
        if (timestamp <= 0L) return "Recent"
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(timestamp)
    }
}
