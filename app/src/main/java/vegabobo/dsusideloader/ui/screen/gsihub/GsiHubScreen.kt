package vegabobo.dsusideloader.ui.screen.gsihub

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.model.gsirepo.GsiRelease
import vegabobo.dsusideloader.model.gsirepo.GsiRepoSource
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.components.frostedglass.ApplyWindowBlurEffect
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface
import vegabobo.dsusideloader.ui.screen.multidsu.components.AddGsiRepoDialog
import vegabobo.dsusideloader.ui.screen.multidsu.components.GsiMaintainerCard
import vegabobo.dsusideloader.ui.screen.multidsu.components.GsiMaintainersCreditsCard
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GsiHubScreen(
    navigate: (String) -> Unit = {},
    onNavigateToHome: (() -> Unit)? = null,
    viewModel: GsiHubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isRefreshing = uiState.isGsiReposLoading
    val blurEffect by animateDpAsState(targetValue = if (isRefreshing) 16.dp else 0.dp)

    // Instant real-time search filtering
    val filteredRepoData: List<Pair<GsiRepoSource, List<GsiRelease>>> = remember(uiState.gsiRepos, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) {
            uiState.gsiRepos
        } else {
            uiState.gsiRepos.mapNotNull { (source, releases) ->
                val sourceMatches = source.name.lowercase(Locale.ROOT).contains(q) ||
                    source.url.lowercase(Locale.ROOT).contains(q)

                val filteredReleases = releases.mapNotNull { release ->
                    val relMatches = release.title.lowercase(Locale.ROOT).contains(q) ||
                        release.romName.lowercase(Locale.ROOT).contains(q) ||
                        release.androidVersionLabel.lowercase(Locale.ROOT).contains(q) ||
                        release.tagName.lowercase(Locale.ROOT).contains(q)

                    val matchingAssets = release.assets.filter { asset ->
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

    ApplicationScreen(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .blur(blurEffect),
        enableDefaultScrollBehavior = true,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        topBar = {
            TopBar(
                barTitle = "GSI Hub",
                barSubtitle = "Explore & Sideloader Treble GSI Builds",
                scrollBehavior = null,
            )
        },
        content = {
            // Action Buttons Row: Perfectly aligned Refresh Builds and + Add Repo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassButton(
                    text = if (isRefreshing) "Refreshing..." else "Refresh Builds",
                    iconPainter = painterResource(id = R.drawable.ic_refresh),
                    variant = GlassButtonVariant.PRIMARY,
                    enabled = !isRefreshing,
                    height = 42.dp,
                    onClick = { viewModel.fetchGsiRepos() },
                    modifier = Modifier.weight(1.1f),
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

            // Real-Time Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search GSI...",
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

            // Loading / Empty / Content states
            if (uiState.isGsiReposLoading && uiState.gsiRepos.isEmpty()) {
                CardBox(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
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
                            text = if (searchQuery.isNotBlank()) "No GSIs matched '$searchQuery'" else "No GSI repositories loaded. Tap 'Refresh Builds' to fetch releases.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // Maintainer cards (Recent to least recent order, compact view)
                filteredRepoData.forEach { (source, releases) ->
                    GsiMaintainerCard(
                        source = source,
                        releases = releases,
                        downloadsMap = uiState.gsiDownloads,
                        onDownloadAsset = { viewModel.downloadGsiAsset(it) },
                        onCancelAsset = { viewModel.cancelDownload(it) },
                        onInstallAsset = {
                            viewModel.installGsiAsset(it) {
                                onNavigateToHome?.invoke()
                            }
                        },
                        initiallyExpanded = searchQuery.isNotBlank(),
                        onDeleteRepo = { viewModel.removeRepo(source) },
                        onDeleteCustomRepo = if (source.isCustom) {
                            { viewModel.removeCustomRepo(source.url) }
                        } else {
                            null
                        },
                    )
                }
            }

            // Special Thanks to GSI Maintainers Card
            GsiMaintainersCreditsCard()

            Spacer(Modifier.height(80.dp))
        },
    )

    if (showAddDialog) {
        AddGsiRepoDialog(
            onDismissRequest = { showAddDialog = false },
            onAddRepo = { url ->
                viewModel.addCustomRepo(url)
                showAddDialog = false
            },
        )
    }

    // Center Blur Refresh Modal Dialog (Multi-DSU exact match)
    if (isRefreshing) {
        Dialog(onDismissRequest = { }) {
            ApplyWindowBlurEffect(blurRadius = 24.dp)
            GlassSurface(
                modifier = Modifier
                    .widthIn(min = 180.dp, max = 290.dp)
                    .wrapContentHeight(),
                cornerRadius = 24.dp,
                opacity = 0.90f,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Spacer(Modifier.height(4.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(46.dp),
                        color = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.5.dp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Refreshing...",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}
