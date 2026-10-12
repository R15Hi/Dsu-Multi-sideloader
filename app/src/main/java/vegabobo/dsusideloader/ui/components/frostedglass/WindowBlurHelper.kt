package vegabobo.dsusideloader.ui.components.frostedglass

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Utility to apply native Window-level background blur on Android 12+ (API 31+).
 * This blurs the content behind Dialogs, Popups, or Windows directly in hardware SurfaceFlinger.
 */
object WindowBlurHelper {

    fun applyWindowBlur(window: Window?, blurRadiusPx: Int = 80) {
        if (window == null) return
        try {
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            window.setDimAmount(0.55f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                val params = window.attributes
                params.blurBehindRadius = blurRadiusPx
                window.attributes = params
                window.setBackgroundBlurRadius(blurRadiusPx)
            }
        } catch (_: Throwable) {
            // Gracefully ignore on device ROMs where vendor disabled blur
        }
    }

    fun findWindow(view: View): Window? {
        var current: View? = view
        while (current != null) {
            if (current is DialogWindowProvider) {
                return current.window
            }
            val parent = current.parent
            current = if (parent is View) parent else null
        }

        var context: Context? = view.context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context.window
            }
            context = context.baseContext
        }
        return null
    }
}

/**
 * Composable that automatically enables hardware window blur behind the current dialog or window.
 */
@Composable
fun ApplyWindowBlurEffect(blurRadius: Dp = 28.dp) {
    val view = LocalView.current
    val density = LocalDensity.current
    val blurRadiusPx = with(density) { blurRadius.roundToPx() }

    SideEffect {
        val window = WindowBlurHelper.findWindow(view)
        WindowBlurHelper.applyWindowBlur(window, blurRadiusPx)
    }
}
