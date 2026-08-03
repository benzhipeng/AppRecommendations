# BioScanKit Android

Native Jetpack Compose implementation of the shared BioScanKit feature boundaries. The iOS implementation remains in the parent directory as a Swift Package.

## Packages

- `design`: adaptive iNature theme and version model.
- `settings`: page/card/row/membership/recommended-app components and action models.
- `capture`: camera slots, finder, crop geometry/configuration, and recognition processing UI.
- `paywall`: platform-neutral billing models, adapter interface, idempotent credit ledger, and iNature paywall components.

The host app owns CameraX sessions, navigation, recognition engines, history, analytics, product identifiers, Android Activities, and the RevenueCat/Google Billing adapter.

## Local integration

```kotlin
// settings.gradle.kts
include(":bioscankit")
project(":bioscankit").projectDir = file("../../AppRecommendations/BioScanKit/Android")

// app/build.gradle.kts
dependencies {
    implementation(project(":bioscankit"))
}
```

Run the library tests through a host Gradle wrapper:

```sh
cd plantCam-dev/android
./gradlew :bioscankit:testDebugUnitTest
```
