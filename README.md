<div align="center">

  <img src="https://github.com/tapframe/NuvioTV/blob/main/assets/brand/app_logo_wordmark.png" alt="Nuvio Enhanced iOS" width="340" />

  <h1>Nuvio Enhanced for iOS & iPadOS</h1>

  <p><strong>Unofficial iOS fork of Nuvio Enhanced with automated CI/CD IPA compilation and SideStore / AltStore community sources.</strong></p>

  <p>
    <a href="https://github.com"><img src="https://img.shields.io/badge/Platform-iOS%20%7C%20iPadOS-blue?style=for-the-badge&logo=apple" alt="iOS & iPadOS" /></a>
    <a href="https://github.com"><img src="https://img.shields.io/badge/SideStore-Compatible-7B2FF7?style=for-the-badge" alt="SideStore Compatible" /></a>
    <a href="https://github.com"><img src="https://img.shields.io/badge/AltStore-Compatible-2496ED?style=for-the-badge" alt="AltStore Compatible" /></a>
    <a href="https://github.com"><img src="https://img.shields.io/badge/TrollStore-Ready-orange?style=for-the-badge" alt="TrollStore Ready" /></a>
  </p>

  <p>
    <a href="#-quick-install-sidestore--altstore">SideStore Source</a> •
    <a href="#-features">Features</a> •
    <a href="#-automated-github-actions-builds">Automated Builds</a> •
    <a href="#-sideloading-instructions">Installation</a> •
    <a href="IOS_SETUP.md">Detailed Setup Guide</a>
  </p>

</div>

---

## ⚡ Quick Install: SideStore & AltStore

Add this repository as a community source to your sideload manager for automatic updates:

```text
https://raw.githubusercontent.com/illuminati945/NuvioMobile-iOS/main/NuvioEnhanced.json
```

| Source Variant | Description | Community Source URL |
|---|---|---|
| **Nuvio Enhanced** *(Recommended)* | Full features, Live TV, custom player options & tweaks | `.../NuvioEnhanced.json` |
| **Nuvio Full** | Upstream build variant | `.../NuvioFull.json` |

---

## ✨ Features

- 📱 **Full iOS & iPadOS Support**: Designed specifically for iPhone and iPad with native gestures and hardware acceleration.
- ⚡ **Background Downloads & Notifications**: Continuous downloads in the background with extended execution time, Dynamic Island / Live Activities, and native local push notifications upon completion or failure.
- 📂 **Native Files App Integration**: Full `UIFileSharing` and document support—browse, play, or export downloaded media and subtitles directly in the iOS **Files** app (`On My iPhone > Nuvio Enhanced`).
- 📺 **Live TV & EPG**: Functional M3U playlist integration, XMLTV guide, channel switcher, and favorites.
- 🎬 **Enhanced Video Player**: Tap-to-seek, aspect ratio toggling, audio & subtitle track preservation across quality changes.
- 💬 **Advanced Subtitle Styling**: Custom fonts, opacity adjustment, language groups, and SDH subtitle stripping.
- 🤖 **AI Content Assistant**: Grounded content recommendations and smart synopsis.
- 📅 **Release Radar & Library Calendar**: Built-in upcoming release calendar and series tracking.
- 🔄 **Cloud & Tracking Sync**: Seamless Trakt and Simkl synchronization with QR/device-code authentication.
- 🚀 **Zero-Config Cloud CI**: Automated compilation via GitHub Actions without needing a Mac or local Xcode.

---

## 🛠️ Automated GitHub Actions Builds

This repository includes fully automated GitHub Actions workflows:

1. **`Build iOS IPA` (`.github/workflows/build-ipa.yml`)**:
   - Compiles `.ipa` on Apple macOS runners on demand.
   - Run manually from the **Actions** tab with choice of `Enhanced` or `Full` variant.
   - Outputs ready-to-sideload `.ipa` artifact with SHA-256 hash.

2. **`Create iOS Release` (`.github/workflows/ios-release.yml`)**:
   - Automatically builds IPAs and publishes GitHub Releases.

3. **`Update AltStore & SideStore Sources` (`.github/workflows/update-altstore.yml`)**:
   - Automatically parses new releases, extracts IPA metadata, and updates `NuvioEnhanced.json` / `NuvioFull.json` so SideStore users get instant updates.

---

## 📥 Sideloading Instructions

### 1. SideStore (Recommended)
1. Open **SideStore** on your iOS device.
2. Go to **Sources** tab -> tap **+** in top right.
3. Paste: `https://raw.githubusercontent.com/illuminati945/NuvioMobile-iOS/main/NuvioEnhanced.json`
4. Tap **Add** and install **Nuvio Enhanced**.

### 2. AltStore
1. In **AltStore**, tap the **+** icon in the My Apps / Sources tab.
2. Enter the source URL: `https://raw.githubusercontent.com/illuminati945/NuvioMobile-iOS/main/NuvioEnhanced.json`

### 3. TrollStore (iOS 14.0 – 17.0)
1. Download `Nuvio-v<version>-Enhanced.ipa` from the repository Releases.
2. Open with TrollStore for permanent installation.

### 4. Sideloadly / Scarlet / Esign / Feather
1. Download the unsigned `.ipa` from the GitHub Actions Artifacts or Releases page.
2. Sideload using your preferred desktop or on-device tool.

---

## 📖 Building Locally

Requirements:
- macOS with Xcode 15 or 16 installed
- JDK 17 (`brew install openjdk@17`)

```bash
# Clone with submodules
git clone --recurse-submodules https://github.com/illuminati945/NuvioMobile-iOS.git
cd YOUR_REPO

# Fetch Nuvio Engine
mkdir -p ../nuvio-engine/platform/apple
curl -L "https://github.com/NuvioMedia/nuvio-engine/releases/download/v0.1.1/nuvio-engine-apple-0.1.1.zip" -o /tmp/engine.zip
ditto -x -k /tmp/engine.zip /tmp/engine
ditto /tmp/engine/NuvioEngine.xcframework ../nuvio-engine/platform/apple/NuvioEngine.xcframework

# Build Xcode project
cd iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Release -destination 'generic/platform=iOS' build
```

---

## 🤝 Attribution & Credits

- Upstream NuvioMobile by [NuvioMedia](https://github.com/NuvioMedia) & [tapframe](https://github.com/tapframe)
- Enhanced features based on [AKRusso](https://github.com/AKRusso/NuvioMobile-Enhanced)
- iOS build & SideStore configuration inspiration from [luqmanfadlli](https://github.com/luqmanfadlli/NuvioMobile-iOS)
- [MPVKit](https://github.com/mpvkit/MPVKit) & [libmpv](https://mpv.io)
