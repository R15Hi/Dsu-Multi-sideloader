package vegabobo.dsusideloader.ui.components.frostedglass

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vegabobo.dsusideloader.ui.theme.GlassTokens

enum class GlassButtonVariant {
    PRIMARY,
    SUCCESS,
    DANGER,
    NEUTRAL,
    SECONDARY,
}

/**
 * Reusable GlassButton component for iOS frosted glass buttons.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    variant: GlassButtonVariant = GlassButtonVariant.PRIMARY,
    enabled: Boolean = true,
    cornerRadius: Dp = GlassTokens.CornerRadiusButton,
    height: Dp = 50.dp,
    fontSize: TextUnit = 15.sp,
    horizontalPadding: Dp = 16.dp,
    iconSize: Dp = 20.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val baseAlpha = if (enabled) 1f else 0.45f

    val backgroundBrush: Brush?
    val backgroundColor: Color
    val borderColor: Color
    val contentColor: Color

    when (variant) {
        GlassButtonVariant.PRIMARY -> {
            backgroundBrush = GlassTokens.PrimaryButtonGradient
            backgroundColor = Color.Transparent
            borderColor = Color(0x66FFFFFF)
            contentColor = Color.White
        }
        GlassButtonVariant.SUCCESS -> {
            backgroundBrush = GlassTokens.SuccessButtonGradient
            backgroundColor = Color.Transparent
            borderColor = if (isDark) Color(0x66A7F3D0) else Color(0x3310B981)
            contentColor = Color.White
        }
        GlassButtonVariant.DANGER -> {
            backgroundBrush = null
            backgroundColor = if (isDark) Color(0x28EF4444) else Color(0x1CEF4444)
            borderColor = if (isDark) Color(0x66EF4444) else Color(0x40EF4444)
            contentColor = if (isDark) Color(0xFFFF8B8B) else Color(0xFFDC2626)
        }
        GlassButtonVariant.NEUTRAL -> {
            backgroundBrush = null
            backgroundColor = if (isDark) Color(0x20FFFFFF) else Color(0x12000000)
            borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x20000000)
            contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        }
        GlassButtonVariant.SECONDARY -> {
            backgroundBrush = null
            backgroundColor = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
            borderColor = if (isDark) Color(0x24FFFFFF) else Color(0x18000000)
            contentColor = if (isDark) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    val animatedContentColor by animateColorAsState(targetValue = contentColor.copy(alpha = baseAlpha))

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .then(
                if (backgroundBrush != null) {
                    Modifier.background(backgroundBrush)
                } else {
                    Modifier.background(backgroundColor.copy(alpha = backgroundColor.alpha * baseAlpha))
                },
            )
            .border(
                width = GlassTokens.BorderWidth,
                color = borderColor.copy(alpha = borderColor.alpha * baseAlpha),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val spacerWidth = if (iconSize < 16.dp) 4.dp else 8.dp
            if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(Modifier.width(spacerWidth))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedContentColor,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(Modifier.width(spacerWidth))
            }
            Text(
                text = text,
                color = animatedContentColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
