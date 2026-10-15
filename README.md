# DSU Multi-Sideloader

A powerful, modernized fork of the original DSU Sideloader that brings **Multi-DSU slot management**, an integrated **GSI Hub** community catalog, and a **Liquid Glass** aesthetic overhaul to seamlessly install, manage, and switch between Generic System Images (GSIs) via Android's Dynamic System Updates (DSU) feature.

<div align="center">
  <img src="https://github.com/user-attachments/assets/a42e9488-c533-4355-9ec1-f7ab79975de6" alt="Preview 1" width="24%"/>
  <img src="https://github.com/user-attachments/assets/975eda24-3c82-4ee3-ac0f-3b71bd56efc8" alt="Preview 2" width="24%"/>
  <img src="https://github.com/user-attachments/assets/79d580e8-4faf-4195-adcc-eec9d0b4147a" alt="Preview 3" width="24%"/>
  <img src="https://github.com/user-attachments/assets/2d06df59-05a4-4996-9676-225aa4e69a0a" alt="Preview 4" width="24%"/>
  <img src="https://github.com/user-attachments/assets/44ddbc46-9171-422a-9ca1-d33bd25b03fb" alt="Preview 5" width="24%"/>
  <img src="https://github.com/user-attachments/assets/f71140d6-1bcc-4824-8639-be0b4be2ebbd" alt="Preview 6" width="24%"/>
  <img src="https://github.com/user-attachments/assets/b1c0d77c-6b9a-4b83-a665-d657146b7766" alt="Preview 7" width="24%"/>
</div>

---

## ✨ Features

### 🔄 Multi-DSU Slot Manager
Android natively limits dynamic systems to a single DSU slot. DSU Multi-Sideloader bypasses this restriction:
- **Slot Inspection**: Easily scan, inspect, and track all configured DSU slots and partitions on your device.
- **Root Auto-Stashing**: Safely stashes the active DSU's block maps and metadata to a backup folder, freeing the DSU slot so you can install and test multiple GSIs simultaneously.
- **Backup & Instant Switching**: Restore previous backups and boot into any installed DSU slot directly from the app.

### 🌐 GSI Hub
An integrated catalog for discovering and installing Generic System Images directly from community maintainers:
- **Live Maintainer Feeds**: Direct integration with active GSI releases from popular maintainers (AndyYan, ponces, phhusson, MisterZTR, Nazim, and more).
- **ROM Recognition**: Automatic detection and brand logos for major ROM families (AOSP, Pixel Experience, LineageOS, crDroid, Evolution X, etc.).
- **Changelog Inspection**: Built-in release notes and changelog viewer with smooth collapsible previews.
- **Atomic One-Tap Installation**: Direct download and installation workflow with automatic cancellation cleanup to ensure partially downloaded files never enter installation.
- **Custom Repositories**: Flexibility to add your own favorite GSI release repositories or remove them with a long-press.

### 💎 Liquid Glass & Modern UI Design
A completely refreshed interface combining visual excellence with fluid navigation:
- **Liquid Glass Navigation Bar**: Translucent floating capsule with multi-pass wallpaper backdrop blur, dynamic contrast curve, 25% tint wash, and specular rim reflection.
- **Multiple Theme Styles**: Choose between Dynamic (Material You), Dark, Light, AMOLED Pitch Black, and Glassy mode.
- **Centered Dialogs & Blurs**: Fluid dialog sheets with frosted backdrop blurs and centered layout hierarchy.
- **Dual-Mode Vector Icons**: Vibrant custom squircle vector iconography designed for both dark and light modes.

---

## 📱 Navigation Tabs

The app is organized into four dedicated tabs:
1. **Installed**: Manage local GSI installations, customize partition userdata sizes, and track installation progress.
2. **Multi DSU**: Inspect slots, stash active dynamic systems, and switch between installed DSU slots.
3. **GSI Hub**: Discover, inspect changelogs, download, and install community GSIs directly.
4. **Settings**: Theme selection, operation mode configuration, and developer diagnostics.

---

## 📋 Requirements
- **Android 10 or higher**
- **Unlocked Bootloader**
- **Device with Dynamic Partitions**
- **Compatible GSI**: Ensure the GSI architecture matches your device (`arm64`, `arm`, `x86_64`) and VNDK requirements (`ab` / system-as-root).

---

## 📥 Downloads

Get the latest installable packages from the [Releases Section](https://github.com/R15Hi/Dsu-Multi-sideloader/releases/latest):
- **`app-release.apk`**: Installable Android application package.
- **`module_DSU_Sideloader.zip`**: Magisk module for system mode & patched `gsid` binaries.

---

## 🚀 How to use?

1. Install and launch the app.
2. Grant read/write storage access to a working directory for temporary files (extracted images).
3. Select a GSI:
   - **From GSI Hub**: Browse maintainer releases, check the changelog, and tap **Download & Install**.
   - **From Local Storage**: Select an image file (`.gz`, `.xz`, `.img`, or `.zip` DSU package) on the **Installed** tab.
4. Customize your installation settings (such as userdata partition allocation).
5. Tap **Install** and wait for processing to finish.
6. Boot into your dynamic system through the system notification or directly from the **Multi-DSU** tab!

---

## ⚙️ Operation Modes

DSU Sideloader automatically chooses the best available operation mode:

| Mode | Root Required? | Description |
| :--- | :---: | :--- |
| **ADB** | ❌ No | Default fallback mode. Invokes DSU via standard ADB commands. |
| **Shizuku** | ❌ No | No PC required. Communicates directly with system services with installation diagnostics. |
| **Root** | ✔️ Yes | Direct DynamicSystem API access, multi-slot auto-stashing, and built-in installer. |
| **System Mode** | ✔️ Yes (Magisk) | Runs as a system privileged app, bypassing SELinux denials with custom `gsid`. |
| **System/Root** | ✔️ Yes | Combines all root capabilities with patched system binaries for maximum compatibility. |

---

## ❓ Frequently Asked Questions

1. **DSU finishes installing but device doesn't boot into it?**
   - Android Verified Boot (AVB) may be blocking custom GSIs. Flashing a disabled `vbmeta` image (`--disable-verity --disable-verification`) usually resolves this.
2. **Why is the userdata allocation limited?**
   - Stock Android caps DSU allocation to 40% of free storage. The bundled patched `gsid` module reduces this restriction to 20%.
3. **Can I keep my host system data safe?**
   - Yes! DSUs run completely isolated in dynamic partitions on your storage. Discarding or unstashing a DSU leaves your host system untouched.
4. **How do I remove a custom repository from GSI Hub?**
   - Tap and hold (long-press) on any maintainer's card in the GSI Hub tab to delete that repository feed.

---

## 💖 Credits & Acknowledgments

- Based on the original [DSU-Sideloader](https://github.com/VegaBobo/DSU-Sideloader) by [VegaBobo](https://github.com/VegaBobo).
- Multi-DSU subsystem, GSI Hub, and Liquid Glass UI overhaul by [R15Hi](https://github.com/R15Hi).
- Launcher icon designed by [WSTxda](https://github.com/WSTxda).
- Special thanks to all GSI maintainers (AndyYan, phhusson, ponces, MisterZTR, Nazim, and community creators) for keeping the Treble ecosystem vibrant.
- Translations hosted on Crowdin: [crowdin.com/translate/dsu-sideloader](https://crowdin.com/translate/dsu-sideloader/).
