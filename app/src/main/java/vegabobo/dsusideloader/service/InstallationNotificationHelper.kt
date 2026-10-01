package vegabobo.dsusideloader.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import vegabobo.dsusideloader.R

/**
 * Manages installation progress notifications.
 * Creates a notification channel on first use and updates a persistent
 * notification with progress percentage during extraction and installation.
 */
object InstallationNotificationHelper {

    private const val CHANNEL_ID = "dsu_install_progress"
    private const val NOTIFICATION_ID = 9001

    private var channelCreated = false
    private var lastPostedPct = -1

    private fun ensureChannel(context: Context) {
        if (channelCreated) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel_install)
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows extraction and installation progress"
                setShowBadge(false)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
        channelCreated = true
    }

    /**
     * Shows or updates the progress notification.
     * Throttled: only posts when the integer percentage actually changes,
     * so at most ~100 notify() calls instead of thousands.
     */
    fun updateProgress(context: Context, title: String, progress: Float) {
        val pct = if (progress < 0f) -1 else (progress * 100).toInt().coerceIn(0, 100)

        // Skip if percentage hasn't changed — this is the key throttle
        if (pct == lastPostedPct) return
        lastPostedPct = pct

        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_mono)
            .setContentTitle(title)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)

        if (pct < 0) {
            builder.setProgress(0, 0, true)
            builder.setContentText("Processing…")
        } else {
            builder.setProgress(100, pct, false)
            builder.setContentText("$pct%")
        }

        nm.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Shows the completion notification and removes the ongoing flag.
     */
    fun showComplete(context: Context) {
        lastPostedPct = -1
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_mono)
            .setContentTitle(context.getString(R.string.notif_install_done))
            .setContentText("DSU is ready. Reboot to use it.")
            .setOngoing(false)
            .setAutoCancel(true)
        nm.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Cancels the notification entirely.
     */
    fun cancel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }
}
