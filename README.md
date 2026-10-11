# DSU Multi-Sideloader

A powerful fork of the original DSU Sideloader that brings multi-DSU management and a completely revamped user experience to seamlessly install and manage Generic System Images (GSIs) via Android's DSU feature.

<div align="center">
  <img src="https://github.com/user-attachments/assets/a42e9488-c533-4355-9ec1-f7ab79975de6" alt="Preview 1" width="24%"/>
  <img src="https://github.com/user-attachments/assets/975eda24-3c82-4ee3-ac0f-3b71bd56efc8" alt="Preview 2" width="24%"/>
  <img src="https://github.com/user-attachments/assets/79d580e8-4faf-4195-adcc-eec9d0b4147a" alt="Preview 3" width="24%"/>
  <img src="https://github.com/user-attachments/assets/2d06df59-05a4-4996-9676-225aa4e69a0a" alt="Preview 4" width="24%"/>
  <img src="https://github.com/user-attachments/assets/44ddbc46-9171-422a-9ca1-d33bd25b03fb" alt="Preview 5" width="24%"/>
  <img src="https://github.com/user-attachments/assets/f71140d6-1bcc-4824-8639-be0b4be2ebbd" alt="Preview 6" width="24%"/>
  <img src="https://github.com/user-attachments/assets/b1c0d77c-6b9a-4b83-a665-d657146b7766" alt="Preview 7" width="24%"/>
</div>

## ✨ What's New in This Version

This version introduces significant core architecture and UI/UX improvements over the original app:

### Multi-DSU Manager
The app now features a **Multi-DSU Manager** tab, allowing you to manage multiple Dynamic System Updates seamlessly:
- **View Installations**: Easily refresh and view all currently configured DSUs on your device.
- **Restore & Boot**: Restore previous backups and initiate the boot process into your chosen DSU directly from the app.
- **Auto-stashing**: Android natively only allows a single DSU. This app overcomes this (Requires Root) by safely stashing the active DSU's block maps and metadata to a backup folder. This tricks Android into thinking the DSU slot is empty, allowing you to install a brand new GSI. If you reboot back into your original host system, the app automatically stashes your restored DSU in the background to keep the slot free!

### UI/UX & Core Architecture Improvements
- **Core Architecture Overhaul**: Modernized app architecture with a redesigned Settings UI and improved Magisk module integration.
- **Centered Dialogs & Blurs**: All settings and theme selection dialogs now pop up in the center of the screen with a modern, blurred, and dimmed background.
- **Dynamic Header Text**: The top header text seamlessly centers itself when scrolling for an elegant scrolling experience.
- **Clean Layout**: Removed the extra icon from the header completely to provide a cleaner layout without visual duplication.
- **Colorful Icons**: Replaced plain icons with colorful, vibrant vector icons in the home screen cards.
- **Redesigned App Icon**: Refreshed the launcher icon by increasing the width and proportions of the phone element.
- **Refresh State Indicator**: Added a centered visual loading indicator to the Multi-DSU tab so you always know when your installations are being scanned or refreshed.

## 📋 Requirements
- Android 10 or higher
- Unlocked Bootloader
- Device with Dynamic Partitions
- A GSI you want to use!

**Remember to use GSIs compatible with your architecture and VNDK implementation.**

## 📥 Downloads

You can download the latest APK from the [Releases Section](https://github.com/R15Hi/Dsu-Multi-sideloader/releases/latest) of this repository.

## 🚀 How to use?
1. Install the app.
2. When opening for the first time, give read/write permission to a folder, create a new folder and allow access. *(This folder will be used to store temporary files, like extracted GSIs)*.
3. Select a GSI to install (accepted formats: gz, xz, img, and zip DSU packages).
4. Customize your installation (e.g., changing userdata size for dynamic systems).
5. Tap on "Install".
6. Wait until it finishes (it may take some time).
7. Once finished:
	- If the built-in installer is enabled, no extra step is required.
	- On rooted/system/shizuku mode (with built-in installer disabled), the DSU screen will appear prompting you to confirm installation. Check your notifications to start it.
	- On ADB mode, run the prompted adb command to start the installation.
8. Once dynamic system is ready, you can boot it through notifications, or directly from the app.

## ⚙️ Operation modes

DSU Sideloader supports multiple operation modes. The best available mode is picked automatically:

1. **ADB**: Default mode. Requires adb command to invoke DSU system-app.
2. **Shizuku**: No adb required. Supports tracking installation progress and diagnostics.
3. **Root**: Supports all Shizuku features without special permissions. Allows direct DynamicSystem API usage and built-in DSU installer.
4. **System mode**: Runs as a system-app (via Magisk module). Fixes common SELinux denials and includes custom gsid binary.
5. **System/Root mode**: Combines all features of root and system operation modes.

### Recommendations
- Non-rooted: **Shizuku** is a highly recommended operation mode.
- Rooted: **Root** operation mode is more than enough for most people.
- Having DSU issues?: Go with **System/Root**.
- Use with Stock ROMs for the best compatibility.

## ❓ Common questions

1. **DSU installation finishes with no errors, but device doesn't boot into installed DSU?**
   - AVB might be preventing the boot. Try flashing disabled vbmeta.
2. **Why can't I set a high userdata value?**
   - Android limits allocation (max 40%). The custom gsid binary reduces this limitation to 20%.
3. **Why does the "Unmount SD" option exist?**
   - Allocating in SD cards isn't always supported and can cause errors. This enforces allocation in device storage.
4. **Why does the built-in installer require root?**
   - It uses Android's internal DynamicSystem API which is heavily restricted without root.

## 💖 Credits & Other

- Based on the original [DSU-Sideloader](https://github.com/VegaBobo/DSU-Sideloader) by [VegaBobo](https://github.com/VegaBobo).
- For translators, we now have a Crowdin, feel free to send your translations: [https://crowdin.com/translate/dsu-sideloader/](https://crowdin.com/translate/dsu-sideloader/).
- App icon made by [WSTxda](https://github.com/WSTxda).
