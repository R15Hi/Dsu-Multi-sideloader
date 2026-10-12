package vegabobo.dsusideloader.ui.sdialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.DialogLikeBottomSheet

@Composable
fun DiscardDSUSheet(
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit,
) {
    DialogLikeBottomSheet(
        title = stringResource(id = R.string.discard_dsu_question),
        iconPainter = painterResource(id = R.drawable.ic_discard),
        text = stringResource(id = R.string.dsu_already_installed_warning),
        confirmText = stringResource(id = R.string.yes),
        cancelText = stringResource(id = R.string.cancel),
        onClickConfirm = onClickConfirm,
        onClickCancel = onClickCancel,
    )
}
