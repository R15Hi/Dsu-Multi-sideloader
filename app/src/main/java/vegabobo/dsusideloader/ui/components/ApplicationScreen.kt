package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(0.dp),
    columnContent: Boolean = true,
    enableDefaultScrollBehavior: Boolean = true,
    topBar: @Composable (TopAppBarScrollBehavior?) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    outsideContent: @Composable (PaddingValues) -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val insets = WindowInsets
        .systemBars
        .only(WindowInsetsSides.Vertical)
        .asPaddingValues()

    Surface(
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxSize(),
            topBar = { topBar(null) },
            bottomBar = { bottomBar() },
            content = { innerPadding ->
                val scrollState = rememberScrollState()
                val scrollModifier =
                    if (enableDefaultScrollBehavior) Modifier.verticalScroll(scrollState) else Modifier
                if (columnContent) {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                            .then(scrollModifier),
                        verticalArrangement = verticalArrangement,
                    ) {
                        content()
                        Spacer(modifier = Modifier.height(130.dp))
                    }
                } else {
                    Surface(
                        color = Color.Transparent,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                            .then(scrollModifier),
                    ) {
                        Column {
                            content()
                            Spacer(modifier = Modifier.height(130.dp))
                        }
                    }
                }
            },
        )
    }

    outsideContent(insets)
}
