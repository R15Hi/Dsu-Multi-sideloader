package vegabobo.dsusideloader.ui.sdialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.model.DSUConstants
import vegabobo.dsusideloader.ui.components.DialogItem
import vegabobo.dsusideloader.ui.components.DialogLikeBottomSheet

@Composable
fun ConfirmInstallationSheet(
    filename: String,
    userdata: String,
    fileSize: Long,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit,
) {
    DialogLikeBottomSheet(
        title = stringResource(id = R.string.proceed_installation),
        iconPainter = painterResource(id = R.drawable.ic_install_mobile),
        text = stringResource(id = R.string.proceed_installation_description),
        content = {
            Spacer(modifier = Modifier.padding(4.dp))
            DialogItem(
                iconPainter = painterResource(id = R.drawable.ic_file),
                title = "${stringResource(id = R.string.selected_file)}:",
                text = filename,
                textColor = MaterialTheme.colorScheme.onBackground,
            )
            DialogItem(
                iconPainter = painterResource(id = R.drawable.ic_storage),
                title = "${stringResource(id = R.string.userdata_size)}:",
                text = "${userdata}GB",
                textColor = MaterialTheme.colorScheme.onBackground,
            )
            if (fileSize != DSUConstants.DEFAULT_IMAGE_SIZE) {
                DialogItem(
                    iconPainter = painterResource(id = R.drawable.ic_image_size),
                    title = "${stringResource(id = R.string.image_size)}:",
                    text = "${fileSize}b",
                    textColor = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        confirmText = stringResource(id = R.string.proceed),
        cancelText = stringResource(id = R.string.cancel),
        onClickConfirm = { onClickConfirm() },
        onClickCancel = { onClickCancel() },
    )
}
