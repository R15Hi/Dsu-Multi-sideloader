### DSU Multi-Sideloader v2.20 (10)

- **GSI Hub Subsystem**:
  - Dedicated "GSI Hub" tab in bottom navigation for browsing and installing active Generic System Images
  - Automatic discovery, indexing, and live fetching of active community GSI GitHub releases
  - Chronological release sorting (latest to oldest) with human-readable release date and timestamps
  - ROM brand logos & classifications (AOSP, Pixel Experience, LineageOS, crDroid, Evolution X, and more)
  - Interactive collapsible changelog inspection with smooth zero-jank accordion animation
  - One-tap "Download & Install" with cancellation safety (incomplete downloads are never loaded into installation)
  - Custom repository management with tap-and-hold deletion
  - Full-featured maintainer credits section honoring GSI developers

- **Liquid Glass Navigation Bar Overhaul**:
  - Reverse-engineered frosted glass rendering pipeline inspired by Liquid Glass architecture
  - Precision mathematical calibration:
    - Glass Blur: 55 radius multi-pass blur in `WallpaperHelper` (converging to true Gaussian blur)
    - Contrast Boost: 40 (Skia ColorMatrix dynamic contrast curve with 1.20x expansion)
    - Panel Colour Opacity: 25% (0.25f alpha) primary accent tint over smoked frosted acrylic backing
    - Shadow Strength: 12.dp elevation with 12% ambient shadow and 24% spot shadow
    - Hairline Specular Rim: 1.2.dp border with top highlight reflection sheen
  - Universal theme compatibility across Dynamic, Dark, Light, AMOLED, and Glassy modes

- **UI/UX Polishing**:
  - Renamed primary tab from "Installation" to "Installed" for clarity
  - Centered blur refresh animation matching Multi-DSU tab
  - Optimized button layouts, chips, and typography across all screen resolutions

Read more at: https://github.com/R15Hi/Dsu-Multi-sideloader/releases/tag/v2.20