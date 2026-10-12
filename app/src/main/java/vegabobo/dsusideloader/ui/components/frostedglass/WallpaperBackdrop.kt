package vegabobo.dsusideloader.ui.components.frostedglass

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * Renders the device's actual active system wallpaper frosted backdrop
 * with zero flicker and zero swipe lag, powered by WallpaperHelper.
 */
@Composable
fun WallpaperBackdrop(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var wallpaperBitmap by remember {
        mutableStateOf<Bitmap?>(WallpaperHelper.getCachedWallpaper() ?: WallpaperHelper.getInitialFastBackdrop(context))
    }

    LaunchedEffect(Unit) {
        val blurred = WallpaperHelper.getBlurredWallpaper(context)
        wallpaperBitmap = blurred
    }

    Box(modifier = modifier.fillMaxSize()) {
        val bitmap = wallpaperBitmap
        if (bitmap != null && !bitmap.isRecycled) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Light, authentic frosted scrim that lets the wallpaper's details and colors show through
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x1F090C13),
                            Color(0x1410141E),
                            Color(0x240C101A),
                        ),
                    ),
                ),
        )

        // Subtle top ambient shade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x1A000000),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = 160f,
                    ),
                ),
        )
    }
}
