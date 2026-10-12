package vegabobo.dsusideloader.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized theme tokens for the Glassy iOS-Style theme.
 * Defines opacities, corner radii, borders, highlight brushes, and accent colors.
 */
object GlassTokens {
    // Corner Radii
    val CornerRadiusCard: Dp = 24.dp
    val CornerRadiusButton: Dp = 20.dp
    val CornerRadiusPill: Dp = 32.dp
    val CornerRadiusDialog: Dp = 28.dp
    val CornerRadiusSheet: Dp = 28.dp

    // Border and Stroke Tokens
    val BorderWidth: Dp = 1.dp
    val HairlineBorderColor: Color = Color(0x33FFFFFF)
    val SubtleBorderColor: Color = Color(0x1EFFFFFF)

    // Opacity Tokens
    const val GlassCardOpacity: Float = 0.42f
    const val GlassSurfaceOpacity: Float = 0.50f
    const val GlassPillOpacity: Float = 0.35f
    const val GlassModalOpacity: Float = 0.88f

    // Accent Colors
    val AccentCyan: Color = Color(0xFF06B6D4)
    val AccentBlue: Color = Color(0xFF3B82F6)
    val AccentViolet: Color = Color(0xFF8B5CF6)
    val AccentGreen: Color = Color(0xFF10B981)
    val AccentRed: Color = Color(0xFFEF4444)

    // Base Glass Tint (iOS Midnight Glass)
    val GlassSurfaceTint: Color = Color(0x28101426)
    val GlassCardTint: Color = Color(0x32161A2E)

    // Specular Sheen Brush
    val SpecularHighlightBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.14f),
            Color.White.copy(alpha = 0.03f),
            Color.Transparent,
        ),
    )

    // Hairline Border Gradient Brush
    val BorderHighlightBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.40f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.04f),
        ),
    )

    // Primary Neon Action Gradient (Violet to Cyan)
    val NeonProgressGradient: Brush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF8B5CF6), // Violet
            Color(0xFF3B82F6), // Blue
            Color(0xFF06B6D4), // Cyan
            Color(0xFF8B5CF6), // Violet
        ),
    )

    val PrimaryButtonGradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2563EB), // Rich Blue
            Color(0xFF0284C7), // Sky Blue
            Color(0xFF06B6D4), // Vibrant Cyan
        ),
    )

    val SuccessButtonGradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF059669), // Emerald
            Color(0xFF10B981), // Green
        ),
    )

    val DangerButtonGradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFDC2626), // Crimson
            Color(0xFFEF4444), // Coral Red
        ),
    )
}
