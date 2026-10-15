package vegabobo.dsusideloader.core.gsirepo

import java.text.DecimalFormat
import java.util.Locale

object GsiClassifier {

    private val supportedExtensions = listOf(".xz", ".gz", ".img", ".zip", ".gzip", ".zst")

    fun isGsiAsset(filename: String): Boolean {
        val lower = filename.lowercase(Locale.ROOT)
        // Check extension
        val hasExt = supportedExtensions.any { lower.endsWith(it) }
        if (!hasExt) return false

        // Filter out sha256, md5, txt, etc.
        if (lower.endsWith(".sha256") || lower.endsWith(".md5") || lower.endsWith(".txt")) {
            return false
        }
        return true
    }

    fun extractRomName(repoName: String, title: String): String {
        val combined = "$repoName $title".lowercase(Locale.ROOT)
        return when {
            combined.contains("crdroid") || combined.contains("cr-droid") -> "crDroid"
            combined.contains("rising") || combined.contains("risingos") -> "RisingOS"
            combined.contains("lineage") || combined.contains("los") -> "LineageOS"
            combined.contains("restless") -> "RestlessOS"
            combined.contains("pixel") || combined.contains("pe") -> "PixelOS"
            combined.contains("evolution") || combined.contains("evo") -> "EvolutionX"
            combined.contains("derpfest") -> "DerpFest"
            combined.contains("cherish") -> "CherishOS"
            combined.contains("corvus") -> "CorvusOS"
            combined.contains("havoc") -> "HavocOS"
            combined.contains("arrow") -> "ArrowOS"
            combined.contains("banana") -> "BananaDroid"
            combined.contains("spark") -> "SparkOS"
            combined.contains("matrixx") -> "Project Matrixx"
            combined.contains("elixir") -> "Project Elixir"
            combined.contains("alphadroid") -> "AlphaDroid"
            combined.contains("superior") -> "SuperiorOS"
            combined.contains("google") || combined.contains("aosp") -> "AOSP Official"
            else -> {
                val clean = repoName.substringAfterLast("/").replace("treble_", "").replace("device_", "").replace("_", " ")
                if (clean.isNotBlank()) clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } else "Generic GSI"
            }
        }
    }

    fun parseAndroidVersion(vararg texts: String): Pair<Int, String> {
        val combined = texts.joinToString(" ").lowercase(Locale.ROOT)

        // Android 16 (Baklava)
        if (combined.contains("android-16") || combined.contains("android 16") || combined.contains("a16") ||
            combined.contains("baklava") || combined.contains("v16.")
        ) {
            return Pair(16, "Android 16 (Baklava)")
        }

        // Android 15 (VanillaIceCream / AP2A / AP3A)
        if (combined.contains("android-15") || combined.contains("android 15") || combined.contains("a15") ||
            combined.contains("vanillaicecream") || combined.contains("ap2a") || combined.contains("ap3a") ||
            combined.contains("v15.") || combined.contains("15.0")
        ) {
            return Pair(15, "Android 15 (Vanilla Ice Cream)")
        }

        // Android 14 (UpsideDownCake / UP1A / UQ1A)
        if (combined.contains("android-14") || combined.contains("android 14") || combined.contains("a14") ||
            combined.contains("upsidedowncake") || combined.contains("up1a") || combined.contains("uq1a") ||
            combined.contains("v14.") || combined.contains("14.0")
        ) {
            return Pair(14, "Android 14 (Upside Down Cake)")
        }

        // Android 13 (Tiramisu / TQ1A / TQ2A / TQ3A)
        if (combined.contains("android-13") || combined.contains("android 13") || combined.contains("a13") ||
            combined.contains("tiramisu") || combined.contains("tq") || combined.contains("td1a") ||
            combined.contains("v13.") || combined.contains("13.0")
        ) {
            return Pair(13, "Android 13 (Tiramisu)")
        }

        // Android 12 / 12L (Snow Cone / Sv2)
        if (combined.contains("android-12") || combined.contains("android 12") || combined.contains("a12") ||
            combined.contains("snowcone") || combined.contains("sp1a") || combined.contains("sq1a") ||
            combined.contains("12.0") || combined.contains("12.1")
        ) {
            return Pair(12, "Android 12 / 12L (Snow Cone)")
        }

        // Android 11 (Red Velvet Cake)
        if (combined.contains("android-11") || combined.contains("android 11") || combined.contains("a11") ||
            combined.contains("redvelvetcake") || combined.contains("rp1a") || combined.contains("rq") ||
            combined.contains("11.0")
        ) {
            return Pair(11, "Android 11 (Red Velvet Cake)")
        }

        // Fallback generic
        return Pair(0, "Generic AOSP / Treble")
    }

    fun parseArchitecture(filename: String): String {
        val lower = filename.lowercase(Locale.ROOT)
        return when {
            lower.contains("arm64") || lower.contains("aarch64") -> "ARM64"
            lower.contains("a64") || lower.contains("arm32_binder64") -> "A64 (arm32_b64)"
            lower.contains("x86_64") || lower.contains("x86-64") -> "x86_64"
            lower.contains("arm") -> "ARM32"
            lower.contains("x86") -> "x86"
            else -> "ARM64"
        }
    }

    fun parseVariant(filename: String): String {
        val lower = filename.lowercase(Locale.ROOT)
        val variants = mutableListOf<String>()

        // GApps vs Vanilla vs MicroG
        when {
            lower.contains("gapps") || lower.contains("gms") || lower.contains("-bg") || lower.contains("_bg") -> variants.add("GApps")
            lower.contains("microg") || lower.contains("-bm") || lower.contains("_bm") -> variants.add("MicroG")
            lower.contains("vanilla") || lower.contains("-bv") || lower.contains("_bv") || lower.contains("nosu") -> variants.add("Vanilla")
            lower.contains("floss") -> variants.add("FLOSS")
        }

        if (lower.contains("vndklite") || lower.contains("vndk-lite") || lower.contains("vndk_lite")) {
            variants.add("VNDK-Lite")
        }

        if (lower.contains("secure") || lower.contains("sec")) {
            variants.add("Secure")
        }

        if (variants.isEmpty()) {
            return "Vanilla"
        }
        return variants.joinToString(" • ")
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return ""
        val df = DecimalFormat("#.##")
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "${df.format(gb)} GB"
            mb >= 1.0 -> "${df.format(mb)} MB"
            else -> "${df.format(kb)} KB"
        }
    }
}
