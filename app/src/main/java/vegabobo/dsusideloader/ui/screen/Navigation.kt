package vegabobo.dsusideloader.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import vegabobo.dsusideloader.ui.screen.about.AboutScreen
import vegabobo.dsusideloader.ui.screen.adb.AdbScreen
import vegabobo.dsusideloader.ui.screen.home.Home
import vegabobo.dsusideloader.ui.screen.libraries.LibrariesScreen
import vegabobo.dsusideloader.ui.screen.multidsu.MultiDsuScreen

object Destinations {
    const val Homepage = "home"
    const val Preferences = "preferences"
    const val ADBInstallation = "adb_installation"
    const val About = "about"
    const val Libraries = "libraries"
    const val Up = "up"
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabsScreen(navigate: (String) -> Unit) {
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                    label = { Text("Installation") },
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    icon = { Icon(Icons.Outlined.List, contentDescription = null) },
                    label = { Text("Multi DSU") },
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 2,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                )
            }
        },
    ) { innerPadding ->
        HorizontalPager(
            pageCount = 3,
            state = pagerState,
            beyondBoundsPageCount = 2,
            modifier = Modifier.padding(innerPadding),
        ) { page ->
            when (page) {
                0 -> Home(navigate = { navigate(it) })
                1 -> MultiDsuScreen(navigate = { navigate(it) })
                2 -> vegabobo.dsusideloader.ui.screen.settings.Settings(navigate = { navigate(it) })
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.Homepage,
    ) {
        fun navigate(destination: String) {
            if (destination == Destinations.Up) {
                navController.navigateUp()
            } else {
                navController.navigate(destination)
            }
        }

        composable(Destinations.Homepage) { TabsScreen(navigate = { navigate(it) }) }
        composable(Destinations.Preferences) { vegabobo.dsusideloader.ui.screen.settings.Settings(navigate = { navigate(it) }) }
        composable(Destinations.ADBInstallation) { AdbScreen(navigate = { navigate(it) }) }
        composable(Destinations.About) { AboutScreen(navigate = { navigate(it) }) }
        composable(Destinations.Libraries) { LibrariesScreen(navigate = { navigate(it) }) }
    }
}
