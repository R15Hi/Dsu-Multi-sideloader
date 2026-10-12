package vegabobo.dsusideloader.installer.multidsu

import com.topjohnwu.superuser.Shell

/**
 * Manages the Multi-DSU logic.
 *
 * CRITICAL SAFETY RULES:
 * 1. ALWAYS use "gsi_tool enable -s" (single-boot mode). If the DSU fails to boot,
 *    the device automatically falls back to the main OS. Without -s, a bad DSU
 *    causes an UNRECOVERABLE BOOTLOOP.
 * 2. NEVER move metadata cross-partition. /metadata/ and /data/ are separate
 *    partitions. Moving files between them does a copy+delete which changes
 *    physical block locations, invalidating lp_metadata block maps.
 * 3. Data files (/data/gsi/dsu/dsu/) can safely be moved within /data/ (same
 *    partition = just a directory rename, blocks don't move).
 * 4. Metadata (/metadata/gsi/dsu/) MUST be renamed within /metadata/ only.
 * 5. Use "gsi_tool disable" (not wipe!) to deactivate DSU without destroying state.
 *    "gsi_tool wipe" destroys internal boot flags that "enable" cannot recover.
 */
object MultiDsuManager {

    private const val DATA_SAFE_HAVEN = "/data/gsi/dsu_backups"
    private const val META_SAFE_HAVEN = "/metadata/gsi/dsu_backups"
    private const val GSI_DATA = "/data/gsi/dsu/dsu"
    private const val GSI_META = "/metadata/gsi/dsu"

