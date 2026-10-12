package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    cardTitle: String = "",
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    addToggle: Boolean = false,
    isToggleChecked: Boolean = false,
    isToggleEnabled: Boolean = true,
    addPadding: Boolean = true,
    cardColor: Color = Color.Unspecified,
    onCheckedChange: ((Boolean) -> Unit) = {},
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable (ColumnScope) -> Unit,
) {
    val isGlassy = MaterialTheme.colorScheme.background.alpha < 1f
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val effectiveColor = when {
        isGlassy -> if (isDark) Color(0x32141926) else Color(0x75FFFFFF)
        cardColor == Color.Unspecified || cardColor == MaterialTheme.colorScheme.inverseOnSurface -> MaterialTheme.colorScheme.surfaceVariant
        else -> cardColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(roundedCornerShape)
            .background(effectiveColor)
            .then(
                if (addPadding) {
                    Modifier
                        .padding(all = 10.dp)
                        .padding(end = 4.dp, start = 4.dp)
                } else {
                    Modifier
                },
            ),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (isGlassy && isDark) Color.White else MaterialTheme.colorScheme.onSurface,
        ) {
            Column(modifier = modifier) {
                if (cardTitle.isNotEmpty()) {
                    if (addToggle) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            CardTitle(
                                modifier = modifier.weight(1F),
                                cardTitle = cardTitle,
                                icon = icon,
                                iconPainter = iconPainter,
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Switch(
                                checked = isToggleChecked,
                                onCheckedChange = onCheckedChange,
                                enabled = isToggleEnabled,
                            )
                        }
                    } else {
                        CardTitle(
                            cardTitle = cardTitle,
                            icon = icon,
                            iconPainter = iconPainter,
                            modifier = Modifier.padding(top = 9.5.dp, bottom = 9.5.dp),
                        )
                    }
                }
                content(this)
            }
        }
    }
}

@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    cardTitle: String = "",
    iconPainter: Painter,
    addToggle: Boolean = false,
    isToggleChecked: Boolean = false,
    isToggleEnabled: Boolean = true,
    addPadding: Boolean = true,
    cardColor: Color = Color.Unspecified,
    onCheckedChange: ((Boolean) -> Unit) = {},
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable (ColumnScope) -> Unit,
) {
    CardBox(
        modifier = modifier,
        cardTitle = cardTitle,
        icon = null,
        iconPainter = iconPainter,
        addToggle = addToggle,
        isToggleChecked = isToggleChecked,
        isToggleEnabled = isToggleEnabled,
        addPadding = addPadding,
        cardColor = cardColor,
        onCheckedChange = onCheckedChange,
        roundedCornerShape = roundedCornerShape,
        content = content,
    )
}
