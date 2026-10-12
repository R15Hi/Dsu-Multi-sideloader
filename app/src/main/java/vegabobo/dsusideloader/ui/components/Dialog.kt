package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.ui.components.frostedglass.ApplyWindowBlurEffect

@Composable
fun Dialog(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    title: String = "",
    text: String = "",
    confirmText: String = "",
    cancelText: String = "",
    onClickConfirm: () -> Unit = {},
    onClickCancel: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isGlassy = MaterialTheme.colorScheme.background.alpha < 1f

    // High-opacity iOS frosted translucent material to obscure background text
    val containerColor = when {
        isGlassy -> Color(0xF2151829)
        isDark -> Color(0xF6181C2E)
        else -> Color(0xFBF8FAFC)
    }

    AlertDialog(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isDark) Color(0x38FFFFFF) else Color(0x66FFFFFF),
                        if (isDark) Color(0x12FFFFFF) else Color(0x18000000),
                    ),
                ),
                shape = RoundedCornerShape(26.dp),
            ),
        shape = RoundedCornerShape(26.dp),
        containerColor = containerColor,
        iconContentColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = {
            // Invoked inside DialogWindowProvider hierarchy to blur background window
            ApplyWindowBlurEffect(blurRadius = 32.dp)
            if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp),
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        onDismissRequest = onClickCancel,
        confirmButton = {
            if (confirmText.isNotEmpty()) {
                TextButton(onClick = onClickConfirm) {
                    Text(
                        text = confirmText,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        dismissButton = {
            if (cancelText.isNotEmpty()) {
                TextButton(onClick = onClickCancel) {
                    Text(text = cancelText)
                }
            }
        },
        text = {
            Column {
                if (title.isNotEmpty()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.padding(6.dp))
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                content()
            }
        },
    )
}
