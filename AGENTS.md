# Nuvio Enhanced iOS — Agent Instructions & Architecture Guide

This document contains operational workflows, build instructions, CI/CD automation, and iOS architecture guidelines for developing and maintaining the Nuvio Enhanced iOS codebase. Any AI agent joining this workspace should follow these instructions to implement features, fix bugs, trigger builds, and manage releases autonomously.

---

## 1. 🌿 Repository & Branch Strategy

- **Repository**: `https://github.com/illuminati945/NuvioMobile-iOS`
- **Architecture**: Kotlin Multiplatform (Compose Multiplatform iOS + Native Swift host in `iosApp/` + `MPVKit`).
- **Primary Working Branches**:
  - `main`: Tracks stable releases, documentation, and SideStore/AltStore community source manifests.
  - `enhanced`: Active development and CI build target for GitHub Actions.
- **Rule**: Whenever pushing commits, **always push to both `main` and `enhanced` in lockstep** so they stay synchronized:
  ```bash
  git push origin main
  git checkout enhanced && git merge main
  git push origin enhanced
  git checkout main
  ```

---

## 2. 📢 Discord CI/CD Tracking & Notifications

Builds are monitored live and streamed to Discord channels:

- **Channel A (Live CI/CD Tracker)**: Posts real-time compilation step progress, progress bars, and elapsed time every 30 seconds.
- **Channel B (High-Level Announcements & Updates)**: Posts version release notes, changelogs, build success status with direct `.ipa` download links, or build failure diagnostics with exact compiler errors and fixes.

### Configuration & Daemon
- Configuration is stored locally in `scratch/discord_config.json` (contains GitHub PAT and Discord webhook URLs).
- **Run the background watcher daemon during every CI build**:
  ```bash
  python3 /home/ubuntu/.gemini/antigravity-cli/brain/b9d69cc5-7785-4024-bff0-feac8ab3b824/scratch/discord_watcher.py <RUN_ID>
  ```
- The watcher automatically edits old embeds to keep the channel clean and posts fresh bottom announcements with unread pings when the job completes.

---

## 3. 🚀 Triggering Automated CI/CD Builds

Trigger an Apple Silicon macOS build via GitHub Actions workflow dispatch:

```python
import json, urllib.request

# Load token from scratch/discord_config.json
with open('/home/ubuntu/.gemini/antigravity-cli/brain/b9d69cc5-7785-4024-bff0-feac8ab3b824/scratch/discord_config.json') as f:
    config = json.load(f)

token = config['github_token']
headers = {
    'Authorization': f'Bearer {token}',
    'Accept': 'application/vnd.github+json',
    'User-Agent': 'nuvio-ios-setup',
    'X-GitHub-Api-Version': '2022-11-28'
}
url = 'https://api.github.com/repos/illuminati945/NuvioMobile-iOS/actions/workflows/build-ipa.yml/dispatches'
data = json.dumps({
    'ref': 'enhanced',
    'inputs': {
        'mode': 'enhanced',
        'description': 'Build Nuvio Enhanced iOS'
    }
}).encode('utf-8')
req = urllib.request.Request(url, data=data, headers=headers, method='POST')
with urllib.request.urlopen(req) as resp:
    print('Workflow dispatched!')
```

To fetch the new Run ID:
```python
url = 'https://api.github.com/repos/illuminati945/NuvioMobile-iOS/actions/runs?per_page=5'
req = urllib.request.Request(url, headers=headers)
with urllib.request.urlopen(req) as resp:
    runs = json.load(resp)['workflow_runs']
    latest_run_id = runs[0]['id']
    print(f'Latest Run ID: {latest_run_id}')
```

---

## 4. 🏷️ Versioning & Release Checklist

When fixing bugs or adding new features, follow this exact sequence:

1. **Increment Version**:
   - Update `iosApp/Configuration/Version.xcconfig`:
     ```ini
     CURRENT_PROJECT_VERSION=<build_number>  # e.g. 121 -> 122
     MARKETING_VERSION=<semver>              # e.g. 0.4.17 -> 0.4.18
     ```
