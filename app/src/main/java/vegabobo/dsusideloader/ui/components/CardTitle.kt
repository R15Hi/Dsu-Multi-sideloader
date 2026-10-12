package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun CardTitle(
    modifier: Modifier = Modifier,
    cardTitle: String,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.padding(end = 8.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.padding(end = 8.dp))
        }
        Text(
            text = cardTitle,
            fontSize = 20.sp,
            maxLines = 1,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun CardTitle(
    modifier: Modifier = Modifier,
    cardTitle: String,
    iconPainter: Painter,
) {
    CardTitle(
        modifier = modifier,
        cardTitle = cardTitle,
        icon = null,
        iconPainter = iconPainter,
    )
}
