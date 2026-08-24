<div align="center">

  <img src="https://github.com/tapframe/NuvioTV/blob/main/assets/brand/app_logo_wordmark.png" alt="Nuvio Enhanced" width="300" />

  # Nuvio Enhanced for iOS & iPadOS

  **The complete streaming, Live TV, and tracking experience for iOS.**

  <div style="display: flex; justify-content: center; align-items: center; gap: 12px; margin: 18px 0; flex-wrap: wrap;">
    <a href="https://altdirect.app/?url=https://raw.githubusercontent.com/illuminati945/NuvioMobile-iOS/main/NuvioEnhanced.json" target="_blank">
      <img src="https://altdirect.app/assets/png/AltSource_Purple.png" alt="Add AltSource" width="180" />
    </a>
    <a href="https://github.com/illuminati945/NuvioMobile-iOS/releases/latest" target="_blank">
      <img src="https://altdirect.app/assets/png/Download_Purple.png" alt="Download .ipa" width="180" />
    </a>
  </div>

  <p>
    <a href="https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=livecontainer"><img src="https://img.shields.io/badge/Open%20in-LiveContainer-30B0C7?style=flat-square&logo=apple&logoColor=white" alt="LiveContainer" /></a>
    <a href="https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=sidestore"><img src="https://img.shields.io/badge/Add%20to-SideStore-7B2FF7?style=flat-square&logo=apple&logoColor=white" alt="SideStore" /></a>
    <a href="https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=altstore"><img src="https://img.shields.io/badge/Add%20to-AltStore-2496ED?style=flat-square&logo=apple&logoColor=white" alt="AltStore" /></a>
    <a href="https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=feather"><img src="https://img.shields.io/badge/Add%20to-Feather-FF3B30?style=flat-square&logo=apple&logoColor=white" alt="Feather" /></a>
    <a href="apple-magnifier://install?url=https%3A%2F%2Fgithub.com%2Filluminati945%2FNuvioMobile-iOS%2Freleases%2Fdownload%2Fv0.4.12%2FNuvio-v0.4.12-Enhanced.ipa"><img src="https://img.shields.io/badge/Install%20with-TrollStore-FF5C00?style=flat-square&logo=apple&logoColor=white" alt="TrollStore" /></a>
  </p>

</div>

---

### 📲 Sideload Source

Paste into **SideStore**, **AltStore**, or **Feather**:

```text
https://raw.githubusercontent.com/illuminati945/NuvioMobile-iOS/main/NuvioEnhanced.json
```

| Installer | Action |
|---|---|
| **LiveContainer / LiveLauncher** | [**🚀 Open in LiveContainer**](https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=livecontainer) or select `.ipa` via `+` |
| **SideStore** | [**📲 Add Source**](https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=sidestore) |
| **AltStore** | [**📲 Add Source**](https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=altstore) |
| **Feather** | [**🪶 Add Source**](https://altdirect.app/?url=https%3A%2F%2Fraw.githubusercontent.com%2Filluminati945%2FNuvioMobile-iOS%2Fmain%2FNuvioEnhanced.json&r=feather) |
| **TrollStore** | [**⚡ 1-Tap Install**](apple-magnifier://install?url=https%3A%2F%2Fgithub.com%2Filluminati945%2FNuvioMobile-iOS%2Freleases%2Fdownload%2Fv0.4.12%2FNuvio-v0.4.12-Enhanced.ipa) |
| **Direct IPA** | [**📦 Download Latest `.ipa`**](https://github.com/illuminati945/NuvioMobile-iOS/releases/latest) |

---

### ✨ Highlights

- ⚡ **Background Downloads & Notifications**: Continuous downloads with background assertions, Dynamic Island / Live Activities, and local completion alerts.
- 📂 **iOS Files App Access**: `UIFileSharing` support — access downloads directly in **Files > On My iPhone > Nuvio Enhanced** for Infuse, VLC, or Outplayer playback.
- 📺 **Live TV & EPG**: M3U playlists, XMLTV electronic program guide, and fast channel navigation.
- 🎬 **Custom MPV Player**: Tap-to-seek, custom subtitle fonts, language groupings, and track preservation across streams.
- 🔄 **Cloud Tracking**: Two-way Trakt & Simkl synchronization.
- 🤖 **AI Assistant**: Smart content recommendations and synopses.

---

<details>
<summary><b>🛠️ Local Compilation Guide</b></summary>

```bash
git clone --recurse-submodules https://github.com/illuminati945/NuvioMobile-iOS.git
cd NuvioMobile-iOS/iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Release -destination 'generic/platform=iOS' build
```
</details>

---

<div align="center">
  <sub>Built with ❤️ for the Nuvio community • Automated Apple Silicon CI/CD</sub>
</div>
