package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceItem(
    title: String,
    description: String = "",
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    onClick: (Boolean) -> Unit = {},
    isChecked: Boolean = false,
    showToggle: Boolean = false,
    showChevron: Boolean = false,
    isEnabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(isChecked) }, enabled = isEnabled)
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 12.dp,
                top = 12.dp,
            ),
    ) {
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 18.dp)
                    .size(24.dp),
                tint = Color.Unspecified,
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (description.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (showToggle) {
            Switch(
                checked = isChecked,
                enabled = isEnabled,
                onCheckedChange = { onClick(isChecked) },
                modifier = Modifier.padding(start = 8.dp),
            )
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
