package vegabobo.dsusideloader.ui.screen.multidsu.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.model.gsirepo.GsiAsset
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadProgress
import vegabobo.dsusideloader.model.gsirepo.GsiDownloadState
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant

@Composable
fun GsiAssetCard(
    asset: GsiAsset,
    downloadProgress: GsiDownloadProgress?,
    onDownloadClick: () -> Unit,
    onCancelClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val isDownloading = downloadProgress?.state == GsiDownloadState.DOWNLOADING
    val isDownloaded = downloadProgress?.state == GsiDownloadState.DOWNLOADED
    val progressPercent = downloadProgress?.progress ?: 0
    var isChangelogExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color(0x18FFFFFF) else Color(0x0C000000))
            .border(
                width = 1.dp,
                color = if (asset.isNewOrUpdated) Color(0x6610B981) else if (isDark) Color(0x1FFFFFFF) else Color(0x14000000),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Icon + Filename + (Optional NEW/UPDATED Badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (asset.isNewOrUpdated) Color(0x3310B981) else Color(0x2838BDF8)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = if (asset.isNewOrUpdated) R.drawable.ic_settings_new_releases else R.drawable.ic_file),
                        contentDescription = null,
                        tint = if (asset.isNewOrUpdated) Color(0xFF10B981) else Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = asset.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (asset.isNewOrUpdated) {
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

            // Prominent Release Date & Time (Always Visible on its own line)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logs),
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (asset.releaseDate.isNotBlank()) "Release Date: ${asset.releaseDate}" else "Release Date: Recent Build",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                )
            }

            // Specifications Chips Row: Architecture, Variant, File Size
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Architecture Chip
                TagChip(
                    text = asset.architecture,
                    bgColor = if (isDark) Color(0x2A3B82F6) else Color(0x1E3B82F6),
                    textColor = Color(0xFF60A5FA),
                )

                // Variant Chip
                TagChip(
                    text = asset.variant,
                    bgColor = if (isDark) Color(0x288B5CF6) else Color(0x1E8B5CF6),
                    textColor = Color(0xFFA78BFA),
                )

                // Size Chip
                if (asset.formattedSize.isNotBlank()) {
                    TagChip(
                        text = asset.formattedSize,
                        bgColor = if (isDark) Color(0x20FFFFFF) else Color(0x12000000),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Downloading Progress Bar
            if (isDownloading) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                    LinearProgressIndicator(
                        progress = progressPercent / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF06B6D4),
                        trackColor = if (isDark) Color(0x33FFFFFF) else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Downloading to Multi-DSU-Sideloader ($progressPercent%)...",
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4),
                    )
                }
            }

            // Action Buttons (Compact height & horizontal padding to fit perfectly)
            if (isDownloading) {
                // While Downloading: Progress + Cancel button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassButton(
                        text = "$progressPercent% Downloading...",
                        variant = GlassButtonVariant.SECONDARY,
                        enabled = false,
                        height = 32.dp,
                        fontSize = 11.5.sp,
                        horizontalPadding = 8.dp,
                        onClick = { },
                        modifier = Modifier.weight(1.3f),
                    )

                    GlassButton(
                        text = "Cancel",
                        iconPainter = painterResource(id = R.drawable.ic_cancel),
                        variant = GlassButtonVariant.DANGER,
                        height = 32.dp,
                        fontSize = 11.5.sp,
                        horizontalPadding = 8.dp,
                        iconSize = 13.dp,
                        onClick = onCancelClick,
                        modifier = Modifier.weight(0.9f),
                    )
                }
            } else {
                // When Not Downloading: Changelog + Download & Install buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Changelog Button (collapsible view toggle)
                    GlassButton(
                        text = if (isChangelogExpanded) "Hide Notes ▴" else "Changelog ▾",
                        iconPainter = painterResource(id = R.drawable.ic_settings_info),
                        variant = if (isChangelogExpanded) GlassButtonVariant.PRIMARY else GlassButtonVariant.SECONDARY,
                        height = 32.dp,
                        fontSize = 11.5.sp,
                        horizontalPadding = 8.dp,
                        iconSize = 13.dp,
                        onClick = { isChangelogExpanded = !isChangelogExpanded },
                        modifier = Modifier.weight(1f),
                    )

                    // Download & Install / Install GSI Button
                    GlassButton(
                        text = if (isDownloaded) "Install GSI" else "Download & Install",
                        iconPainter = painterResource(id = R.drawable.ic_install_mobile),
                        variant = if (isDownloaded) GlassButtonVariant.SUCCESS else GlassButtonVariant.PRIMARY,
                        height = 32.dp,
                        fontSize = 11.5.sp,
                        horizontalPadding = 8.dp,
                        iconSize = 14.dp,
                        onClick = onInstallClick,
                        modifier = Modifier.weight(1.3f),
                    )
                }
            }

            // Collapsible Changelog Container (Tap anywhere on changelog to collapse)
            AnimatedVisibility(
                visible = isChangelogExpanded,
                enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(180)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0x22FFFFFF) else Color(0x0A000000))
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color(0x18FFFFFF) else Color(0x10000000),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { isChangelogExpanded = false }
                        .padding(10.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings_info),
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Release Notes (Tap to collapse)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Collapse",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = if (asset.changelog.isNotBlank()) asset.changelog else "No detailed changelog provided by maintainer.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    bgColor: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
