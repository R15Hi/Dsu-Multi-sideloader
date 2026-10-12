package vegabobo.dsusideloader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Purplish80,
    background = Color(0xFF13131A),
    surface = Color(0xFF1B1B24),
    surfaceVariant = Color(0xFF242432),
    onBackground = Color(0xFFE8E8F0),
    onSurface = Color(0xFFE8E8F0),
    onSurfaceVariant = Color(0xFFC0C0D0),
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Purplish40,
    background = Color(0xFFF6F7FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEF0F6),
    onBackground = Color(0xFF16181F),
    onSurface = Color(0xFF16181F),
    onSurfaceVariant = Color(0xFF4A4D58),
)

@Composable
fun DSUHelperTheme(
    themeStyle: String = "dynamic",
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeStyle) {
        "dark", "amoled", "glassy" -> true
        "light" -> false
        else -> systemDark
    }

    val context = LocalContext.current
    var colorScheme = when {
        themeStyle == "dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    if (themeStyle == "amoled") {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color(0xFF0C0C10),
            surfaceVariant = Color(0xFF16161C),
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFFA8A8B8),
        )
    } else if (themeStyle == "glassy") {
        colorScheme = colorScheme.copy(
            primary = Color(0xFF70B8FF),
            primaryContainer = Color(0x593B82F6),
            secondary = Color(0xFF80DEEA),
            secondaryContainer = Color(0x4D00BCD4),
            background = Color(0x18000000),
            surface = Color(0x80141824),
            surfaceVariant = Color(0x59263044),
            onBackground = Color(0xFFF2F5FA),
            onSurface = Color(0xFFF2F5FA),
            onSurfaceVariant = Color(0xFFD2D8E4),
            outline = Color(0x4DFFFFFF),
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            val window = activity.window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            val params = window.attributes
            if (themeStyle == "glassy") {
                vegabobo.dsusideloader.ui.components.frostedglass.WallpaperHelper.applyTranslucentWindow(activity)
            } else {
                params.flags = params.flags and android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER.inv()
                window.attributes = params
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        window.setBackgroundBlurRadius(0)
                    } catch (_: Throwable) {}
                }
                val isAmoled = themeStyle == "amoled"
                val bgColor = if (isAmoled) {
                    android.graphics.Color.BLACK
                } else if (isDark) {
                    android.graphics.Color.parseColor("#1C1B1F")
                } else {
                    android.graphics.Color.parseColor("#FFFBFE")
                }
                window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
