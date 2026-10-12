package vegabobo.dsusideloader.ui.cards.installation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface
import vegabobo.dsusideloader.ui.screen.home.InstallationCardState

@Composable
fun NotInstallingCardContent(
    uiState: InstallationCardState,
    onClickSelectFile: () -> Unit = {},
    onClickClear: () -> Unit,
    onClickInstall: () -> Unit,
) {
    val isFileSelected = uiState.isInstallable && uiState.text.isNotEmpty()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isFileSelected) {
            // Unselected state: "Select a file to be installed" with chevron arrow
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickSelectFile() },
                cornerRadius = 18.dp,
                opacity = 0.35f,
                shadowElevation = 0.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_file),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.select_gsi_info),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Tap to choose GSI image (.img, .xz, .gz)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        } else {
            // Selected state: Displays file icon, filename, file size, and remove/clear icon
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickSelectFile() },
                cornerRadius = 18.dp,
                opacity = 0.40f,
                shadowElevation = 0.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_file),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.fileName.ifEmpty { uiState.text },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val sizeText = if (uiState.fileSize > 0) formatSize(uiState.fileSize) else ""
                        val fileTypeDesc = if (sizeText.isNotEmpty()) "$sizeText · GSI Image" else "GSI Image"
                        Text(
                            text = fileTypeDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onClickClear,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x14000000)),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = uiState.isError) {
            Text(
                text = stringResource(id = R.string.file_unsupported),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(14.dp))

        // Install button below
        GlassButton(
            text = stringResource(R.string.install),
            onClick = onClickInstall,
            enabled = uiState.isInstallable,
            variant = GlassButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
}
