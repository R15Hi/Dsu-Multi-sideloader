package vegabobo.dsusideloader.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.map
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.DialogLikeBottomSheet
import vegabobo.dsusideloader.ui.components.PreferenceItem
import vegabobo.dsusideloader.ui.components.Title
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.util.OperationMode
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navigate: (String) -> Unit,
    showBackButton: Boolean = false,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        settingsViewModel.checkDevOpt()
    }

    val currentTheme by settingsViewModel.dataStore.data.map {
        it[stringPreferencesKey(AppPrefs.THEME_STYLE)] ?: "dynamic"
    }.collectAsState(initial = uiState.themeStyle)

    var expanded by remember { mutableStateOf(false) }

    val isGlassy = MaterialTheme.colorScheme.background.alpha < 1f

    val themeDisplayMap = remember {
        mapOf(
            "glassy" to "Glassy (iOS Style)",
            "dynamic" to "Dynamic",
            "dark" to "Dark",
            "light" to "Light",
            "amoled" to "AMOLED",
        )
    }

    ApplicationScreen(
        enableDefaultScrollBehavior = true,
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.settings),
                centerTitle = true,
                scrollBehavior = null,
                onClickBackButton = if (showBackButton) { { navigate(Destinations.Up) } } else null,
            )
        },
    ) {
        if (uiState.isCleaning) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                cornerRadius = 18.dp,
                opacity = 0.50f,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Cleaning up stuck DSU state...", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Section: Installation
        Title(title = stringResource(id = R.string.installation))
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            opacity = if (isGlassy) 0.38f else 0.85f,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                    iconPainter = painterResource(id = R.drawable.ic_settings_build),
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
                Divider(color = Color(0x18FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = stringResource(id = R.string.unmount_sd_title),
                    description = stringResource(id = R.string.unmount_sd_description),
                    iconPainter = painterResource(id = R.drawable.ic_settings_unmount_sd),
                    showToggle = true,
                    isChecked = uiState.preferences[AppPrefs.UMOUNT_SD]!!,
                    onClick = { settingsViewModel.togglePreference(AppPrefs.UMOUNT_SD, !it) },
                )
                Divider(color = Color(0x18FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = stringResource(id = R.string.keep_screen_on),
                    iconPainter = painterResource(id = R.drawable.ic_settings_lightbulb),
                    showToggle = true,
                    isChecked = uiState.preferences[AppPrefs.KEEP_SCREEN_ON]!!,
                    onClick = { settingsViewModel.togglePreference(AppPrefs.KEEP_SCREEN_ON, !it) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Section: Developer Options (if enabled)
        if (uiState.isDevOptEnabled) {
            Title(title = stringResource(id = R.string.developer_options))
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                opacity = if (isGlassy) 0.38f else 0.85f,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PreferenceItem(
                        title = stringResource(id = R.string.storage_check_title),
                        description = stringResource(id = R.string.storage_check_description),
                        iconPainter = painterResource(id = R.drawable.ic_settings_security),
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
                        Divider(color = Color(0x18FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        PreferenceItem(
                            title = stringResource(id = R.string.full_logcat_logging_title),
                            description = stringResource(id = R.string.full_logcat_logging_description),
                            iconPainter = painterResource(id = R.drawable.ic_settings_bug_report),
                            showToggle = true,
                            isChecked = uiState.preferences[AppPrefs.FULL_LOGCAT_LOGGING]!!,
                            onClick = { settingsViewModel.togglePreference(AppPrefs.FULL_LOGCAT_LOGGING, !it) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Section: Theming
        Title(title = "Theming")
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            opacity = if (isGlassy) 0.38f else 0.85f,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Column {
                val currentThemeDisplay = themeDisplayMap[currentTheme.lowercase()]
                    ?: currentTheme.replaceFirstChar { it.uppercase() }

                PreferenceItem(
                    title = "App Theme",
                    description = currentThemeDisplay,
                    iconPainter = painterResource(id = R.drawable.ic_settings_color_lens),
                    showChevron = true,
                    onClick = { expanded = true },
                )
                if (expanded) {
                    vegabobo.dsusideloader.ui.components.ThemeSelectionDialog(
                        currentTheme = currentTheme,
                        onSelectTheme = { settingsViewModel.updateThemeStyle(it) },
                        onDismiss = { expanded = false },
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Section: Other
        Title(title = stringResource(id = R.string.other))
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            opacity = if (isGlassy) 0.38f else 0.85f,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                PreferenceItem(
                    title = "Fix Stuck Installation",
                    description = "Force clears all active DSU state and temp files. Use this if installation gets stuck after extraction.",
                    iconPainter = painterResource(id = R.drawable.ic_settings_build_circle),
                    onClick = {
                        settingsViewModel.updateSheetDisplay(DialogSheetState.CLEAN_DSU_STATE)
                    },
                )
                Divider(color = Color(0x18FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = stringResource(id = R.string.operation_mode),
                    description = settingsViewModel.checkOperationMode(),
                    iconPainter = painterResource(id = R.drawable.ic_settings_settings_suggest),
                )
                Divider(color = Color(0x18FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = stringResource(id = R.string.about),
                    description = stringResource(id = R.string.about_description),
                    iconPainter = painterResource(id = R.drawable.ic_settings_info),
                    showChevron = true,
                    onClick = { navigate(Destinations.About) },
                )
            }
        }
    }

    when (uiState.dialogSheetDisplay) {
        DialogSheetState.BUILT_IN_INSTALLER ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.experimental_feature),
                iconPainter = painterResource(id = R.drawable.ic_settings_new_releases),
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
                iconPainter = painterResource(id = R.drawable.ic_settings_warning_amber),
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
                iconPainter = painterResource(id = R.drawable.ic_settings_warning_amber),
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
