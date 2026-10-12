package vegabobo.dsusideloader.ui.cards.installation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preparation.InstallationStep
import vegabobo.dsusideloader.ui.cards.installation.content.CircularInstallationProgress
import vegabobo.dsusideloader.ui.cards.installation.content.NotInstallingCardContent
import vegabobo.dsusideloader.ui.cards.installation.content.ProgressableCardContent
import vegabobo.dsusideloader.ui.screen.home.InstallationCardState

@Composable
fun InstallationCardStep(
    uiState: InstallationCardState,
    minPercentageOfFreeStorage: String = "40",
    onClickSelectFile: () -> Unit = {},
    onClickClear: () -> Unit,
    onClickInstall: () -> Unit,
    onClickRetryInstallation: () -> Unit,
    onClickUnmountSdCardAndRetry: () -> Unit,
    onClickSetSeLinuxPermissive: () -> Unit,
    onClickCancelInstallation: () -> Unit,
    onClickDiscardInstalledGsiAndInstall: () -> Unit,
    onClickDiscardDsu: () -> Unit,
    onClickRebootToDynOS: () -> Unit,
    onClickViewLogs: () -> Unit,
    onClickViewCommands: () -> Unit,
    onClickBackupAndHide: (() -> Unit)? = null,
) {
    when (uiState.installationStep) {
        InstallationStep.NOT_INSTALLING ->
            NotInstallingCardContent(
                uiState = uiState,
                onClickSelectFile = onClickSelectFile,
                onClickClear = onClickClear,
                onClickInstall = onClickInstall,
            )

        InstallationStep.DSU_ALREADY_INSTALLED ->
            ProgressableCardContent(
                text = stringResource(R.string.dsu_already_installed),
                textFirstButton = stringResource(id = R.string.reboot_into_dsu),
                onClickFirstButton = onClickRebootToDynOS,
                textSecondButton = stringResource(id = R.string.discard),
                onClickSecondButton = onClickDiscardDsu,
            )

        InstallationStep.DSU_ALREADY_RUNNING_DYN_OS ->
            ProgressableCardContent(
                text = stringResource(R.string.already_running_dsu),
            )

        // Active extraction, preparation, installation, and completion steps
        // render the large glowing circular progress component
        InstallationStep.PROCESSING,
        InstallationStep.COPYING_FILE,
        InstallationStep.DECOMPRESSING_XZ,
        InstallationStep.COMPRESSING_TO_GZ,
        InstallationStep.DECOMPRESSING_GZIP,
        InstallationStep.EXTRACTING_FILE,
        InstallationStep.PROCESSING_LOG_READABLE,
        InstallationStep.INSTALLING,
        InstallationStep.INSTALLING_ROOTED,
        InstallationStep.CREATING_PARTITION,
        InstallationStep.INSTALL_SUCCESS,
        InstallationStep.INSTALL_SUCCESS_REBOOT_DYN_OS,
        ->
            CircularInstallationProgress(
                uiState = uiState,
                onClickCancel = onClickCancelInstallation,
                onClickRebootToDynOS = onClickRebootToDynOS,
                onClickDiscardDsu = onClickDiscardDsu,
                onClickBackupAndHide = onClickBackupAndHide,
            )

        InstallationStep.DISCARD_CURRENT_GSI -> {
            ProgressableCardContent(
                text = stringResource(R.string.discard_dsu_otg),
                textFirstButton = stringResource(id = R.string.discard_dsu),
                onClickFirstButton = onClickDiscardInstalledGsiAndInstall,
                textSecondButton = stringResource(id = R.string.cancel),
                onClickSecondButton = onClickCancelInstallation,
                progress = uiState.installationProgress,
            )
        }

        InstallationStep.WAITING_USER_CONFIRMATION -> {
            ProgressableCardContent(
                text = stringResource(R.string.installation_prompt),
                textFirstButton = stringResource(id = R.string.try_again),
                onClickFirstButton = onClickRetryInstallation,
                textSecondButton = stringResource(id = R.string.cancel),
                onClickSecondButton = onClickCancelInstallation,
            )
        }

        InstallationStep.ERROR ->
            ProgressableCardContent(
                text = stringResource(R.string.unknown_error, uiState.errorText),
                textFirstButton = stringResource(id = R.string.view_logs),
                onClickFirstButton = onClickViewLogs,
                textSecondButton = stringResource(id = R.string.mreturn),
                onClickSecondButton = onClickClear,
            )

        InstallationStep.ERROR_CANCELED ->
            ProgressableCardContent(
                text = stringResource(R.string.installation_canceled),
                textFirstButton = stringResource(id = R.string.view_logs),
                onClickFirstButton = onClickViewLogs,
                textSecondButton = stringResource(id = R.string.mreturn),
                onClickSecondButton = onClickClear,
            )

        InstallationStep.ERROR_REQUIRES_DISCARD_DSU ->
            ProgressableCardContent(
                text = stringResource(R.string.discard_dsu_otg),
                textFirstButton = stringResource(id = R.string.discard),
                onClickFirstButton = onClickDiscardInstalledGsiAndInstall,
                textSecondButton = stringResource(id = R.string.cancel),
                onClickSecondButton = onClickCancelInstallation,
            )

        InstallationStep.ERROR_ALREADY_RUNNING_DYN_OS ->
            ProgressableCardContent(
                text = stringResource(R.string.already_running_dsu),
                textSecondButton = stringResource(id = R.string.mreturn),
                onClickSecondButton = onClickClear,
            )

        InstallationStep.ERROR_CREATE_PARTITION ->
            ProgressableCardContent(
                text = stringResource(R.string.failed_create_partition, uiState.errorText),
                textSecondButton = stringResource(id = R.string.mreturn),
                onClickSecondButton = onClickClear,
            )

        InstallationStep.ERROR_EXTERNAL_SDCARD_ALLOC ->
            ProgressableCardContent(
                text = stringResource(
                    R.string.allocation_error_description,
                    uiState.errorText,
                ),
                textFirstButton = stringResource(id = R.string.allocation_error_action),
                onClickFirstButton = onClickUnmountSdCardAndRetry,
                textSecondButton = stringResource(id = R.string.cancel),
                onClickSecondButton = onClickCancelInstallation,
            )

        InstallationStep.ERROR_NO_AVAIL_STORAGE ->
            ProgressableCardContent(
                text = stringResource(R.string.storage_error_description, minPercentageOfFreeStorage),
                textFirstButton = stringResource(id = R.string.try_again),
                onClickFirstButton = onClickRetryInstallation,
                textSecondButton = stringResource(id = R.string.cancel),
                onClickSecondButton = onClickCancelInstallation,
            )

        InstallationStep.ERROR_F2FS_WRONG_PATH ->
            ProgressableCardContent(
                text = stringResource(
                    R.string.fs_features_error_description,
                    uiState.errorText,
                ),
                textFirstButton = stringResource(id = R.string.view_logs),
                onClickFirstButton = onClickViewLogs,
                textSecondButton = stringResource(id = R.string.clear),
                onClickSecondButton = onClickClear,
            )

        InstallationStep.ERROR_EXTENTS ->
            ProgressableCardContent(
                text = stringResource(R.string.extents_error_description),
                textFirstButton = stringResource(id = R.string.view_logs),
                onClickFirstButton = onClickViewLogs,
                textSecondButton = stringResource(id = R.string.mreturn),
                onClickSecondButton = onClickClear,
            )

        InstallationStep.ERROR_SELINUX ->
            ProgressableCardContent(
                text = stringResource(R.string.selinux_error_description),
                textFirstButton = stringResource(id = R.string.selinux_error_action),
                onClickFirstButton = onClickSetSeLinuxPermissive,
                textSecondButton = stringResource(id = R.string.cancel),
                onClickSecondButton = onClickCancelInstallation,
            )

        InstallationStep.ERROR_SELINUX_ROOTLESS ->
            ProgressableCardContent(
                text = stringResource(R.string.selinux_error_description),
                textFirstButton = stringResource(id = R.string.view_logs),
                onClickFirstButton = onClickViewLogs,
                textSecondButton = stringResource(id = R.string.mreturn),
                onClickSecondButton = onClickClear,
            )

        InstallationStep.REQUIRES_ADB_CMD_TO_CONTINUE ->
            ProgressableCardContent(
                text = stringResource(R.string.require_adb_cmd_to_continue),
                textFirstButton = stringResource(id = R.string.see_commands),
                onClickFirstButton = onClickViewCommands,
                textSecondButton = stringResource(id = R.string.mreturn),
                onClickSecondButton = onClickClear,
            )
    }
}
