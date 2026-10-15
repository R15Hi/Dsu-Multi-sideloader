package vegabobo.dsusideloader.ui.screen.multidsu.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.model.gsirepo.GsiAsset
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadProgress
import vegabobo.dsusideloader.model.gsirepo.GsiRelease
import vegabobo.dsusideloader.model.gsirepo.GsiRepoSource
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGsiReposSection(
    repoData: List<Pair<GsiRepoSource, List<GsiRelease>>>,
    downloadsMap: Map<String, GsiDownloadProgress>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onAddRepo: (String) -> Unit,
    onDeleteCustomRepo: (String) -> Unit,
    onDownloadAsset: (GsiAsset) -> Unit,
    onCancelAsset: (GsiAsset) -> Unit,
    onInstallAsset: (GsiAsset) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Instant real-time search filtering
    val filteredRepoData = remember(repoData, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) {
            repoData
        } else {
            repoData.mapNotNull { (source, releases) ->
                val sourceMatches = source.name.lowercase(Locale.ROOT).contains(q) ||
                    source.url.lowercase(Locale.ROOT).contains(q)

                val filteredReleases = releases.mapNotNull { release ->
                    val relMatches = release.title.lowercase(Locale.ROOT).contains(q) ||
                        release.romName.lowercase(Locale.ROOT).contains(q) ||
                        release.androidVersionLabel.lowercase(Locale.ROOT).contains(q) ||
                        release.tagName.lowercase(Locale.ROOT).contains(q)

                    val matchingAssets = release.assets.filter { asset ->
                        relMatches || sourceMatches ||
                            asset.name.lowercase(Locale.ROOT).contains(q) ||
                            asset.architecture.lowercase(Locale.ROOT).contains(q) ||
                            asset.variant.lowercase(Locale.ROOT).contains(q)
                    }

                    if (matchingAssets.isNotEmpty()) {
                        release.copy(assets = matchingAssets)
                    } else if (relMatches || sourceMatches) {
                        release
                    } else {
                        null
                    }
                }

                if (filteredReleases.isNotEmpty()) {
                    Pair(source, filteredReleases)
                } else {
                    null
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Section Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Active GSI Repos",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Latest community & official GSIs (Android version-wise)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Action Buttons Row: Refresh & Add Repo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassButton(
                text = if (isLoading) "Refreshing..." else "Refresh Builds",
                iconPainter = painterResource(id = R.drawable.ic_refresh),
                variant = GlassButtonVariant.PRIMARY,
                enabled = !isLoading,
                height = 42.dp,
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
            )

            GlassButton(
                text = "+ Add Repo",
                iconPainter = painterResource(id = R.drawable.ic_build),
                variant = GlassButtonVariant.SECONDARY,
                height = 42.dp,
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(0.9f),
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search GSI (e.g. crdroid, a15, gapps, arm64)...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_file),
                    contentDescription = "Search",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cancel),
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        // Loading indicator
        if (isLoading && repoData.isEmpty()) {
            CardBox(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF06B6D4),
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Fetching latest GSI builds from active repos...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (filteredRepoData.isEmpty()) {
            CardBox(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No GSIs matched '$searchQuery'" else "No GSI repositories found. Tap '+ Add Repo' to add a source.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            // Render Maintainer Folders
            filteredRepoData.forEach { (source, releases) ->
                GsiMaintainerCard(
                    source = source,
                    releases = releases,
                    downloadsMap = downloadsMap,
                    onDownloadAsset = onDownloadAsset,
                    onCancelAsset = onCancelAsset,
                    onInstallAsset = onInstallAsset,
                    initiallyExpanded = searchQuery.isNotBlank(),
                    onDeleteCustomRepo = if (source.isCustom) {
                        { onDeleteCustomRepo(source.url) }
                    } else {
                        null
                    },
                )
            }
        }
    }

    if (showAddDialog) {
        AddGsiRepoDialog(
            onDismissRequest = { showAddDialog = false },
            onAddRepo = { url ->
                onAddRepo(url)
                showAddDialog = false
            },
        )
    }
}
