# AppRecommendations

Shared recommendation configuration and icon assets for the iOS apps.

## Contents

- `RecommendedApps.json`: shared recommendation copy, colors, icon names, and App Store URLs.
- `Icons/*.png`: shared icon files copied into each app bundle at build time.
- `Scripts/copy_app_recommendations.sh`: Xcode Run Script helper used by each app target.

Each app copies these files during the Xcode build phase and reads them from its own app bundle at runtime.
