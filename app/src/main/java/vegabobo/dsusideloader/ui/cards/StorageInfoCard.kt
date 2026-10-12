package vegabobo.dsusideloader.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.util.StorageUtils

@Composable
fun StorageInfoCard(
    modifier: Modifier = Modifier,
) {
    var storageStats by remember { mutableStateOf(StorageUtils.StorageStats(0, 0, 0, 0f)) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            storageStats = StorageUtils.getStorageStats()
        }
    }

    CardBox(
        modifier = modifier,
        cardTitle = "Storage",
        iconPainter = painterResource(id = R.drawable.ic_storage),
        addPadding = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold)) {
                            append("${storageStats.usedGb}")
                        }
                        withStyle(style = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)) {
                            append(" GB used")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "${storageStats.totalGb} GB total",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = storageStats.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF10B981),
                trackColor = if (MaterialTheme.colorScheme.background.alpha < 1f) Color(0x28FFFFFF) else MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
