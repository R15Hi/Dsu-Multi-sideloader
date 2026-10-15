package vegabobo.dsusideloader.ui.screen.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.system.exitProcess
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.ui.cards.DsuInfoCard
import vegabobo.dsusideloader.ui.cards.ImageSizeCard
import vegabobo.dsusideloader.ui.cards.StorageInfoCard
import vegabobo.dsusideloader.ui.cards.UserdataCard
import vegabobo.dsusideloader.ui.cards.installation.InstallationCard
import vegabobo.dsusideloader.ui.cards.warnings.GrantingPermissionCard
import vegabobo.dsusideloader.ui.cards.warnings.RequiresLogPermissionCard
import vegabobo.dsusideloader.ui.cards.warnings.SetupStorage
import vegabobo.dsusideloader.ui.cards.warnings.StorageWarningCard
import vegabobo.dsusideloader.ui.cards.warnings.UnlockedBootloaderCard
import vegabobo.dsusideloader.ui.cards.warnings.UnsupportedCard
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.ui.sdialogs.CancelSheet
import vegabobo.dsusideloader.ui.sdialogs.ConfirmInstallationSheet
import vegabobo.dsusideloader.ui.sdialogs.DiscardDSUSheet
import vegabobo.dsusideloader.ui.sdialogs.ImageSizeWarningSheet
import vegabobo.dsusideloader.ui.sdialogs.ViewLogsBottomSheet
import vegabobo.dsusideloader.ui.util.KeepScreenOn
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

