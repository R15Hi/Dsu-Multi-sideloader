package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    barTitle: String,
    onClickBackButton: (() -> Unit)? = null,
) {
    val fraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    LargeTopAppBar(
        modifier = androidx.compose.ui.Modifier.clip(
            androidx.compose.foundation.shape.RoundedCornerShape(
                bottomStart = 24.dp,
                bottomEnd = 24.dp,
            ),
        ),
        title = {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                Text(
                    text = barTitle,
                    modifier = androidx.compose.ui.Modifier.align(
                        androidx.compose.ui.BiasAlignment(
                            horizontalBias = -1f + (fraction * 1f),
                            verticalBias = 0f,
                        ),
                    ),
                )
            }
        },
        navigationIcon = {
            if (onClickBackButton != null) {
                IconButton(onClickBackButton) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        actions = {},
        scrollBehavior = scrollBehavior,
    )
}
