package vegabobo.dsusideloader.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AvatarCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 16 // 1/16th of available memory
    val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
}

@Composable
fun UserAvatar(
    avatarUrl: String,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    cornerRadius: Dp = 12.dp,
    hasUpdatedBadge: Boolean = false,
) {
    var bitmap by remember(avatarUrl) {
        mutableStateOf<Bitmap?>(if (avatarUrl.isNotBlank()) AvatarCache.memoryCache.get(avatarUrl) else null)
    }

    LaunchedEffect(avatarUrl) {
        if (avatarUrl.isNotBlank() && bitmap == null) {
            val cached = AvatarCache.memoryCache.get(avatarUrl)
            if (cached != null) {
                bitmap = cached
            } else {
                withContext(Dispatchers.IO) {
                    try {
                        val url = URL(avatarUrl)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 6000
                        conn.readTimeout = 6000
                        conn.doInput = true
                        conn.connect()
                        if (conn.responseCode in 200..299) {
                            val input = conn.inputStream
                            val decoded = BitmapFactory.decodeStream(input)
                            input.close()
                            if (decoded != null) {
                                AvatarCache.memoryCache.put(avatarUrl, decoded)
                                withContext(Dispatchers.Main) {
                                    bitmap = decoded
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Fallback will render initial
                    }
                }
            }
        }
    }

    val shape = RoundedCornerShape(cornerRadius)

    val borderColor = if (hasUpdatedBadge) Color(0xFF10B981) else Color(0x3338BDF8)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null && !currentBitmap.isRecycled) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            // Elegant fallback gradient with Initial Letter
            val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "G"
            val fallbackBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF2563EB),
                    Color(0xFF7C3AED),
                ),
            )
            Box(
                modifier = Modifier
                    .size(size)
                    .background(fallbackBrush),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42f).sp,
                )
            }
        }
    }
}