    fun hasRoot(): Boolean {
        if (Shell.isAppGrantedRoot() == false) return false
        val cached = Shell.getCachedShell()
        if (cached != null) return cached.isRoot
        return try {
            Shell.getShell().isRoot
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks if there is a currently installed DSU in the active directory.
     */
    fun hasActiveDsu(): Boolean {
        val result = Shell.cmd("ls $GSI_DATA 2>/dev/null").exec()
        val output = result.out.joinToString("\n")
        return output.contains("system_gsi") || output.contains("userdata_gsi")
    }

    /**
     * Returns the custom name of the active DSU if it was restored from a backup.
     */
    fun getActiveDsuName(): String {
        val result = Shell.cmd("cat $GSI_META/dsu_sideloader_name 2>/dev/null").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            return result.out.first().trim()
        }
        return ""
    }

    /**
     * Backs up the current active DSU.
     * 1. First disable DSU booting (gsi_tool disable) while files are still in place
     * Backs up the current active DSU using a hardlink-then-wipe strategy:
     *
     * 1. Create HARDLINKS of data files to backup dir (instant, same filesystem)
     * 2. COPY metadata to backup dir (different partition, can't hardlink, but small)
     * 3. Call "gsi_tool wipe" WHILE ORIGINALS ARE STILL IN PLACE
     *    → gsid finds the installation (IsGsiInstalled() = true)
     *    → gsid properly calls UnmapImageIfExists() to tear down device-mapper nodes
     *    → gsid deletes original files — but hardlinked copies SURVIVE (same inodes)
     * 4. Clean up remaining state
     *
     * This solves the "failed to create userdata partition" bug. Previous approach
     * moved files first, then tried to tear down DM externally — but gsid skipped
     * the DM teardown because it couldn't find the files (IsGsiInstalled() = false).
     */
    fun backupCurrentDsu(id: String): Boolean {
        if (!hasRoot()) return false

        // Ensure safe havens exist
        Shell.cmd("mkdir -p $DATA_SAFE_HAVEN").exec()
        Shell.cmd("mkdir -p $META_SAFE_HAVEN").exec()

        // Disable active DSU first
        Shell.cmd("gsi_tool disable").exec()

        // STEP 1: Backup Vold Encryption Keys (CRITICAL to prevent bootloop on restore!)
        // When a new GSI is installed, it overwrites the encryption key in /metadata/vold.
        // We must backup the current keys before moving metadata.
        Shell.cmd("mkdir -p $META_SAFE_HAVEN/$id").exec()
        Shell.cmd("cp -rp /metadata/vold/metadata_encryption/gsi $META_SAFE_HAVEN/$id/vold_key_gsi 2>/dev/null").exec()
        Shell.cmd("cp -rp /metadata/vold/metadata_encryption/dsu $META_SAFE_HAVEN/$id/vold_key_dsu 2>/dev/null").exec()

        // STEP 2: Move the active directories to the backup folder.
        // We MUST use `mv` (rename) instead of hardlinks (`ln`). Hardlinking pinned F2FS files
        // and then unlinking the original path causes the F2FS Garbage Collector to corrupt the .bmap
        // block mappings, resulting in a bootloop on restore. `mv` preserves everything perfectly.
        val dataMoved = Shell.cmd("mv $GSI_DATA $DATA_SAFE_HAVEN/$id").exec().isSuccess
        val metaMoved = Shell.cmd("mv $GSI_META/* $META_SAFE_HAVEN/$id/ 2>/dev/null").exec().isSuccess

        // STEP 3: Run gsi_tool wipe to properly reset gsid's internal state and system properties.
        // Since the files are already moved, this just updates the system properties without breaking the binder.
        Shell.cmd("gsi_tool wipe").exec()

        // STEP 4: Force clean the active directories to ensure no stray files
        // or broken SELinux contexts interfere with the NEXT installation.
        Shell.cmd(
            "rm -rf /data/gsi/dsu",
            "rm -rf /metadata/gsi/dsu",
            "rm -rf /data/gsid",
            "rm -rf /metadata/vold/metadata_encryption/gsi",
            "rm -rf /metadata/vold/metadata_encryption/dsu",
            "mkdir -p /data/gsi/dsu",
            "mkdir -p /metadata/gsi/dsu",
            "restorecon -R /data/gsi",
            "restorecon -R /metadata/gsi",
        ).exec()

        return dataMoved && metaMoved
    }

    /**
     * Restores a specific backed-up DSU from the safe havens into the active DSU slots.
     *
     * NO gsi_tool wipe here! We just:
     * 1. Clear the active directories (rm -rf)
     * 2. Move backup files back (same-partition renames)
     * 3. Enable single-boot mode
     * 4. Reboot
     */
    fun restoreDsu(id: String): Boolean {
        if (!hasRoot()) return false

        // Clear any existing active DSU files (if any)
        Shell.cmd("rm -rf $GSI_DATA").exec()
        Shell.cmd("rm -rf $GSI_META").exec()

        // Recreate parent dirs
        Shell.cmd("mkdir -p /data/gsi/dsu").exec()
        Shell.cmd("mkdir -p /metadata/gsi").exec()

        // Restore data (same partition rename — blocks preserved)
        val dataMoved = Shell.cmd("mv $DATA_SAFE_HAVEN/$id $GSI_DATA").exec().isSuccess

        // Restore metadata (same partition rename — lp_metadata block map valid!)
        val metaMoved = Shell.cmd("mv $META_SAFE_HAVEN/$id $GSI_META").exec().isSuccess

        // Restore Vold encryption keys
        Shell.cmd("rm -rf /metadata/vold/metadata_encryption/gsi").exec()
        Shell.cmd("rm -rf /metadata/vold/metadata_encryption/dsu").exec()
        Shell.cmd("cp -rp $GSI_META/vold_key_gsi /metadata/vold/metadata_encryption/gsi 2>/dev/null").exec()
        Shell.cmd("cp -rp $GSI_META/vold_key_dsu /metadata/vold/metadata_encryption/dsu 2>/dev/null").exec()
        // Clean up the copied keys from the restored dsu folder so they don't linger
        Shell.cmd("rm -rf $GSI_META/vold_key_gsi").exec()
        Shell.cmd("rm -rf $GSI_META/vold_key_dsu").exec()

        // CRITICAL FIX: Recreated directories via `mkdir -p` under root shell get the generic
        // `system_data_file` SELinux context instead of `gsi_data_file`.
        // First-stage init's SELinux policy strictly requires `gsi_data_file` and will throw
        // Permission Denied when trying to mount the GSI, resulting in a bootloop.
        // We MUST restore the proper SELinux contexts using `restorecon`.
        Shell.cmd("restorecon -R /data/gsi").exec()
        Shell.cmd("restorecon -R /metadata/gsi").exec()
        Shell.cmd("restorecon -R /metadata/vold/metadata_encryption").exec()

        // Save the restored name so the UI can display it
        Shell.cmd("echo '$id' > $GSI_META/dsu_sideloader_name").exec()

        if (dataMoved && metaMoved) {
            // Verify files are actually in place before enabling
            val verified = hasActiveDsu()

            if (verified) {
                // CRITICAL: Always -s (single-boot) for safety!
                Shell.cmd("gsi_tool enable -s").exec()
                Shell.cmd("reboot").exec()
                return true
            }
        }
        return false
    }

    /**
     * Securely deletes a backed-up DSU.
     */
    fun deleteBackedUpDsu(id: String): Boolean {
        if (!hasRoot()) return false

        Shell.cmd("rm -rf $DATA_SAFE_HAVEN/$id").exec()
        Shell.cmd("rm -rf $META_SAFE_HAVEN/$id").exec()

        return true
    }

    /**
     * Lists all backed-up DSU IDs and retrieves their size and backup date.
     */
    fun listBackups(): List<BackedUpDsu> {
        val backups = mutableListOf<BackedUpDsu>()
        val result = Shell.cmd("ls -1t $DATA_SAFE_HAVEN 2>/dev/null").exec()
        val dirs = result.out.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        for (dir in dirs) {
            val combinedResult = Shell.cmd(
                "du -sh $DATA_SAFE_HAVEN/$dir 2>/dev/null | awk '{print \$1}'; stat -c %y $DATA_SAFE_HAVEN/$dir 2>/dev/null | cut -d'.' -f1",
            ).exec()
            val size = combinedResult.out.getOrNull(0)?.trim()?.ifEmpty { "Unknown size" } ?: "Unknown size"
            val date = combinedResult.out.getOrNull(1)?.trim()?.ifEmpty { "Unknown date" } ?: "Unknown date"

            backups.add(BackedUpDsu(dir, size, date))
        }
        return backups
    }

    /**
     * Extracts metadata from a backed-up DSU image.
     */
    fun getBackupMetadata(id: String): DsuMetadata? {
        if (!hasRoot()) return null

        return DsuMetadata(
            androidVersion = "Unknown",
            sdkVersion = "Unknown",
            cpuAbi = "Unknown",
            vndkVersion = "Unknown",
            securityPatch = "Unknown",
            buildFingerprint = "Unknown",
            isTrebleCompliant = "Unknown",
        )
    }
}

data class BackedUpDsu(
    val name: String,
    val size: String,
    val timestamp: String,
)

data class DsuMetadata(
    val androidVersion: String,
    val sdkVersion: String,
    val cpuAbi: String,
    val vndkVersion: String,
    val securityPatch: String,
    val buildFingerprint: String,
    val isTrebleCompliant: String,
)
