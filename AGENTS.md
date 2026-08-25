# Nuvio Enhanced iOS — Agent Instructions & Workflow Guide

This document contains mandatory operational rules, secrets, architecture guidelines, and procedures for any AI assistant or developer working on this codebase across sessions.

---

## 1. 🔑 Repository, Credentials & Branch Strategy

- **Repository**: `https://github.com/illuminati945/NuvioMobile-iOS`
- **GitHub Username**: `illuminati945`
- **GitHub PAT Token**: Stored in environment / user context 
- **Primary Working Branches**:
  - `main`: Tracks primary stable state and SideStore manifests.
  - `enhanced`: Active development and CI build target.
- **Rule**: Whenever pushing commits, **always push to both `main` and `enhanced`** so they stay synchronized:
  ```bash
  git push origin main
  git checkout enhanced && git merge main
  git push origin enhanced
  git checkout main
  ```

---

## 2. 📢 Discord Webhook & Reporting Protocols

There are **two distinct Discord webhooks** with strict formatting and lifecycle rules:

### Channel A: Detailed Live CI/CD Tracker
- **Webhook URL**: `Stored securely in local environment / secret manager (Channel A Webhook)`
- **Behavior**:
  - Posts initial message when compilation begins.
  - Updates progress, current step, and elapsed time every 30 seconds via `PATCH /messages/{msg_id}`.
  - Sends a standalone **heartbeat message every 15 minutes**.
  - Always includes the direct GitHub Actions run link.
  - Edits the embed to green on success or red on failure with release links.

### Channel B: High-Level Announcements & Updates Channel
- **Webhook URL**: `Stored securely in local environment / secret manager (Channel B Webhook)`
- **Behavior**:
  - When build starts: Posts an announcement containing the **complete changelog for the version**, the start timestamp, and the compilation link.
  - When release finishes: **Edits the same message (via `PATCH /messages/{msg_id}`)** to mark it `Released`, providing the direct `.ipa` download link, release tag, SideStore source URL, total build time, and changelog.

### Background Watcher Daemon
Always run the background watcher script during CI runs:
```bash
python3 /home/ubuntu/.gemini/antigravity-cli/brain/b9d69cc5-7785-4024-bff0-feac8ab3b824/scratch/discord_watcher.py <RUN_ID>
```

---

## 3. 🚀 Triggering Automated CI/CD Builds

Trigger a new Apple Silicon macOS build via the GitHub REST API:
```python
import urllib.request, json

# Use the token from active context
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
urllib.request.urlopen(req)
```

---

## 4. 🏷️ Versioning & Release Checklist

When fixing bugs or adding features, follow this sequence:
1. **Increment Version**:
   - Update `iosApp/Configuration/Version.xcconfig`:
     ```ini
     CURRENT_PROJECT_VERSION=<build_number>
     MARKETING_VERSION=<semver>
     ```
2. **Update SideStore / AltStore Manifests**:
   - Update `NuvioEnhanced.json` and `NuvioFull.json`:
     - Set `version`, `buildVersion`, `date`.
     - Set direct `downloadURL`: `https://github.com/illuminati945/NuvioMobile-iOS/releases/download/v<version>/Nuvio-v<version>-Enhanced.ipa`.
     - Set exact `size` in bytes.
3. **Update Documentation**:
   - Add entry to `CHANGELOG.md` under `## <version> - <YYYY-MM-DD>`.
   - Update `README.md` features if applicable.
4. **Push to GitHub & Dispatch Build**:
   - Push to `main` and `enhanced`.
   - Trigger `build-ipa.yml`.
   - Start `discord_watcher.py <RUN_ID>` in background.

---

## 5. 🛡️ Critical iOS Architecture Rules

### A. Compose Multiplatform Inset Crashes (`UIViewLayoutRegion`)
- **Root Cause**: `CMPLayoutRegion.o` in `org.jetbrains.compose.ui:ui-uikit` invokes `+[UIViewLayoutRegion marginsLayoutRegionWithCornerAdaptation:]` and `-[UIView edgeInsetsForLayoutRegion:]`.
- **Requirement**: Maintain the complete Objective-C class implementation and `UIView (UIViewLayoutRegionSupport)` category in `iosApp/iosApp/AppIconBridge.m`. Do not remove or stub this without full dynamic method forwarding.

### B. Files App Sharing (`UIFileSharingEnabled`)
- In `iosApp/iosApp/Info.plist`:
  - `<key>UIFileSharingEnabled</key><true/>`
  - `<key>LSSupportsOpeningDocumentsInPlace</key><true/>`
- Exposes `Documents/nuvio_downloads` in the iOS **Files** app under **On My iPhone ➔ Nuvio Enhanced** (and in LiveContainer / LiveLauncher shared files).

### C. Background Downloads & Notifications
- In `iosApp/iosApp/Info.plist`:
  - `UIBackgroundModes`: `audio`, `fetch`, `processing`.
- In `composeApp/src/iosMain/kotlin/com/nuvio/app/features/downloads/DownloadsPlatformDownloader.ios.kt`:
  - Keep downloads active using `UIApplication.sharedApplication.beginBackgroundTaskWithName`.
  - Do NOT pause downloads on `applicationDidEnterBackground`.
  - Send local completion and failure notifications via `UNUserNotificationCenter`.

### D. Download Location in Settings UI
- In `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/NuvioEnhancedSettingsPage.kt`:
  - Never wrap the **Downloads Section** with `if (!isIos)`.
- In `composeApp/src/iosMain/kotlin/com/nuvio/app/features/downloads/DownloadsExternalFolderPlatform.ios.kt`:
  - Display the active download path (`Files app > On My iPhone > Nuvio Enhanced / Downloads`) and allow launching the Files app (`shareddocuments://`).
