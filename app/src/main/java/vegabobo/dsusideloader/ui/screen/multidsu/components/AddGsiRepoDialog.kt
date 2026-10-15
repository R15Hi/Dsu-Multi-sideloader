package vegabobo.dsusideloader.ui.screen.multidsu.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import vegabobo.dsusideloader.ui.components.frostedglass.ApplyWindowBlurEffect
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButton
import vegabobo.dsusideloader.ui.components.frostedglass.GlassButtonVariant
import vegabobo.dsusideloader.ui.components.frostedglass.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGsiRepoDialog(
    onDismissRequest: () -> Unit,
    onAddRepo: (String) -> Unit,
) {
    var repoUrl by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Dialog(onDismissRequest = onDismissRequest) {
        ApplyWindowBlurEffect(blurRadius = 24.dp)
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            cornerRadius = 24.dp,
            opacity = 0.92f,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add GSI Repository",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Enter a GitHub repository or user profile URL to automatically index their GSI builds (e.g. https://github.com/username/repo or https://github.com/user).",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = {
                        repoUrl = it
                        isError = false
                    },
                    label = { Text("Repository or User URL") },
                    placeholder = { Text("https://github.com/...") },
                    singleLine = true,
                    isError = isError,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = if (isDark) Color(0x20FFFFFF) else Color(0x0A000000),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isError) {
                    Text(
                        text = "Please enter a valid GitHub repository or user link.",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassButton(
                        text = "Cancel",
                        variant = GlassButtonVariant.SECONDARY,
                        onClick = onDismissRequest,
                        height = 42.dp,
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    GlassButton(
                        text = "Add Repository",
                        variant = GlassButtonVariant.PRIMARY,
                        onClick = {
                            val trimmed = repoUrl.trim()
                            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                                onAddRepo(trimmed)
                                onDismissRequest()
                            } else {
                                isError = true
                            }
                        },
                        height = 42.dp,
                    )
                }
            }
        }
    }
}
