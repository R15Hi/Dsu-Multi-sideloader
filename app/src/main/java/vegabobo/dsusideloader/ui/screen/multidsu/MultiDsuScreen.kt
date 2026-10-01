package vegabobo.dsusideloader.ui.screen.multidsu

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDsuScreen(
    navigate: (String) -> Unit,
    viewModel: MultiDsuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newDsuName by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.refreshState()
            kotlinx.coroutines.delay(600)
            isRefreshing = false
        }
    }

    LaunchedEffect(uiState.activeDsuName) {
        if (uiState.activeDsuName.isNotBlank() && newDsuName.isBlank()) {
            newDsuName = uiState.activeDsuName
        }
    }

    val blurEffect by animateDpAsState(targetValue = if (isRefreshing || uiState.isRefreshing) 16.dp else 0.dp)

    ApplicationScreen(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp).blur(blurEffect),
        enableDefaultScrollBehavior = true,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        topBar = {
            TopBar(
                barTitle = "Multi-Dsu Manager",
                scrollBehavior = it,
            )
        },
        content = {
            if (!uiState.hasRoot) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Root Required", fontWeight = FontWeight.Bold)
                        Text("Multi-DSU relies on privileged file swapping which requires full Root access (Magisk/KernelSU). Shizuku is not sufficient.")
                    }
                }
                return@ApplicationScreen
            }

            Button(onClick = { isRefreshing = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Refresh State")
            }

            if (isRefreshing || uiState.isRefreshing) {
                Dialog(onDismissRequest = { }) {
                    Card(
                        modifier = Modifier.widthIn(min = 160.dp, max = 280.dp).wrapContentHeight(),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            if (uiState.progressAction.isNotEmpty()) {
                                Text(uiState.progressAction, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(8.dp))
                                androidx.compose.material3.LinearProgressIndicator(progress = uiState.progress, modifier = Modifier.fillMaxWidth())
                                Text("${(uiState.progress * 100).toInt()}%", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                            } else {
                                Spacer(Modifier.height(16.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp,
                                )
                                Spacer(Modifier.height(16.dp))
                                Text("Refreshing...", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (uiState.hasActiveDsu) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (uiState.activeDsuName.isNotBlank()) "Active DSU Detected: ${uiState.activeDsuName}" else "Active DSU Detected",
                            fontWeight = FontWeight.Bold,
                        )
                        Text("You currently have an active DSU installed.")
                        OutlinedTextField(
                            value = newDsuName,
                            onValueChange = { newDsuName = it },
                            label = { Text("Backup Name (e.g. LineageOS)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = {
                                if (newDsuName.isNotBlank()) viewModel.backupActiveDsu(newDsuName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Backup & Hide Active DSU")
                        }
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No Active DSU", fontWeight = FontWeight.Bold)
                        Text("Your active DSU slot is currently empty. Swipe left to the 'Installed' tab to extract and install a new GSI. Once installed, you can back it up here!")
                    }
                }
            }
            Text("Backed Up GSIs", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))

            Column(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.backedUpDsus.isEmpty() && !uiState.isRefreshing && !isRefreshing) {
                    Text("No backed up GSIs found.")
                } else {
                    uiState.backedUpDsus.forEach { dsu ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.inspectDsu(dsu.name) },
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(dsu.name, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)

                                // Display storage info
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Storage taken:", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                    Text(dsu.size, fontWeight = FontWeight.SemiBold, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                }

                                // Display timestamp info
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Backup date:", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                    Text(dsu.timestamp, fontWeight = FontWeight.SemiBold, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                    Button(onClick = { viewModel.restoreDsu(dsu.name) }, modifier = Modifier.weight(1f)) {
                                        Text("Restore & Boot")
                                    }
                                    Button(onClick = { viewModel.deleteDsu(dsu.name) }, modifier = Modifier.weight(1f)) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
    )

    if (uiState.isInspecting) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { viewModel.dismissInspector() }) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Image Metadata", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.inspectingDsu == null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Mounting and extracting metadata...")
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
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(label, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(value, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.dismissInspector() },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
