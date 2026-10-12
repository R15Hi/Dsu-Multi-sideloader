package vegabobo.dsusideloader.ui.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.components.CardTitle
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton

@Composable
fun DsuInfoCard(
    modifier: Modifier = Modifier,
    onClickViewDocs: () -> Unit,
    onClickLearnMore: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    CardBox(
        modifier = modifier,
        cardTitle = "",
        addPadding = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardTitle(
                cardTitle = stringResource(id = R.string.what_is_dsu),
                iconPainter = painterResource(id = R.drawable.ic_settings_info),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand",
                modifier = Modifier.rotate(rotationState),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = stringResource(id = R.string.what_is_dsu_description),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    Spacer(modifier = Modifier.weight(1F))
                    SecondaryButton(
                        text = stringResource(id = R.string.view_docs),
                        onClick = onClickViewDocs,
                    )
                    Spacer(modifier = Modifier.padding(start = 10.dp))
                    SecondaryButton(
                        text = stringResource(id = R.string.learn_more),
                        onClick = onClickLearnMore,
                    )
                }
            }
        }
    }
}
