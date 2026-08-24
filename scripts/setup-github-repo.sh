#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 1 ]; then
    echo "Usage: ./scripts/setup-github-repo.sh <github-username>/<repo-name>"
    echo "Example: ./scripts/setup-github-repo.sh myuser/NuvioMobile-iOS"
    exit 1
fi

TARGET_REPO="$1"
TARGET_USER="$(echo "$TARGET_REPO" | cut -d'/' -f1)"
REPO_NAME="$(echo "$TARGET_REPO" | cut -d'/' -f2)"

echo "Setting up repository for: ${TARGET_REPO} (${TARGET_USER}/${REPO_NAME})"

# Update README and Docs
sed -i "s|YOUR_GITHUB_USER/YOUR_REPO|${TARGET_REPO}|g" README.md IOS_SETUP.md
sed -i "s|https://github.com/AKRusso/NuvioMobile-Enhanced|https://github.com/${TARGET_REPO}|g" NuvioEnhanced.json NuvioFull.json
sed -i "s|\"website\": \"https://github.com\"|\"website\": \"https://github.com/${TARGET_REPO}\"|g" NuvioEnhanced.json NuvioFull.json

echo "Updated references in README.md, IOS_SETUP.md, NuvioEnhanced.json, and NuvioFull.json."

# Configure git remote
if git remote get-url origin >/dev/null 2>&1; then
    git remote set-url origin "https://github.com/${TARGET_REPO}.git"
else
    git remote add origin "https://github.com/${TARGET_REPO}.git"
fi

echo "Git remote 'origin' set to: https://github.com/${TARGET_REPO}.git"
echo "Done! You can now commit and push with: git push -u origin enhanced"
