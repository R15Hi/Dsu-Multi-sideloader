package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CardTitle(
    modifier: Modifier = Modifier,
    cardTitle: String,
    icon: ImageVector? = null,
) {
    val scroll = rememberScrollState(0)
    Row(
        modifier = modifier.horizontalScroll(scroll),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
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
        )
    }
}
