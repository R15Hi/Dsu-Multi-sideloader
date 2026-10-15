package vegabobo.dsusideloader.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.frostedglass.WallpaperHelper
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingBottomBar(
    pagerState: androidx.compose.foundation.pager.PagerState,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isGlassy = MaterialTheme.colorScheme.background.alpha < 1f
    val isAmoled = MaterialTheme.colorScheme.surface.luminance() < 0.03f

    var wallpaperBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(
            WallpaperHelper.getCachedWallpaper() ?: WallpaperHelper.getInitialFastBackdrop(context, blurRadius = 55),
        )
    }

    LaunchedEffect(isGlassy) {
        if (isGlassy) {
            val blurred = WallpaperHelper.getBlurredWallpaper(context, blurRadius = 55)
            wallpaperBitmap = blurred
        }
    }

    val pillBorderBrush = if (isGlassy) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.40f), // Top specular rim highlight
                Color.White.copy(alpha = 0.18f), // Mid translucency
                Color.White.copy(alpha = 0.08f), // Bottom subtle edge
            ),
        )
    } else {
        SolidColor(if (isDark) Color(0x24FFFFFF) else Color(0x18000000))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Floating glass capsule pill (Liquid Glass "Frosted (with tint)" theme combination)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 12.dp, // Shadow strength: 12
                    shape = CircleShape,
                    spotColor = if (isGlassy) Color.Black.copy(alpha = 0.24f) else Color(0x30000000), // 24% spot shadow
                    ambientColor = if (isGlassy) Color.Black.copy(alpha = 0.12f) else Color(0x1F000000), // 12% ambient shadow
                )
                .clip(CircleShape)
                .border(
                    width = if (isGlassy) 1.2.dp else 1.dp,
                    brush = pillBorderBrush,
                    shape = CircleShape,
                ),
        ) {
            if (isGlassy) {
                // Layer 1: Real sampled blurred wallpaper backdrop slice with Glass Blur = 55 and Contrast Boost = 40
                val bmp = wallpaperBitmap
                val blurPaint = remember {
                    val contrastBoost = 1.20f // 40% Contrast Boost from Liquid Glass shader: (rgb - 0.5) * 1.20 + 0.5
                    val t = (1f - contrastBoost) / 2f * 255f
                    val contrastMatrix = android.graphics.ColorMatrix(
                        floatArrayOf(
                            contrastBoost, 0f, 0f, 0f, t,
                            0f, contrastBoost, 0f, 0f, t,
                            0f, 0f, contrastBoost, 0f, t,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    )
                    android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG).apply {
                        colorFilter = android.graphics.ColorMatrixColorFilter(contrastMatrix)
                    }
                }
                if (bmp != null && !bmp.isRecycled) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val canvasW = size.width.toInt()
                        val canvasH = size.height.toInt()
                        val bmpW = bmp.width
                        val bmpH = bmp.height
                        val srcTop = (bmpH * 0.82f).toInt().coerceIn(0, bmpH - 1)
                        val srcBottom = (bmpH * 0.98f).toInt().coerceIn(srcTop + 1, bmpH)
                        val srcRect = android.graphics.Rect(0, srcTop, bmpW, srcBottom)
                        val dstRect = android.graphics.Rect(0, 0, canvasW, canvasH)
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawBitmap(bmp, srcRect, dstRect, blurPaint)
                        }
                    }
                }

                // Layer 2: Frosted Acrylic Base Diffusion Layer (smokes the background for depth and contrast)
                val acrylicBase = if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0x38101827),
                            Color(0x450A0F1D),
                        ),
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0x35FFFFFF),
                            Color(0x25F1F5F9),
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(acrylicBase),
                )

                // Layer 3: Panel Color Opacity 25 - Liquid Glass Tint Color mixed at 25% (0.25f) opacity
                val tintColor = MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    tintColor.copy(alpha = 0.28f),
                                    tintColor.copy(alpha = 0.22f),
                                ),
                            ),
                        ),
                )

                // Layer 4: Top Specular Rim Sheen (light catching the top curved rim of the glass)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.White.copy(alpha = 0.06f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
            } else {
                // Non-glassy themes: 100% solid, fully opaque background with zero bleed-through
                val solidBg = if (isDark) {
                    if (isAmoled) Color(0xFF14141A) else Color(0xFF1B2232)
                } else {
                    Color(0xFFFFFFFF)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SolidColor(solidBg)),
                )
            }

            val navItems = remember {
                listOf(
                    Triple(0, R.drawable.ic_home, "Installed"),
                    Triple(1, R.drawable.ic_list, "Multi DSU"),
                    Triple(2, R.drawable.ic_settings_new_releases, "GSI Hub"),
                    Triple(3, R.drawable.ic_settings, "Settings"),
                )
            }

            val activeIndicatorBrush = if (isGlassy) {
                Brush.verticalGradient(
                    listOf(
                        Color(0x55FFFFFF),
                        Color(0x3838BDF8),
                        Color(0x2B2563EB),
                    ),
                )
            } else {
                SolidColor(MaterialTheme.colorScheme.secondaryContainer)
            }

            val activeIndicatorBorder = if (isGlassy) {
                Brush.verticalGradient(
                    listOf(
                        Color(0x85FFFFFF),
                        Color(0x4038BDF8),
                    ),
                )
            } else {
                SolidColor(Color.Transparent)
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
            ) {
                val tabWidth = maxWidth / navItems.size
                val selectedIndex by remember(pagerState) {
                    derivedStateOf { pagerState.currentPage.coerceIn(0, navItems.size - 1) }
                }

                // Animated selected tab capsule (hardware-accelerated 1:1 real-time drag/scroll tracking)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(tabWidth)
                        .offset {
                            val position = (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, (navItems.size - 1).toFloat())
                            val x = (tabWidth.toPx() * position).roundToInt()
                            IntOffset(x = x, y = 0)
                        }
                        .padding(horizontal = 3.dp)
                        .clip(CircleShape)
                        .background(activeIndicatorBrush)
                        .border(
                            width = if (isGlassy) 1.dp else 0.dp,
                            brush = activeIndicatorBorder,
                            shape = CircleShape,
                        ),
                )

                // Interactive tab items
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    navItems.forEach { (pageIndex, icon, label) ->
                        val isSelected = selectedIndex == pageIndex

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 0.94f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing,
                            ),
                        )

                        val contentAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.65f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing,
                            ),
                        )

                        val activeColor = if (isGlassy) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        val inactiveColor = if (isGlassy) Color(0x99FFFFFF) else MaterialTheme.colorScheme.onSurfaceVariant
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) activeColor else inactiveColor,
                            animationSpec = tween(durationMillis = 200),
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pageIndex)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = contentAlpha
                                },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = label,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    color = contentColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TabsScreen(navigate: (String) -> Unit) {
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            FloatingBottomBar(pagerState = pagerState)
        },
    ) { _ ->
        HorizontalPager(
            pageCount = 4,
            state = pagerState,
            beyondBoundsPageCount = 0,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> Home(navigate = { navigate(it) })
                1 -> MultiDsuScreen(
                    navigate = { navigate(it) },
                    onNavigateToHome = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                )
                2 -> vegabobo.dsusideloader.ui.screen.gsihub.GsiHubScreen(
                    navigate = { navigate(it) },
                    onNavigateToHome = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                )
                3 -> vegabobo.dsusideloader.ui.screen.settings.Settings(
                    navigate = { navigate(it) },
                    showBackButton = false,
                )
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
        composable(Destinations.Preferences) {
            vegabobo.dsusideloader.ui.screen.settings.Settings(
                navigate = { navigate(it) },
                showBackButton = true,
            )
        }
        composable(Destinations.ADBInstallation) { AdbScreen(navigate = { navigate(it) }) }
        composable(Destinations.About) { AboutScreen(navigate = { navigate(it) }) }
        composable(Destinations.Libraries) { LibrariesScreen(navigate = { navigate(it) }) }
    }
}
