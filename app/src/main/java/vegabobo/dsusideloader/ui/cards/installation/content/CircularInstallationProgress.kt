package vegabobo.dsusideloader.ui.cards.installation.content

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preparation.InstallationStep
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface
import vegabobo.dsusideloader.ui.screen.home.InstallationCardState
import vegabobo.dsusideloader.ui.theme.GlassTokens

private enum class StepState {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
}

/**
 * Large circular installation progress component matching the iOS-style Glassy theme design.
 * Displays real extraction/installation progress, selected file details, phase indicators,
 * and completion state with the 3 mandatory actions.
 */
@Composable
fun CircularInstallationProgress(
    uiState: InstallationCardState,
    onClickCancel: () -> Unit,
    onClickRebootToDynOS: () -> Unit,
    onClickDiscardDsu: () -> Unit,
    onClickBackupAndHide: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val step = uiState.installationStep
    val isSuccess = step == InstallationStep.INSTALL_SUCCESS || step == InstallationStep.INSTALL_SUCCESS_REBOOT_DYN_OS

    // Determine current phase
    val isExtractingPhase = when (step) {
        InstallationStep.COPYING_FILE,
        InstallationStep.DECOMPRESSING_XZ,
        InstallationStep.COMPRESSING_TO_GZ,
        InstallationStep.DECOMPRESSING_GZIP,
        InstallationStep.EXTRACTING_FILE,
        -> true
        InstallationStep.PROCESSING -> uiState.currentPartitionText.isEmpty()
        else -> false
    }

    val isInstallingPhase = when (step) {
        InstallationStep.WAITING_USER_CONFIRMATION,
        InstallationStep.PROCESSING_LOG_READABLE,
        InstallationStep.INSTALLING,
        InstallationStep.INSTALLING_ROOTED,
        InstallationStep.CREATING_PARTITION,
        -> true
        else -> false
    }

    // Step states for the 3-step indicator row
    val extractionStepState = when {
        isSuccess -> StepState.COMPLETED
        isInstallingPhase -> StepState.COMPLETED
        isExtractingPhase -> StepState.IN_PROGRESS
        else -> StepState.PENDING
    }

    val installationStepState = when {
        isSuccess -> StepState.COMPLETED
        isInstallingPhase -> StepState.IN_PROGRESS
        else -> StepState.PENDING
    }

    val finalizingStepState = when {
        isSuccess -> StepState.COMPLETED
        step == InstallationStep.CREATING_PARTITION && uiState.currentPartitionText.contains("userdata", ignoreCase = true) -> StepState.IN_PROGRESS
        else -> StepState.PENDING
    }

    // Progress calculations
    val rawProgress = when {
        isSuccess -> 1.0f
        uiState.installationProgress in 0f..1f -> uiState.installationProgress
        else -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
    )

    val isIndeterminate = !isSuccess && (uiState.isProgressBarIndeterminate || (rawProgress <= 0f && (step == InstallationStep.PROCESSING || step == InstallationStep.PROCESSING_LOG_READABLE || step == InstallationStep.WAITING_USER_CONFIRMATION)))

    // Phase title and subtext
    val phaseLabel = when {
        isSuccess -> "COMPLETED"
        isExtractingPhase -> "EXTRACTING"
        isInstallingPhase -> "INSTALLING"
        else -> "PROCESSING"
    }

    val operationText = when (step) {
        InstallationStep.PROCESSING -> stringResource(R.string.processing)
        InstallationStep.COPYING_FILE -> stringResource(R.string.copying_file)
        InstallationStep.DECOMPRESSING_XZ -> stringResource(R.string.decompressing_xz)
        InstallationStep.COMPRESSING_TO_GZ -> stringResource(R.string.compressing_to_gz)
        InstallationStep.DECOMPRESSING_GZIP,
        InstallationStep.EXTRACTING_FILE,
        -> stringResource(R.string.extracting_file)
        InstallationStep.WAITING_USER_CONFIRMATION -> stringResource(R.string.installation_prompt)
        InstallationStep.PROCESSING_LOG_READABLE -> stringResource(R.string.installing)
        InstallationStep.INSTALLING,
        InstallationStep.INSTALLING_ROOTED,
        -> {
            if (uiState.currentPartitionText.isNotEmpty()) {
                stringResource(R.string.installing_partition, uiState.currentPartitionText)
            } else {
                stringResource(R.string.installing)
            }
        }
        InstallationStep.CREATING_PARTITION -> {
            if (uiState.currentPartitionText.isNotEmpty()) {
                stringResource(R.string.creating_partition, uiState.currentPartitionText)
            } else {
                stringResource(R.string.processing)
            }
        }
        InstallationStep.INSTALL_SUCCESS,
        InstallationStep.INSTALL_SUCCESS_REBOOT_DYN_OS,
        -> "DSU installation completed!"
        else -> stringResource(R.string.processing)
    }

    val bytesDetailText = formatBytesDetail(
        bytesProcessed = uiState.bytesProcessed,
        bytesTotal = uiState.bytesTotal,
        fileSize = uiState.fileSize,
        progress = rawProgress,
        isSuccess = isSuccess,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Selected File Header Card
        val displayName = uiState.fileName.ifEmpty { uiState.text }
        if (displayName.isNotEmpty()) {
            SelectedFileHeader(
                filename = displayName,
                fileSize = uiState.fileSize,
                partition = uiState.currentPartitionText,
            )
            Spacer(Modifier.height(18.dp))
        }

        // 2. Large Circular Progress Ring
        CircularProgressRing(
            progress = animatedProgress,
            isIndeterminate = isIndeterminate,
            isSuccess = isSuccess,
            phaseLabel = phaseLabel,
            ringDiameter = 240.dp,
            strokeWidth = 18.dp,
        )

        Spacer(Modifier.height(16.dp))

        // 3. Status description & Bytes details
        Text(
            text = operationText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (bytesDetailText.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = bytesDetailText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else if (isSuccess) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "The GSI has been installed successfully.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(20.dp))

        // 4. Three-step workflow indicators: Extracting -> Installing -> Finalizing
        StepsIndicatorRow(
            extractionState = extractionStepState,
            installationState = installationStepState,
            finalizingState = finalizingStepState,
        )

        Spacer(Modifier.height(24.dp))

        // 5. Action Buttons (Cancel during progress, 3 Actions upon completion)
        if (isSuccess) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 1. Reboot into DSU
                GlassButton(
                    text = stringResource(R.string.reboot_into_dsu),
                    icon = Icons.Outlined.PowerSettingsNew,
                    variant = GlassButtonVariant.SUCCESS,
                    onClick = onClickRebootToDynOS,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 2. Discard
                GlassButton(
                    text = stringResource(R.string.discard),
                    iconPainter = painterResource(id = R.drawable.ic_discard),
                    variant = GlassButtonVariant.DANGER,
                    onClick = onClickDiscardDsu,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 3. Backup & Hide
                if (onClickBackupAndHide != null) {
                    GlassButton(
                        text = "Backup & Hide",
                        iconPainter = painterResource(id = R.drawable.ic_userdata),
                        variant = GlassButtonVariant.NEUTRAL,
                        onClick = onClickBackupAndHide,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            // Cancel button during progress
            GlassButton(
                text = stringResource(R.string.cancel),
                iconPainter = painterResource(id = R.drawable.ic_cancel),
                variant = GlassButtonVariant.DANGER,
                onClick = onClickCancel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Top file preview card with icon, filename, and file size details.
 */
@Composable
private fun SelectedFileHeader(
    filename: String,
    fileSize: Long,
    partition: String,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        opacity = 0.35f,
        shadowElevation = 0.dp,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x333B82F6)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_file),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = filename,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = buildString {
                    if (fileSize > 0) {
                        append(formatSize(fileSize))
                    }
                    if (partition.isNotEmpty()) {
                        if (isNotEmpty()) append(" · ")
                        append(partition.replaceFirstChar { it.uppercase() })
                    } else if (filename.endsWith(".img") || filename.endsWith(".xz") || filename.endsWith(".gz")) {
                        if (isNotEmpty()) append(" · ")
                        append("GSI Image")
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Glowing circular progress ring with neon gradient arc and center percentage/phase.
 */
@Composable
private fun CircularProgressRing(
    progress: Float,
    isIndeterminate: Boolean,
    isSuccess: Boolean,
    phaseLabel: String,
    ringDiameter: Dp = 240.dp,
    strokeWidth: Dp = 18.dp,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val trackColor = if (isDark) Color(0x22FFFFFF) else Color(0x1F000000)

    val neonGradient = GlassTokens.NeonProgressGradient
    val successGradient = Brush.sweepGradient(
        listOf(
            Color(0xFF10B981),
            Color(0xFF34D399),
            Color(0xFF059669),
            Color(0xFF10B981),
        ),
    )

    Box(
        modifier = modifier
            .size(ringDiameter),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(ringDiameter)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // 1. Background Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // 2. Active Glowing Progress Arc
            if (isSuccess) {
                // Completed 100% Ring
                drawArc(
                    brush = successGradient,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            } else if (isIndeterminate) {
                // Rotating indeterminate arc
                drawArc(
                    brush = neonGradient,
                    startAngle = rotationAngle,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            } else if (progress > 0f) {
                val sweep = (progress * 360f).coerceIn(4f, 360f)

                // Outer soft ambient glow layer
                drawArc(
                    brush = neonGradient,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx + 6.dp.toPx(), cap = StrokeCap.Round),
                    alpha = 0.28f,
                )

                // Crisp primary neon arc
                drawArc(
                    brush = neonGradient,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        // Center display (Percentage + Phase label OR Big Success Checkmark)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isSuccess) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x2810B981))
                        .border(1.5.dp, Color(0xFF34D399), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "100%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = phaseLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp,
                    color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF059669),
                )
            } else {
                val percentageText = if (isIndeterminate) {
                    "..."
                } else {
                    "${(progress * 100).toInt()}%"
                }
                Text(
                    text = percentageText,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = phaseLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp,
                    color = if (isDark) Color(0xFF93C5FD) else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Seamless connected iOS timeline displaying the 3 sequential steps: Extracting -> Installing -> Finalizing.
 * Replaces boxy square cards with an elegant connected milestone indicator without artifacts.
 */
@Composable
private fun StepsIndicatorRow(
    extractionState: StepState,
    installationState: StepState,
    finalizingState: StepState,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) Color(0x22131627) else Color(0x14000000))
            .border(
                1.dp,
                if (isDark) Color(0x28FFFFFF) else Color(0x18000000),
                RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepMilestone(
                title = "Extracting",
                state = extractionState,
                modifier = Modifier.weight(1f),
            )

            MilestoneConnector(
                isCompleted = extractionState == StepState.COMPLETED,
            )

            StepMilestone(
                title = "Installing",
                state = installationState,
                modifier = Modifier.weight(1f),
            )

            MilestoneConnector(
                isCompleted = installationState == StepState.COMPLETED,
            )

            StepMilestone(
                title = "Finalizing",
                state = finalizingState,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MilestoneConnector(
    isCompleted: Boolean,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(
                if (isCompleted) Color(0xFF10B981) else if (isDark) Color(0x25FFFFFF) else Color(0x1F000000),
            ),
    )
}

@Composable
private fun StepMilestone(
    title: String,
    state: StepState,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val subtitle = when (state) {
        StepState.COMPLETED -> "Done"
        StepState.IN_PROGRESS -> "Running"
        StepState.PENDING -> "Pending"
    }

    val color = when (state) {
        StepState.COMPLETED -> Color(0xFF34D399)
        StepState.IN_PROGRESS -> Color(0xFF60A5FA)
        StepState.PENDING -> if (isDark) Color(0x66FFFFFF) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.20f))
                .border(1.5.dp, color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (state == StepState.COMPLETED) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp),
                )
            } else if (state == StepState.IN_PROGRESS) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (state == StepState.PENDING) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else if (isDark) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatBytesDetail(
    bytesProcessed: Long,
    bytesTotal: Long,
    fileSize: Long,
    progress: Float,
    isSuccess: Boolean,
): String {
    if (isSuccess) return ""
    return if (bytesProcessed > 0L && bytesTotal > 0L) {
        "${formatSize(bytesProcessed)} of ${formatSize(bytesTotal)}"
    } else if (fileSize > 0L && progress > 0f) {
        val estimatedDone = (fileSize * progress).toLong()
        "${formatSize(estimatedDone)} of ${formatSize(fileSize)}"
    } else {
        ""
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
}