2. **Update SideStore / AltStore Manifests**:
   - Update `NuvioEnhanced.json` and `NuvioFull.json`:
     - Update `version`, `buildVersion`, and `date`.
     - Update `downloadURL`: `https://github.com/illuminati945/NuvioMobile-iOS/releases/download/v<version>/Nuvio-v<version>-Enhanced.ipa`.
     - Update `localizedDescription` with release bullet points.
3. **Update Documentation**:
   - Add a new section in `CHANGELOG.md` under `## <version> - <YYYY-MM-DD>`.
   - Update `README.md` download links and feature list if applicable.
4. **Push to GitHub & Dispatch CI Build**:
   - Commit changes and push to `main` and `enhanced`.
   - Dispatch `build-ipa.yml` on `enhanced`.
   - Start `discord_watcher.py <RUN_ID>` as a background task.
5. **Post-Build Exact Size Sync**:
   - Once the build succeeds, query the release asset:
     `https://api.github.com/repos/illuminati945/NuvioMobile-iOS/releases/tags/v<version>`
   - Copy the exact byte size (`size`) into `NuvioEnhanced.json` and `NuvioFull.json`.
   - Commit and push the size sync to `main` and `enhanced`.

---

## 5. 🛡️ Critical iOS Architecture Rules

### A. Clean Touch & Gesture Handling in Compose Multiplatform
- **Rule**: In Compose Multiplatform for iOS (`ui-uikit`), never wrap top-level navigation routes or screen destinations with custom full-screen gesture recognizers or `pointerInput` overlays. Custom containers disrupt the UIKit-to-Compose hit-testing tree and intercept clicks from child buttons, carousels, and lists.
- **Requirement**: Allow standard navigation destinations to render directly inside `entry<Route>` so that all touch events pass unobstructed to Compose controls.

### B. App Launch Overlay Auto-Dismiss Safety
- In `composeApp/src/commonMain/kotlin/com/nuvio/app/App.kt`:
  - `initialHomeReady` must always have a safety fallback `LaunchedEffect` with a short delay (e.g. 1.2s) to guarantee the splash/loading overlay dismisses even if catalogs encounter network latency or empty state.

### C. Compose Multiplatform Inset Crashes (`UIViewLayoutRegion`)
- **Root Cause**: `CMPLayoutRegion.o` in `org.jetbrains.compose.ui:ui-uikit` invokes `+[UIViewLayoutRegion marginsLayoutRegionWithCornerAdaptation:]` and `-[UIView edgeInsetsForLayoutRegion:]`.
- **Requirement**: Maintain the complete Objective-C class implementation and `UIView (UIViewLayoutRegionSupport)` category in `iosApp/iosApp/AppIconBridge.m`. Do not remove or stub this without full dynamic method forwarding.

### D. Files App Sharing (`UIFileSharingEnabled`)
- In `iosApp/iosApp/Info.plist`:
  - `<key>UIFileSharingEnabled</key><true/>`
  - `<key>LSSupportsOpeningDocumentsInPlace</key><true/>`
- Exposes `Documents/nuvio_downloads` in the iOS **Files** app under **On My iPhone ➔ Nuvio Enhanced** (and in LiveContainer / LiveLauncher shared files).

### E. Background Downloads & Notifications
- In `iosApp/iosApp/Info.plist`:
  - `UIBackgroundModes`: `audio`, `fetch`, `processing`.
- In `composeApp/src/iosMain/kotlin/com/nuvio/app/features/downloads/DownloadsPlatformDownloader.ios.kt`:
  - Keep downloads active using `UIApplication.sharedApplication.beginBackgroundTaskWithName`.
  - Do NOT pause downloads on `applicationDidEnterBackground`.
  - Send local completion and failure notifications via `UNUserNotificationCenter`.

### F. Liquid Glass Frosted Navigation Bar
- In `composeApp/src/commonMain/kotlin/com/nuvio/app/core/ui/NavigationBar.kt`:
  - Uses `hazeEffect` frosted backdrop blur, subtle specular highlights, and a stable fixed container height (`64.dp`) with scroll-locking during tab transitions to prevent vertical bouncing.
  - Enabled by default out of the box in `ThemeSettingsRepository.kt`.

### G. Security & Secrets Management
- **Rule**: Never commit raw Discord webhook URLs or GitHub Personal Access Tokens (PATs) directly into repository files or git history.
- Always read credentials from local `scratch/discord_config.json` or system environment variables.

