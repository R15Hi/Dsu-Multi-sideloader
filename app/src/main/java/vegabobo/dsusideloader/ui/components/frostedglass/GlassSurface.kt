package vegabobo.dsusideloader.ui.components.frostedglass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.ui.theme.GlassTokens

/**
 * Reusable GlassSurface component for iOS frosted-glass panels.
 * Efficiently layers translucent tint, specular sheen, and hairline border
 * without performing redundant heavy multi-pass blurs on every element.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerRadiusCard,
    shape: Shape = RoundedCornerShape(cornerRadius),
    tint: Color? = null,
    opacity: Float = GlassTokens.GlassCardOpacity,
    borderWidth: Dp = 0.dp,
    borderBrush: Brush? = null,
    borderColor: Color? = null,
    specularSheen: Boolean = false,
    shadowElevation: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val baseTint = tint ?: if (isDark) {
        Color(0xFF141828).copy(alpha = opacity)
    } else {
        Color(0xFFFFFFFF).copy(alpha = opacity.coerceAtLeast(0.70f))
    }

    val effectiveBorderBrush = borderBrush
    val effectiveBorderColor = borderColor ?: Color.Transparent

    val shadowModifier = if (shadowElevation > 0.dp) {
        Modifier.shadow(elevation = shadowElevation, shape = shape, clip = true)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape),
    ) {
        // 1. Translucent Base Tint
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(baseTint),
        )

        // 2. Specular Sheen (simulates top light reflection on glass surface)
        if (specularSheen) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        if (isDark) {
                            GlassTokens.SpecularHighlightBrush
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                            )
                        },
                    ),
            )
        }

        // 3. Hairline Border
        if (borderWidth > 0.dp) {
            if (effectiveBorderBrush != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(borderWidth, effectiveBorderBrush, shape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(borderWidth, effectiveBorderColor, shape),
                )
            }
        }

        // 4. Foreground Content with guaranteed contrast
        val contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
        ) {
            Box(
                modifier = Modifier.padding(contentPadding),
                content = content,
            )
        }
    }
}

/**
 * Reusable GlassCard component with standard card padding and corner radius.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CornerRadiusCard,
    tint: Color? = null,
    opacity: Float = GlassTokens.GlassCardOpacity,
    borderWidth: Dp = GlassTokens.BorderWidth,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    GlassSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        tint = tint,
        opacity = opacity,
        borderWidth = borderWidth,
        contentPadding = contentPadding,
        content = content,
    )
}
