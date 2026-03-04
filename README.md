<picture>
        <source media="(prefers-color-scheme: dark)" srcset="docs/media/logo/simmerly_logo_text_white.png">
        <source media="(prefers-color-scheme: light)" srcset="docs/media/logo/simmerly_logo_text_black.png">
        <img src="docs/media/logo/simmerly_logo_text_black.png" alt="Simmerly Logo" width="300">
</picture>

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin)](https://www.jetbrains.com/kotlin-multiplatform/)
[![Compose](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpackcompose)](https://www.jetbrains.com/compose-multiplatform/)

<br/>

Simmerly is a Free and Open Source (FOSS) frontend client for [Mealie](https://mealie.io/), the
self-hosted recipe manager. It brings the power of Mealie to your fingertips on Android, iOS, and
desktop platforms — all from a single Kotlin codebase.

<br/>

## Platforms

| Platform                        | Status      |
|---------------------------------|-------------|
| Android                         | ✅ Supported |
| iOS                             | ✅ Supported |
| Desktop (Windows, macOS, Linux) | ✅ Supported |

<br/>

## Tech Stack

### UI

- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) — shared UI across all
  platforms
- [Material 3](https://m3.material.io/components) — design system and components
- [Material 3 Adaptive](https://developer.android.com/develop/ui/compose/layouts/adaptive) —
  adaptive layouts for different screen sizes
- [Coil](https://github.com/coil-kt/coil) — image loading

### Navigation

- [Navigation 3](https://developer.android.com/jetpack/compose/navigation) — type-safe navigation
  for Compose Multiplatform

### Architecture

- MVI pattern with [MVIKotlin](https://github.com/arkivanov/MVIKotlin/)
- [Koin](https://github.com/InsertKoinIO/koin) — dependency injection
- [Essenty](https://github.com/arkivanov/Essenty) — lifecycle utilities for KMP
- [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) —
  ViewModel & lifecycle-aware Compose

### Networking

- [Ktor](https://github.com/ktorio/ktor) — HTTP client for all platforms
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization) — JSON parsing

### Data

- [Room](https://developer.android.com/training/data-storage/room) — local database (KMP)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) — preferences
  storage
- [Kotlinx DateTime](https://github.com/Kotlin/kotlinx-datetime) — date and time utilities

### Async

- [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) — asynchronous operations

### Other

- [Compose Rich Text](https://github.com/MohamedRejeb/compose-rich-editor) — rich text rendering and
  editing

<br/>

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
