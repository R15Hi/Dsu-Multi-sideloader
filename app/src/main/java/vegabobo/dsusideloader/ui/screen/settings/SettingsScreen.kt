package vegabobo.dsusideloader.ui.screen.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.BuildCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.DialogLikeBottomSheet
import vegabobo.dsusideloader.ui.components.PreferenceItem
import vegabobo.dsusideloader.ui.components.Title
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.util.OperationMode
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navigate: (String) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        settingsViewModel.checkDevOpt()
    }

    var expanded by remember { mutableStateOf(false) }
    val isAnyDialogOpen = expanded || uiState.dialogSheetDisplay != DialogSheetState.NONE
    val blurEffect by animateDpAsState(targetValue = if (isAnyDialogOpen) 16.dp else 0.dp)

    ApplicationScreen(
        enableDefaultScrollBehavior = true,
        modifier = Modifier.blur(blurEffect),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.settings),
                scrollBehavior = it,
                onClickBackButton = { navigate(vegabobo.dsusideloader.ui.screen.Destinations.Up) },
            )
        },
    ) {
        if (uiState.isCleaning) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Cleaning up stuck DSU state...", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Title(title = stringResource(id = R.string.installation))
        PreferenceItem(
            title = stringResource(id = R.string.builtin_installer),
            description =
            if (settingsViewModel.isAndroidQ()) {
                stringResource(id = R.string.unsupported)
            } else if (uiState.isRoot) {
                stringResource(id = R.string.builtin_installer_description)
            } else {
                stringResource(R.string.requires_root)
            },
            icon = Icons.Outlined.Build,
            showToggle = true,
            isEnabled = uiState.isRoot && !settingsViewModel.isAndroidQ(),
            isChecked = uiState.preferences[AppPrefs.USE_BUILTIN_INSTALLER]!!,
            onClick = {
                if (!it) {
                    settingsViewModel.updateSheetDisplay(DialogSheetState.BUILT_IN_INSTALLER)
                }
                settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, !it)
            },
        )
        PreferenceItem(
            title = stringResource(id = R.string.unmount_sd_title),
            description = stringResource(id = R.string.unmount_sd_description),
            icon = Icons.Outlined.SdCard,
            showToggle = true,
            isChecked = uiState.preferences[AppPrefs.UMOUNT_SD]!!,
            onClick = { settingsViewModel.togglePreference(AppPrefs.UMOUNT_SD, !it) },
        )
        PreferenceItem(
            title = stringResource(id = R.string.keep_screen_on),
            icon = Icons.Outlined.Lightbulb,
            showToggle = true,
            isChecked = uiState.preferences[AppPrefs.KEEP_SCREEN_ON]!!,
            onClick = { settingsViewModel.togglePreference(AppPrefs.KEEP_SCREEN_ON, !it) },
        )

        if (uiState.isDevOptEnabled) {
            Title(title = stringResource(id = R.string.developer_options))
            PreferenceItem(
                title = stringResource(id = R.string.storage_check_title),
                description = stringResource(id = R.string.storage_check_description),
                icon = Icons.Outlined.Security,
                showToggle = true,
                isChecked = uiState.preferences[AppPrefs.DISABLE_STORAGE_CHECK]!!,
                onClick = {
                    if (!it) {
                        settingsViewModel.updateSheetDisplay(DialogSheetState.DISABLE_STORAGE_CHECK)
                    }
                    settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, !it)
                },
            )
            if (settingsViewModel.getOperationMode() != OperationMode.ADB) {
                PreferenceItem(
                    title = stringResource(id = R.string.full_logcat_logging_title),
                    description = stringResource(id = R.string.full_logcat_logging_description),
                    icon = Icons.Outlined.BugReport,
                    showToggle = true,
                    isChecked = uiState.preferences[AppPrefs.FULL_LOGCAT_LOGGING]!!,
                    onClick = { settingsViewModel.togglePreference(AppPrefs.FULL_LOGCAT_LOGGING, !it) },
                )
            }
        }

        Title(title = "Theming")
        Box {
            PreferenceItem(
                title = "App Theme",
                description = uiState.themeStyle.replaceFirstChar { it.uppercase() },
                icon = Icons.Outlined.ColorLens,
                onClick = { expanded = true },
            )
            if (expanded) {
                Dialog(onDismissRequest = { expanded = false }) {
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Select Theme", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            listOf("dynamic", "dark", "light", "amoled", "glassy").forEach { theme ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            settingsViewModel.updateThemeStyle(theme)
                                            expanded = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Outlined.ColorLens, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(16.dp))
                                    Text(theme.replaceFirstChar { it.uppercase() }, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Title(title = stringResource(id = R.string.other))
        PreferenceItem(
            title = "Fix Stuck Installation",
            description = "Force clears all active DSU state and temp files. Use this if installation gets stuck after extraction.",
            icon = Icons.Outlined.BuildCircle,
            onClick = {
                settingsViewModel.updateSheetDisplay(DialogSheetState.CLEAN_DSU_STATE)
            },
        )
        PreferenceItem(
            title = stringResource(id = R.string.operation_mode),
            description = settingsViewModel.checkOperationMode(),
            icon = Icons.Outlined.SettingsSuggest,
        )
        PreferenceItem(
            title = stringResource(id = R.string.about),
            description = stringResource(id = R.string.about_description),
            icon = Icons.Outlined.Info,
            onClick = { navigate(Destinations.About) },
        )
    }

    when (uiState.dialogSheetDisplay) {
        DialogSheetState.BUILT_IN_INSTALLER ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.experimental_feature),
                icon = Icons.Outlined.NewReleases,
                text = stringResource(id = R.string.experimental_feature_description),
                confirmText = stringResource(id = R.string.yes),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, false)
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                },
                onClickConfirm = { settingsViewModel.updateSheetDisplay(DialogSheetState.NONE) },
            )

        DialogSheetState.DISABLE_STORAGE_CHECK ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.warning_storage_check_title),
                icon = Icons.Outlined.WarningAmber,
                text = stringResource(id = R.string.warning_storage_check_description),
                confirmText = stringResource(id = R.string.continue_anyway),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, false)
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                },
                onClickConfirm = { settingsViewModel.updateSheetDisplay(DialogSheetState.NONE) },
            )

        DialogSheetState.CLEAN_DSU_STATE ->
            DialogLikeBottomSheet(
                title = "Fix Stuck Installation",
                icon = Icons.Outlined.WarningAmber,
                text = "This will forcefully remove any active DSU installation data, temporary files, and reset the Dynamic System Update service. Use this only if you are stuck during installation. Your Multi-DSU backups will NOT be deleted. The app will restart after cleanup. Are you sure?",
                confirmText = "Clean & Restart",
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                },
                onClickConfirm = {
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                    settingsViewModel.cleanupDsu()
                },
            )

        else -> {}
    }
}
