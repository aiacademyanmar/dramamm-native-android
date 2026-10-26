# DramaMM Native Android V2.1.1

Independent Native Kotlin/View app for YouTube/TikTok drama research candidates and official source accounts.

Includes 10 review video links, source channels, Discover, Shorts, Search, My List, Profile, Details, Embedded WebView player, Settings, click sounds, scale pop animation and haptics.

**Important**: This repository currently contains a build-focused streamlined implementation. It is not a pixel-perfect copy of the prior 20-screen UI mockups, and is not a verified licensed content library. Subtitle tracks and embedded playback may vary by source. Rewards and VIP are non-transactional previews.

## Build

Actions → Build DramaMM Native APK → latest run → Artifacts → DramaMM-V2.1-debug-APK. Workflow runs on pushes to main; manual runs available via workflow_dispatch. Install `app-debug.apk` from the downloaded artifact ZIP.

For local builds, use JDK 17, Android SDK 35, Gradle 8.9, then `gradle :app:assembleDebug`.
