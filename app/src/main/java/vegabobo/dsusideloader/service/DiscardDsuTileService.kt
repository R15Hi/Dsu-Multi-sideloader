package vegabobo.dsusideloader.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.topjohnwu.superuser.Shell

/**
 * Quick Settings Tile to discard (wipe) the currently installed DSU.
 * Tapping it runs `gsi_tool wipe` which removes the DSU without rebooting.
 */
class DiscardDsuTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        try {
            if (Shell.getShell().isRoot) {
                val check = Shell.cmd("ls /data/gsi/dsu/dsu").exec().out.joinToString("")
                if (check.contains("system_gsi") || check.contains("userdata_gsi")) {
                    tile.state = Tile.STATE_INACTIVE
                    tile.label = "Discard DSU"
                    tile.subtitle = "DSU installed"
                } else {
                    tile.state = Tile.STATE_UNAVAILABLE
                    tile.label = "Discard DSU"
                    tile.subtitle = "No DSU"
                }
            } else {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.label = "Discard DSU"
                tile.subtitle = "Root required"
            }
        } catch (e: Exception) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Discard DSU"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (tile.state == Tile.STATE_UNAVAILABLE) return

        try {
            if (Shell.getShell().isRoot) {
                Shell.cmd("gsi_tool wipe").exec()
                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle = "Discarded"
                tile.updateTile()
            }
        } catch (e: Exception) {
            tile.subtitle = "Failed"
            tile.updateTile()
        }
    }
}