object HomeLinks {
    const val DSU_LEARN_MORE = "https://developer.android.com/topic/dsu"
    const val DSU_DOCS = "https://source.android.com/devices/tech/ota/dynamic-system-updates"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navigate: (String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val context = androidx.compose.ui.platform.LocalContext.current
    val currentTheme by homeViewModel.dataStore.data.map {
        it[stringPreferencesKey(AppPrefs.THEME_STYLE)] ?: "dynamic"
    }.collectAsState(initial = "dynamic")

    val themeCycle = remember {
        listOf(
            "dynamic" to "Dynamic Theme",
            "glassy" to "Glassy Theme",
            "dark" to "Dark Theme",
            "light" to "Light Theme",
            "amoled" to "AMOLED Theme",
        )
    }

    if (uiState.shouldKeepScreenOn) {
        KeepScreenOn()
    }

    LaunchedEffect(Unit) {
        homeViewModel.setupUserPreferences()
        homeViewModel.session.operationMode.collectLatest {
            homeViewModel.initialChecks()
        }
    }

    LaunchedEffect(homeViewModel.session.userSelection.selectedFileUri) {
        val selectedUri = homeViewModel.session.userSelection.selectedFileUri
        if (selectedUri != android.net.Uri.EMPTY) {
            homeViewModel.onFileSelectionResult(selectedUri)
        }
    }

    ApplicationScreen(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        enableDefaultScrollBehavior = true,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        topBar = {
            TopBar(
                barTitle = getDynamicTimeGreeting(),
                barSubtitle = "Welcome to Multi DSU Sideloader",
                scrollBehavior = null,
                actions = {
                    IconButton(onClick = {
                        val currentIndex = themeCycle.indexOfFirst { it.first.equals(currentTheme, ignoreCase = true) }
                        val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % themeCycle.size
                        val (nextKey, nextLabel) = themeCycle[nextIndex]
                        homeViewModel.updateThemeStyle(nextKey)
                        android.widget.Toast.makeText(context, nextLabel, android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings_color_lens),
                            contentDescription = "Theme",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
        content = {
            Box(modifier = Modifier.animateContentSize()) {
                when (uiState.additionalCard) {
                    AdditionalCardState.NO_DYNAMIC_PARTITIONS ->
                        UnsupportedCard(
                            onClickClose = { exitProcess(0) },
                            onClickContinueAnyway = { homeViewModel.overrideDynamicPartitionCheck() },
                        )

                    AdditionalCardState.SETUP_STORAGE ->
                        SetupStorage { homeViewModel.takeUriPermission(it) }

                    AdditionalCardState.UNAVAIABLE_STORAGE ->
                        StorageWarningCard(
                            minPercentageFreeStorage = homeViewModel.allocPercentageInt.toString(),
                            onClick = { homeViewModel.overrideUnavaiableStorage() },
                        )

                    AdditionalCardState.MISSING_READ_LOGS_PERMISSION ->
                        RequiresLogPermissionCard(
                            onClickGrant = { homeViewModel.grantReadLogs() },
                            onClickRefuse = { homeViewModel.refuseReadLogs() },
                        )

                    AdditionalCardState.GRANTING_READ_LOGS_PERMISSION ->
                        GrantingPermissionCard()

                    AdditionalCardState.BOOTLOADER_UNLOCKED_WARNING ->
                        UnlockedBootloaderCard { homeViewModel.onClickBootloaderUnlockedWarning() }

                    AdditionalCardState.NONE -> {}
                }
            }
            if (uiState.passedInitialChecks && uiState.additionalCard == AdditionalCardState.NONE) {
                InstallationCard(
                    uiState = uiState.installationCard,
                    onClickInstall = { homeViewModel.onClickInstall() },
                    onClickUnmountSdCardAndRetry = { homeViewModel.onClickUnmountSdCardAndRetry() },
                    onClickSetSeLinuxPermissive = { homeViewModel.onClickSetSeLinuxPermissive() },
                    onClickRetryInstallation = { homeViewModel.onClickRetryInstallation() },
                    onClickClear = { homeViewModel.resetInstallationCard() },
                    onSelectFileSuccess = { homeViewModel.onFileSelectionResult(it) },
                    onClickCancelInstallation = { homeViewModel.onClickCancel() },
                    onClickDiscardInstalledGsiAndInstall = { homeViewModel.onClickDiscardGsiAndStartInstallation() },
                    onClickDiscardDsu = { homeViewModel.showDiscardSheet() },
                    onClickRebootToDynOS = { homeViewModel.onClickRebootToDynOS() },
                    onClickViewLogs = { homeViewModel.showLogsWarning() },
                    onClickViewCommands = { navigate(Destinations.ADBInstallation) },
                    onClickBackupAndHide = { homeViewModel.onClickBackupAndHide() },
                    minPercentageOfFreeStorage = homeViewModel.allocPercentageInt.toString(),
                )

                if (!uiState.isInstalling()) {
                    StorageInfoCard()

                    UserdataCard(
                        isEnabled = false,
                        uiState = uiState.userDataCard,
                        onCheckedChange = { homeViewModel.onCheckUserdataCard() },
                        onValueChange = { homeViewModel.updateUserdataSize(it) },
                    )
                    ImageSizeCard(
                        isEnabled = false,
                        uiState = uiState.imageSizeCard,
                        onCheckedChange = { homeViewModel.onCheckImageSizeCard() },
                        onValueChange = { homeViewModel.updateImageSize(it) },
                    )
                    DsuInfoCard(
                        onClickViewDocs = { uriHandler.openUri(HomeLinks.DSU_DOCS) },
                        onClickLearnMore = { uriHandler.openUri(HomeLinks.DSU_LEARN_MORE) },
                    )
                }
            }
        },
    )

    when (uiState.sheetDisplay) {
        SheetDisplayState.CONFIRM_INSTALLATION ->
            ConfirmInstallationSheet(
                filename = homeViewModel.obtainSelectedFilename(),
                userdata = homeViewModel.session.userSelection.getUserDataSizeAsGB(),
                fileSize = homeViewModel.session.userSelection.userSelectedImageSize,
                onClickConfirm = { homeViewModel.onConfirmInstallationSheet() },
                onClickCancel = { homeViewModel.dismissSheet() },
            )

        SheetDisplayState.CANCEL_INSTALLATION ->
            CancelSheet(
                onClickConfirm = { homeViewModel.onClickCancelInstallationButton() },
                onClickCancel = { homeViewModel.dismissSheet() },
            )

        SheetDisplayState.IMAGESIZE_WARNING ->
            ImageSizeWarningSheet(
                onClickConfirm = { homeViewModel.dismissSheet() },
                onClickCancel = { homeViewModel.onCheckImageSizeCard() },
            )

        SheetDisplayState.DISCARD_DSU ->
            DiscardDSUSheet(
                onClickConfirm = { homeViewModel.onClickDiscardGsi() },
                onClickCancel = { homeViewModel.dismissSheet() },
            )

        SheetDisplayState.VIEW_LOGS ->
            ViewLogsBottomSheet(
                logs = uiState.installationLogs,
                onClickSaveLogs = { homeViewModel.saveLogs(it) },
                onDismiss = { homeViewModel.dismissSheet() },
            )

        SheetDisplayState.NONE -> {}
    }
}

private fun getDynamicTimeGreeting(): String {
    val calendar = java.util.Calendar.getInstance()
    return when (calendar.get(java.util.Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        in 17..20 -> "Good Evening 🌆"
        else -> "Good Night 🌙"
    }
}
