package vegabobo.dsusideloader.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.topjohnwu.superuser.Shell

/**
 * Quick Settings Tile to reboot directly into the installed DSU.
 * When active (DSU installed), tapping it runs `gsi_tool enable -s && reboot`.
 * When no DSU is installed, tile stays inactive.
 */
class RebootDsuTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        try {
            if (Shell.getShell().isRoot) {
                val check = Shell.cmd("ls /data/gsi/dsu/dsu").exec().out.joinToString("")
                if (check.contains("system_gsi") || check.contains("userdata_gsi")) {
                    tile.state = Tile.STATE_INACTIVE
                    tile.label = "Reboot to DSU"
                    tile.subtitle = "DSU ready"
                } else {
                    tile.state = Tile.STATE_UNAVAILABLE
                    tile.label = "Reboot to DSU"
                    tile.subtitle = "No DSU installed"
                }
            } else {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.label = "Reboot to DSU"
                tile.subtitle = "Root required"
            }
        } catch (e: Exception) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Reboot to DSU"
            tile.subtitle = "Error"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (tile.state == Tile.STATE_UNAVAILABLE) return

        try {
            if (Shell.getShell().isRoot) {
                tile.state = Tile.STATE_ACTIVE
                tile.subtitle = "Rebooting..."
                tile.updateTile()
                Shell.cmd("gsi_tool enable -s").exec()
                Shell.cmd("reboot").exec()
            }
        } catch (e: Exception) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.subtitle = "Failed"
            tile.updateTile()
        }
    }
}
