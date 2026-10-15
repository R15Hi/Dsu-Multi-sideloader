package vegabobo.dsusideloader.ui.screen.multidsu.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.model.gsirepo.GsiAsset
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadProgress
import vegabobo.dsusideloader.model.gsirepo.GsiRelease
import vegabobo.dsusideloader.model.gsirepo.GsiRepoSource
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.components.UserAvatar
import vegabobo.dsusideloader.ui.components.frostedglass.ApplyWindowBlurEffect
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GsiMaintainerCard(
    source: GsiRepoSource,
    releases: List<GsiRelease>,
    downloadsMap: Map<String, GsiDownloadProgress>,
    onDownloadAsset: (GsiAsset) -> Unit,
    onCancelAsset: (GsiAsset) -> Unit,
    onInstallAsset: (GsiAsset) -> Unit,
    onDeleteRepo: (() -> Unit)? = null,
    onDeleteCustomRepo: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var isExpanded by remember(initiallyExpanded) { mutableStateOf(initiallyExpanded) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val hasNewOrUpdated = releases.any { it.isNewOrUpdated || it.assets.any { a -> a.isNewOrUpdated } }

    // Group assets by (ROM Name, Android Version, Label) arranged strictly recent to least recent
    val versionGroups = remember(releases) {
        val map = linkedMapOf<String, MutableList<GsiAsset>>()
        releases.sortedByDescending { it.publishedTimestamp }.forEach { rel ->
            val romTitle = if (rel.romName.isNotBlank() && !source.name.contains(rel.romName, ignoreCase = true)) {
                "${rel.romName} • ${rel.androidVersionLabel}"
            } else {
                rel.androidVersionLabel
            }
            val list = map.getOrPut(romTitle) { mutableListOf() }
            list.addAll(rel.assets.sortedByDescending { it.publishedTimestamp })
        }
        map
    }

    val totalAssetsCount = versionGroups.values.sumOf { it.size }

    CardBox(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Folder Header Row (Tap to expand/collapse, Long-press to remove repository)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = { isExpanded = !isExpanded },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDeleteDialog = true
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // User Avatar
                UserAvatar(
                    avatarUrl = source.avatarUrl,
                    displayName = source.name,
                    hasUpdatedBadge = hasNewOrUpdated,
                    size = 42.dp,
                    cornerRadius = 12.dp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Source URL
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = source.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // Minimal Chip for "Updated"
                        if (hasNewOrUpdated) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0x2E10B981) else Color(0x2210B981))
                                    .border(
                                        width = 0.8.dp,
                                        color = Color(0x6610B981),
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                    )
                                    Text(
                                        text = "Updated",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                        lineHeight = 12.sp,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (totalAssetsCount > 0) "$totalAssetsCount Latest builds" else "No builds found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Delete Button for custom repos
                if (source.isCustom && onDeleteCustomRepo != null) {
                    IconButton(
                        onClick = onDeleteCustomRepo,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_discard),
                            contentDescription = "Remove Repo",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Proper Expand/Collapse Arrow: Down arrow when collapsed, Up arrow when expanded
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isExpanded) (if (isDark) Color(0x24FFFFFF) else Color(0x12000000)) else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = if (isExpanded) Color(0xFF06B6D4) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Expanded Version Groups (Fast, consistent, zero-jank animation)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(180)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180)),
            ) {
                if (versionGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No compatible GSI releases found in this repository.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        versionGroups.forEach { (title, assets) ->
                            GsiVersionGroup(
                                versionTitle = title,
                                assets = assets,
                                downloadsMap = downloadsMap,
                                onDownloadAsset = onDownloadAsset,
                                onCancelAsset = onCancelAsset,
                                onInstallAsset = onInstallAsset,
                                initiallyExpanded = false,
                            )
                        }
                    }
                }
            }
        }
    }

    // Frosted Glass Delete/Remove Repository Confirmation Modal
    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            ApplyWindowBlurEffect(blurRadius = 24.dp)
            GlassSurface(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 340.dp)
                    .wrapContentHeight(),
                cornerRadius = 24.dp,
                opacity = 0.92f,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33EF4444) else Color(0x22EF4444)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_discard),
                            contentDescription = "Remove Repo",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Text(
                        text = "Remove Repository",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "Are you sure you want to remove '${source.name}' from your GSI Hub? You can add it back anytime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GlassButton(
                            text = "Cancel",
                            variant = GlassButtonVariant.SECONDARY,
                            height = 38.dp,
                            fontSize = 13.sp,
                            horizontalPadding = 12.dp,
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.weight(1f),
                        )

                        GlassButton(
                            text = "Remove",
                            iconPainter = painterResource(id = R.drawable.ic_discard),
                            variant = GlassButtonVariant.DANGER,
                            height = 38.dp,
                            fontSize = 13.sp,
                            horizontalPadding = 12.dp,
                            onClick = {
                                showDeleteDialog = false
                                (onDeleteRepo ?: onDeleteCustomRepo)?.invoke()
                            },
                            modifier = Modifier.weight(1.1f),
                        )
                    }
                }
            }
        }
    }
}
