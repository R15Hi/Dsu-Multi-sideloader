package vegabobo.dsusideloader.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.frostedglass.ApplyWindowBlurEffect
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface
import vegabobo.dsusideloader.ui.theme.GlassTokens

data class ThemeOptionItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
)

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onSelectTheme: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBlurEffect(blurRadius = 32.dp)
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            cornerRadius = GlassTokens.CornerRadiusDialog,
            opacity = if (isDark) 0.90f else 0.96f,
            shadowElevation = 0.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x333B82F6) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings_color_lens),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "App Theme",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Choose your visual aesthetic",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                val themeOptions = listOf(
                    ThemeOptionItem(
                        key = "dynamic",
                        title = "Dynamic",
                        subtitle = "Follows system colors",
                        icon = Icons.Outlined.WbSunny,
                        iconTint = Color(0xFFFBBF24),
                    ),
                    ThemeOptionItem(
                        key = "dark",
                        title = "Dark",
                        subtitle = "Clean and consistent dark theme",
                        icon = Icons.Outlined.DarkMode,
                        iconTint = Color(0xFFA78BFA),
                    ),
                    ThemeOptionItem(
                        key = "glassy",
                        title = "Glassy (iOS Style)",
                        subtitle = "Translucent, wallpaper based interface with glass effect",
                        icon = Icons.Outlined.AutoAwesome,
                        iconTint = Color(0xFF38BDF8),
                    ),
                    ThemeOptionItem(
                        key = "light",
                        title = "Light",
                        subtitle = "Clean, high-contrast bright theme",
                        icon = Icons.Outlined.LightMode,
                        iconTint = Color(0xFFF59E0B),
                    ),
                    ThemeOptionItem(
                        key = "amoled",
                        title = "AMOLED",
                        subtitle = "Pure pitch black for OLED displays",
                        icon = Icons.Outlined.Contrast,
                        iconTint = if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                    ),
                )

                themeOptions.forEach { item ->
                    val isSelected = currentTheme.lowercase() == item.key

                    val itemBg = if (isSelected) {
                        if (isDark) Color(0x382563EB) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        if (isDark) Color(0x14FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    }

                    val itemBorder = if (isSelected) {
                        if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = itemBorder,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .background(itemBg)
                            .clickable {
                                onSelectTheme(item.key)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.iconTint.copy(alpha = if (isDark) 0.16f else 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconTint,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = "Selected",
                                tint = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
