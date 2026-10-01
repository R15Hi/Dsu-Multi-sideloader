package vegabobo.dsusideloader.util

import android.os.Environment
import android.os.StatFs

class StorageUtils {

    data class StorageStats(
        val totalGb: Int,
        val usedGb: Int,
        val availableGb: Int,
        val progress: Float,
    )

    companion object {

        fun getAllocInfo(allowedPercentage: Float): Pair<Boolean, Int> {
            val statFs = StatFs(Environment.getDataDirectory().absolutePath)
            val blockSize = statFs.blockSizeLong
            val totalSize = statFs.blockCountLong * blockSize
            val availableSize = statFs.availableBlocksLong * blockSize
            val hasAvailableStorage =
                availableSize.toFloat() / totalSize.toFloat() > allowedPercentage
            var availSizeInGb =
                (availableSize / 1024L / 1024L / 1024L).toInt()

            // Reserve 4GB (4 is a totally arbitrary number).
            // Let say user pick a "img" file to install, this file, will be packed to "gz"
            // the new created file, will take some space, because of that, we reserve something here.
            // We may fix it depending on what user is installing in future.
            if (availSizeInGb >= 6) {
                availSizeInGb -= 4
            }

            val maximumAllowedForAllocation = availSizeInGb / 2
            return Pair(hasAvailableStorage, maximumAllowedForAllocation)
        }

        fun getStorageStats(): StorageStats {
            val statFs = StatFs(Environment.getDataDirectory().absolutePath)
            val blockSize = statFs.blockSizeLong
            val totalSize = statFs.blockCountLong * blockSize
            val availableSize = statFs.availableBlocksLong * blockSize
            val usedSize = totalSize - availableSize

            val totalGb = (totalSize / 1024L / 1024L / 1024L).toInt()
            val usedGb = (usedSize / 1024L / 1024L / 1024L).toInt()
            val availableGb = (availableSize / 1024L / 1024L / 1024L).toInt()
            val progress = usedSize.toFloat() / totalSize.toFloat()

            return StorageStats(totalGb, usedGb, availableGb, progress)
        }
    }
}
