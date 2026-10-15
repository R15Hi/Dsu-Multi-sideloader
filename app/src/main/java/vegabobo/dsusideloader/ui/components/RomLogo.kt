package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun RomLogo(
    romName: String,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    val cleanName = romName.lowercase(Locale.ROOT)

    // Determine ROM Brand styling (Gradients, Brand Monograms & Colors)
    val (gradientColors, brandLabel, textColor) = when {
        cleanName.contains("croid") || cleanName.contains("crdroid") -> Triple(
            listOf(Color(0xFFFF5722), Color(0xFFD32F2F)),
            "cr",
            Color.White,
        )
        cleanName.contains("rising") || cleanName.contains("risingos") -> Triple(
            listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
            "rOS",
            Color.White,
        )
        cleanName.contains("restless") -> Triple(
            listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
            "R⚡",
            Color.White,
        )
        cleanName.contains("lineage") || cleanName.contains("los") -> Triple(
            listOf(Color(0xFF0D9488), Color(0xFF0284C7)),
            "LOS",
            Color.White,
        )
        cleanName.contains("pixel") -> Triple(
            listOf(Color(0xFF4285F4), Color(0xFF34A853)),
            "G",
            Color.White,
        )
        cleanName.contains("aosp") || cleanName.contains("google") -> Triple(
            listOf(Color(0xFF3DDC84), Color(0xFF059669)),
            "AOSP",
            Color.Black,
        )
        cleanName.contains("cherish") -> Triple(
            listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
            "CH",
            Color.White,
        )
        cleanName.contains("evolution") || cleanName.contains("evo") -> Triple(
            listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
            "EVO",
            Color.White,
        )
        else -> Triple(
            listOf(Color(0xFF06B6D4), Color(0xFF2563EB)),
            "GSI",
            Color.White,
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3.2f))
            .background(Brush.linearGradient(gradientColors))
            .border(
                width = 1.dp,
                color = Color(0x4DFFFFFF),
                shape = RoundedCornerShape(size / 3.2f),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = brandLabel,
            color = textColor,
            fontSize = if (brandLabel.length > 3) (size.value * 0.32f).sp else (size.value * 0.42f).sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            lineHeight = (size.value * 0.45f).sp,
        )
    }
}
