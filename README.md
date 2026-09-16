# Kutu Android TV Guide & Tools 🚀

[![Android TV](https://img.shields.io/badge/Android%20TV-9%20--%2014-green.svg)](https://android.com/tv)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![ADB Ready](https://img.shields.io/badge/ADB-Safe%20%26%20Reversible-orange.svg)](#non-negotiable-safety-rules)

A universal, reversible, and AI-compatible ADB workflow for optimizing Android TV / Google TV devices, adding low-latency AirPlay screen mirroring, and deploying a lightweight custom TV launcher.

---

## 📖 Origin & References

This project is built upon the foundational research and step-by-step methodologies from:
* 🇹🇷 **Orijinal Türkçe Rehber:** [Berksim Substack - Mi Box Android TV Rehberi](https://berksim.substack.com/p/mi-box-android-tv-rehberi)
* 🇬🇧 **Original English Guide:** [Berksim Substack - Mi Box Android TV Optimization Guide](https://berksim.substack.com/p/mi-box-android-tv-guide-optimise)
* 📡 **AirPlay Core Engine:** Built on [jqssun/android-airplay-server](https://github.com/jqssun/android-airplay-server) & [UxPlay](https://github.com/antimof/UxPlay).

Tested across diverse hardware platforms:
- **Android TV 9 (Pie):** Xiaomi Mi Box 3 / S (Amlogic S905X)
- **Android 14 (Google TV):** Skyworth / Next HPR314 (Realtek RTD1319D SoC)
- Compatible with most Android TV & Google TV boxes, sticks, and smart TVs.

---

## 🧰 What's Included

```
.
├── ANDROID-TV-OPTIMIZATION-GUIDE.md   # Universal master execution guide (for AI Agents & Developers)
├── kutu-home/                         # Full source code for lightweight Android TV Launcher
├── kutu-mirror/                       # AirPlay screen mirroring receiver (Android TV optimized submodule)
├── RESTORE-ALL.sh                     # 100% reversible one-click restore script
├── DEBLOAT-LOG.md                     # Example package modification and audit log
└── kutu-home-background.png           # Default 1080p high-contrast background wallpaper
```

> 📦 **Precompiled APKs:** Download the latest signed binaries from the [**GitHub Releases**](https://github.com/burakberkkeskin/kutu-android-tv-guide/releases/latest) page.

---

## ✨ Features

### 1. 📺 Kutu Home (`local.kutu.home`)
* **Ultra-Lightweight:** Only **~50 MB RAM** footprint (compared to 180–250 MB on stock Google TV LauncherX).
* **Apple TV Style Shelf:** Responsive bottom dock with glowing focus indicators and app labels.
* **Smart App Management:** Add, remove, and reorder dock apps directly with long-press (`DPAD_CENTER`).
* **Live Package Listener:** Automatically updates whenever an app is installed or uninstalled in real time.
* **Instant HOME Redirection:** 0 ms keycode intercept via Leanback Accessibility Service without breaking Google TV Quick Settings.

### 2. 🪞 Kutu Mirror (`local.kutu.mirror`)
* **Hardware-Accelerated AirPlay:** Native H.264 / 1080p 60fps low-latency AirPlay receiver.
* **Zero Overhead Daemon:** Consumes only **~9 MB RAM** in background standby.
* **Native TV UI:** Dedicated Leanback launcher banner and settings interface.

### 3. 🛡️ Safe & Reversible Debloat Workflow
* **No Root Required:** 100% userland ADB operations (`pm disable-user --user 0`).
* **Non-Destructive:** No `/system` alterations, no fastboot, no risking bricking or bootloops.
* **One-Click Rollback:** Instant restoration of all disabled packages and default launcher via `RESTORE-ALL.sh`.

---

## ⚡ Quick Start

### 1. Connect over ADB
Enable Developer Options and USB/Network Debugging on your Android TV device:
```bash
adb connect <TV_IP_ADDRESS>:5555
adb devices
```

### 2. Instant Install (From GitHub Releases)
Download `kutu-home.apk` and `kutu-mirror.apk` from [Latest Releases](https://github.com/burakberkkeskin/kutu-android-tv-guide/releases/latest) and install via ADB:
```bash
# Install Kutu Home Launcher
adb install -r kutu-home.apk

# Install Kutu Mirror (AirPlay Receiver)
adb install -r kutu-mirror.apk
```

### 3. Enable HOME Redirection & Background Mirroring
```bash
# Enable Accessibility Service for instant HOME key routing
adb shell settings put secure enabled_accessibility_services local.kutu.home/local.kutu.home.service.KutuHomeAccessibilityService

# Start Kutu Mirror daemon in the background
adb shell am start -n local.kutu.mirror/io.github.jqssun.airplay.ReceiverControlActivity
```

---

## 🤖 Using with AI Assistants (Antigravity, Claude, ChatGPT, Cursor, etc.)

Feed `ANDROID-TV-OPTIMIZATION-GUIDE.md` directly to your AI assistant after establishing an ADB connection. The guide instructs the agent to:
1. Conduct a **complete read-only baseline audit** of your specific hardware and active packages.
2. Ask for your usage preferences (e.g. streaming apps, voice assistant, screensaver).
3. Safely debloat vendor bloatware while logging every action.
4. Set up and verify `kutu-home` and `kutu-mirror`.
5. Generate a device-specific `RESTORE-ALL.sh` rollback script.

---

## 🔒 Non-Negotiable Safety Rules

1. **Always establish a baseline** before modifying any package or service.
2. **Never modify `/system`** or use rooting tools.
3. **Never uninstall system packages**—use `pm disable-user --user 0` so changes remain 100% reversible.
4. **Preserve critical services:** Wi-Fi, Bluetooth remote pairing, Widevine DRM, Audio engine (Dolby), Google Play Services, Chromecast, and Assistant.
5. **Always verify rollback:** Keep `RESTORE-ALL.sh` ready at all times.

---

## 🔄 Reversing All Changes

To completely restore all disabled vendor packages and revert to the default stock launcher:
```bash
chmod +x RESTORE-ALL.sh
./RESTORE-ALL.sh <TV_IP_ADDRESS>:5555
```

---

## 📄 License & Attribution

* **Launcher & Automation Scripts:** Released under the [MIT License](LICENSE).
* **AirPlay Server Core:** Licensed under GPLv3 (derived from [UxPlay](https://github.com/antimof/UxPlay) and [android-airplay-server](https://github.com/jqssun/android-airplay-server)).
* **Documentation & Methodologies:** Inspired by [Berksim](https://berksim.substack.com).
