### DSU Multi-Sideloader v2.10 (9)

- **Multi-DSU Manager**: Complete multi-slot DSU management subsystem
  - Slot inspection and live DSU partition mapping
  - Seamless backup, restore, slot switching, and discard capabilities
  - Root auto-stashing: Safely stash active DSU block maps & metadata to bypass Android single-DSU limitation
  - Built-in root diagnostics & Treble metadata inspector
- **Modern UI Redesign & Frosted Glass**:
  - Liquid glassmorphism (`GlassSurface` and `GlassButton`)
  - Dynamic window and live wallpaper blurs with backdrop rendering
  - 5 theme modes: Follow System, Light, Dark, AMOLED Pitch Black, and Translucent Blur
  - Fluid 120Hz-tuned pager navigation and centered dialog sheets
- **Colorful Icon System**:
  - 48 custom dual-mode squircle vector icons across all cards, screens, and dialogs
  - Vibrant day and night tinting
- **Revamped Installation Experience**:
  - Circular progress indicator with pulse animation and stage counters
  - Real-time log inspection sheet and partition progress visualization
- **Quick Settings Tiles**:
  - Dedicated QS tiles to reboot directly into installed DSU or discard active DSU instances
- **Magisk Module**:
  - Bundled patched `gsid` binaries for Android 10-13 (API 29-33) with full SELinux policies

Read more at: https://github.com/R15Hi/Dsu-Multi-sideloader/releases/tag/v2.10