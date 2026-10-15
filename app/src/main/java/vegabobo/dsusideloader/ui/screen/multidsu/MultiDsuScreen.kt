package vegabobo.dsusideloader.ui.screen.multidsu

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.components.frostedglass.ApplyWindowBlurEffect
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDsuScreen(
    navigate: (String) -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    viewModel: MultiDsuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newDsuName by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.refreshState(showLoading = true)
            kotlinx.coroutines.delay(650)
            isRefreshing = false
        }
    }

    LaunchedEffect(uiState.activeDsuName) {
        if (uiState.activeDsuName.isNotBlank() && newDsuName.isBlank()) {
            newDsuName = uiState.activeDsuName
        }
    }

    val blurEffect by animateDpAsState(targetValue = if (isRefreshing || uiState.progressAction.isNotEmpty()) 16.dp else 0.dp)

    ApplicationScreen(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .blur(blurEffect),
        enableDefaultScrollBehavior = true,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        topBar = {
            TopBar(
                barTitle = "Multi-DSU Manager",
                scrollBehavior = null,
            )
        },
        content = {
            if (!uiState.hasRoot) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    opacity = 0.45f,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33EF4444)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings_security),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Root Required", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Multi-DSU relies on privileged file swapping which requires full Root access (Magisk/KernelSU). Shizuku is not sufficient.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Link to GSI Hub
                CardBox(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3306B6D4)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings_new_releases),
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Explore GSI Hub",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "Browse, download and install latest community & official GSIs from the GSI Hub tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                // Refresh State Button
                GlassButton(
                    text = "Refresh State",
                    iconPainter = painterResource(id = R.drawable.ic_refresh),
                    variant = GlassButtonVariant.PRIMARY,
                    onClick = { isRefreshing = true },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Progress/Action Modal Dialog (active during Refresh State or heavy operations like backup/restore)
                if (isRefreshing || uiState.progressAction.isNotEmpty()) {
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
                                if (uiState.progressAction.isNotEmpty()) {
                                    Text(
                                        uiState.progressAction,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    LinearProgressIndicator(
                                        progress = uiState.progress,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = Color(0xFF06B6D4),
                                        trackColor = if (isDark) Color(0x33FFFFFF) else MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "${(uiState.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Spacer(Modifier.height(4.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(46.dp),
                                        color = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.5.dp,
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Refreshing...",
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

                // Active DSU Card / No Active DSU Card
                if (uiState.hasActiveDsu) {
                    CardBox(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x3310B981)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_list),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (uiState.activeDsuName.isNotBlank()) "Active DSU: ${uiState.activeDsuName}" else "Active DSU Detected",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        "You currently have an active DSU installed in the slot.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = newDsuName,
                                onValueChange = { newDsuName = it },
                                label = { Text("Backup Name (e.g. PixelOS-17)") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    containerColor = if (isDark) Color(0x20FFFFFF) else Color(0x0A000000),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            GlassButton(
                                text = "Backup & Hide Active DSU",
                                iconPainter = painterResource(id = R.drawable.ic_userdata),
                                variant = GlassButtonVariant.PRIMARY,
                                onClick = {
                                    if (newDsuName.isNotBlank()) viewModel.backupActiveDsu(newDsuName)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                } else {
                    CardBox(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color(0x28FFFFFF) else Color(0x14000000)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_storage),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "No Active DSU",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Your active DSU slot is currently empty. Swipe left to the 'Installed' tab to extract and install a new GSI. Once installed, you can back it up here!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Backed Up GSIs Section Header
                Spacer(Modifier.height(4.dp))
                Text(
                    "Backed Up GSIs",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp),
                )

                // Backed Up GSIs List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (uiState.backedUpDsus.isEmpty() && !uiState.isRefreshing && !isRefreshing) {
                        CardBox(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    "No backed up GSIs found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        uiState.backedUpDsus.forEach { dsu ->
                            CardBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.inspectDsu(dsu.name) },
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
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
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = dsu.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            softWrap = true,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }

                                    // Storage info row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            "Storage taken:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            dsu.size,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }

                                    // Timestamp info row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            "Backup date:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            dsu.timestamp,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }

                                    // Action buttons row: [Restore & Boot] and [Delete]
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp),
                                    ) {
                                        GlassButton(
                                            text = "Restore & Boot",
                                            iconPainter = painterResource(id = R.drawable.ic_refresh),
                                            variant = GlassButtonVariant.SUCCESS,
                                            onClick = { viewModel.restoreDsu(dsu.name) },
                                            height = 44.dp,
                                            modifier = Modifier.weight(1.3f),
                                        )
                                        GlassButton(
                                            text = "Delete",
                                            iconPainter = painterResource(id = R.drawable.ic_discard),
                                            variant = GlassButtonVariant.DANGER,
                                            onClick = { viewModel.deleteDsu(dsu.name) },
                                            height = 44.dp,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Link to GSI Hub
                CardBox(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3306B6D4)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings_new_releases),
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Explore GSI Hub",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "Browse, download and install latest community & official GSIs from the GSI Hub tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
    )

    // Image Metadata Inspector Dialog
    if (uiState.isInspecting) {
        Dialog(onDismissRequest = { viewModel.dismissInspector() }) {
            ApplyWindowBlurEffect(blurRadius = 24.dp)
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                cornerRadius = 24.dp,
                opacity = 0.90f,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Image Metadata",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (uiState.inspectingDsu == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF06B6D4))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Mounting and extracting metadata...", color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    } else {
                        val meta = uiState.inspectingDsu
                        if (meta != null) {
                            val rows = listOf(
                                "Android version" to meta.androidVersion,
                                "SDK version" to meta.sdkVersion,
                                "CPU ABI" to meta.cpuAbi,
                                "VNDK version" to meta.vndkVersion,
                                "Security patch" to meta.securityPatch,
                                "Build fingerprint" to meta.buildFingerprint,
                                "Treble compliant" to meta.isTrebleCompliant,
                            )
                            rows.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(label, color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                                    Text(value, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    GlassButton(
                        text = "Close",
                        variant = GlassButtonVariant.SECONDARY,
                        onClick = { viewModel.dismissInspector() },
                        modifier = Modifier.align(Alignment.End),
                        height = 42.dp,
                    )
                }
            }
        }
    }
}
