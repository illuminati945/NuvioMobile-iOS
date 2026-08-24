# Nuvio Enhanced iOS Setup & Sideloading Guide

This guide covers everything you need to know about installing **Nuvio Enhanced** on iOS and iPadOS, using SideStore / AltStore community sources, and triggering automated builds on GitHub Actions.

---

## ⚡ Quick Add to SideStore & AltStore

You can add this repository's update channel directly to your sideloading manager so you receive notifications and automatic updates whenever a new version is released.

### 🟣 SideStore (Recommended)
SideStore allows on-device wireless app refreshing using WireGuard without needing a computer running AltServer.

1. Open **SideStore** on your iPhone or iPad.
2. Tap the **Sources** tab in the bottom navigation bar.
3. Tap the **+** (Add) button in the top right corner.
4. Paste the following URL:
   ```text
   https://raw.githubusercontent.com/YOUR_GITHUB_USER/YOUR_REPO/main/NuvioEnhanced.json
   ```
5. Tap **Add**. You will now see **Nuvio Enhanced** in your Sources list ready for one-tap install and refresh.

### 🔷 AltStore
1. Open **AltStore** on your iPhone or iPad.
2. Go to the **My Apps** or **Sources** tab.
3. Tap the **+** button.
4. Enter the source URL:
   ```text
   https://raw.githubusercontent.com/YOUR_GITHUB_USER/YOUR_REPO/main/NuvioEnhanced.json
   ```

---

## 📦 Sideloading Installation Methods

| Method | Refresh Frequency | Requires PC? | Best For |
|---|---|---|---|
| **SideStore** | On-device every 7 days via WireGuard | Setup only | Recommended for most users |
| **AltStore** | Every 7 days via AltServer on Wi-Fi | Yes (AltServer) | Standard sideloading |
| **TrollStore** | **Permanent (Never expires)** | No | iOS 14.0 – 17.0 (supported versions) |
| **Scarlet / Esign** | Direct on device | No | Enterprise certs or direct install |
| **Sideloadly** | Every 7 days via USB/Wi-Fi | Yes | Desktop USB sideloading |
| **LiveContainer** | Unlimited apps | No | JIT & Multi-app container |

### 🚀 Direct IPA Download & Install
1. Go to the [Releases](https://github.com/YOUR_GITHUB_USER/YOUR_REPO/releases) page of this repository.
2. Download the latest `Nuvio-v<version>-Enhanced.ipa`.
3. Open with **TrollStore**, **SideStore**, **AltStore**, or **Sideloadly** to install.

---

## ⚙️ Automated Compilation with GitHub Actions

This repository includes a pre-configured CI/CD pipeline that compiles the iOS app on Apple macOS runners and produces ready-to-sideload `.ipa` packages.

### How to trigger a manual IPA build:
1. Go to the **Actions** tab in your GitHub repository.
2. Under **Workflows**, click on **Build iOS IPA**.
3. Click the **Run workflow** dropdown on the right side.
4. Choose your options:
   - **Variant**: `enhanced` (custom features + Live TV) or `full` (standard).
   - **Create Release**: check to automatically create a GitHub release with the IPA attached.
   - **Description**: Add an optional note.
5. Click the green **Run workflow** button.
6. The macOS runner will compile Kotlin Multiplatform, patch dependencies, run Xcode, and package the `.ipa` artifact.
7. Once finished (~15 minutes), download the `.ipa` file from the **Artifacts** section at the bottom of the workflow run summary!

---

## 🔄 Automatic SideStore & AltStore Source Updates

Whenever a new release is published on GitHub:
1. The `.github/workflows/update-altstore.yml` workflow automatically runs.
2. It fetches the released `.ipa`, parses `Info.plist` (version, build number, minimum iOS version), and updates `NuvioEnhanced.json` and `NuvioFull.json`.
3. It commits and pushes the updated JSON files back to the repository.
4. All users who added your SideStore / AltStore source automatically receive the update notification in their app!

---

## 🛠️ GitHub Repository Permissions Setup

To ensure GitHub Actions can create releases and push source updates:

1. In your GitHub repository, go to **Settings** -> **Actions** -> **General**.
2. Scroll down to **Workflow permissions**.
3. Select **Read and write permissions**.
4. Check **Allow GitHub Actions to create and approve pull requests**.
5. Click **Save**.

### Optional: API Keys & Secrets
If you want to bake custom API keys into your builds, go to **Settings** -> **Secrets and variables** -> **Actions** and add:
- `TMDB_API_KEY`: (Optional) Custom TMDB API key.
- `TRAKT_CLIENT_ID` / `TRAKT_CLIENT_SECRET`: (Optional) Custom Trakt API credentials.
- `SIMKL_CLIENT_ID` / `SIMKL_CLIENT_SECRET`: (Optional) Custom Simkl API credentials.
- `SUPABASE_URL` / `SUPABASE_ANON_KEY`: (Optional) Custom Supabase endpoint.

*(Note: Nuvio includes built-in fallbacks, so configuring these secrets is completely optional!)*
