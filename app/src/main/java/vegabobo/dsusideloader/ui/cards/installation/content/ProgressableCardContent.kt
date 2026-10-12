package vegabobo.dsusideloader.ui.cards.installation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton

@Composable
fun ProgressableCardContent(
    text: String,
    showProgressBar: Boolean = false,
    isIndeterminate: Boolean = false,
    progress: Float = 0F,
    textFirstButton: String = "",
    textSecondButton: String = "",
    textThirdButton: String = "",
    onClickFirstButton: (() -> Unit)? = null,
    onClickSecondButton: (() -> Unit)? = null,
    onClickThirdButton: (() -> Unit)? = null,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
    )
    AnimatedVisibility(visible = showProgressBar) {
        val progressBarModifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 5.dp)
            .height(14.dp)
            .clip(RoundedCornerShape(8.dp))
        if (isIndeterminate) {
            LinearProgressIndicator(
                modifier = progressBarModifier,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            LinearProgressIndicator(
                modifier = progressBarModifier,
                progress = progress,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
    if (showProgressBar && !isIndeterminate && progress > 0F) {
        Text(
            text = "${(progress * 100).toInt()}%",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(modifier = Modifier.padding(top = 4.dp))
    Column(
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (onClickFirstButton != null) {
            PrimaryButton(
                text = textFirstButton,
                onClick = onClickFirstButton,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (onClickSecondButton != null) {
            SecondaryButton(
                text = textSecondButton,
                onClick = onClickSecondButton,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (onClickThirdButton != null) {
            SecondaryButton(
                text = textThirdButton,
                onClick = onClickThirdButton,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
