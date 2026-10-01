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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        addPadding = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 42.sp, fontWeight = FontWeight.Bold)) {
                            append("${storageStats.usedGb}")
                        }
                        withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)) {
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

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = storageStats.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
