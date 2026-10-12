package vegabobo.dsusideloader.ui.cards.installation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.screen.home.InstallationCardState
import vegabobo.dsusideloader.ui.util.launcherAcResult

@Composable
fun InstallationCard(
    uiState: InstallationCardState,
    modifier: Modifier = Modifier,
    minPercentageOfFreeStorage: String,
    onClickClear: () -> Unit,
    onClickInstall: () -> Unit,
    onClickRetryInstallation: () -> Unit,
    onClickUnmountSdCardAndRetry: () -> Unit,
    onClickSetSeLinuxPermissive: () -> Unit,
    onClickCancelInstallation: () -> Unit,
    onClickDiscardInstalledGsiAndInstall: () -> Unit,
    onClickDiscardDsu: () -> Unit,
    onClickRebootToDynOS: () -> Unit,
    onSelectFileSuccess: (Uri) -> Unit,
    onClickViewLogs: () -> Unit,
    onClickViewCommands: () -> Unit,
    onClickBackupAndHide: (() -> Unit)? = null,
) {
    var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
    chooseFile.type = "*/*"
    val mimetypes = arrayOf(
        "application/gzip",
        "application/x-gzip",
        "application/x-xz",
        "application/zip",
        "application/octet-stream",
    )
    chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
    chooseFile = Intent.createChooser(chooseFile, "")

    val launcherSelectFile = launcherAcResult {
        onSelectFileSuccess(it)
    }

    CardBox(
        cardTitle = stringResource(R.string.installation),
        iconPainter = painterResource(id = R.drawable.ic_install_mobile),
        addToggle = false,
        modifier = modifier,
    ) {
        InstallationCardStep(
            uiState = uiState,
            onClickSelectFile = { launcherSelectFile.launch(chooseFile) },
            onClickClear = onClickClear,
            onClickInstall = onClickInstall,
            onClickRetryInstallation = onClickRetryInstallation,
            onClickUnmountSdCardAndRetry = onClickUnmountSdCardAndRetry,
            onClickSetSeLinuxPermissive = onClickSetSeLinuxPermissive,
            onClickCancelInstallation = onClickCancelInstallation,
            onClickDiscardInstalledGsiAndInstall = onClickDiscardInstalledGsiAndInstall,
            onClickDiscardDsu = onClickDiscardDsu,
            onClickRebootToDynOS = onClickRebootToDynOS,
            onClickViewLogs = onClickViewLogs,
            onClickViewCommands = onClickViewCommands,
            onClickBackupAndHide = onClickBackupAndHide,
            minPercentageOfFreeStorage = minPercentageOfFreeStorage,
        )
    }
}
