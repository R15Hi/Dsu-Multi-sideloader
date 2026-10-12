package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DialogLikeBottomSheet(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    title: String = "",
    text: String = "",
    confirmText: String = "",
    cancelText: String = "",
    hideKeyboard: Boolean = true,
    onClickConfirm: () -> Unit = {},
    onClickCancel: () -> Unit = {},
    onDismiss: () -> Unit = onClickCancel,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    if (hideKeyboard) {
        LocalSoftwareKeyboardController.current?.hide()
        LocalFocusManager.current.clearFocus()
    }

    Dialog(
        modifier = modifier,
        icon = icon,
        iconPainter = iconPainter,
        title = title,
        text = text,
        confirmText = confirmText,
        cancelText = cancelText,
        onClickConfirm = onClickConfirm,
        onClickCancel = onClickCancel,
    ) {
        content()
    }
}
