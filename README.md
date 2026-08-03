# AppRecommendations

Shared recommendation configuration and icon assets for the iOS apps.

## Contents

- `RecommendedApps.json`: shared recommendation copy, colors, icon names, and App Store URLs.
- `Icons/*.png`: shared icon files copied into each app bundle at build time.
- `Scripts/copy_app_recommendations.sh`: Xcode Run Script helper used by each app target.

Each app copies these files during the Xcode build phase and reads them from its own app bundle at runtime.

## Integration

See [INTEGRATION_RULES.md](INTEGRATION_RULES.md) for the shared business boundaries, Swift Package setup, Xcode Cloud bootstrap, migration sequence, and validation checklist derived from the iNature, Mr.Mushroom, Mr.Rock, and NatureEar integrations.
