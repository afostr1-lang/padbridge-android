# PadBridge — Phase 1

Personal, offline-first Android gamepad companion for Bluetooth controllers.

## Phase 1 features
- Controller discovery and live foreground input diagnostics.
- Add and launch installed Android applications.
- Store one named game profile per app locally in SQLite.
- Jetpack Compose home, game library, controller, and profiles screens.

**Not yet a functional touchscreen mapper.** Android foreground controller events do not imply the ability to intercept inputs when a game is in the foreground.

## Build without Android Studio
Open **Actions → Build PadBridge Debug APK → Run workflow** in GitHub. After the run succeeds, download the **PadBridge-debug-apk** artifact, extract the ZIP, and install the APK on your Android device. The GitHub-hosted build is unverified until the workflow completes.

## Target platform
Android 16 / API 36; minimum supported API 28. Debug builds are signed with the CI runner's debug key. Because that key can change, later APKs may require uninstalling prior debug installs; export user data before uninstalling.

## Phase 2 engineering notes
1. Prototype Android AccessibilityService dispatchGesture and overlay-based layout editor.
2. Test whether gamepad button and analog events are observable outside the foreground activity; do not assume it is possible via public Android APIs.
3. Investigate Shizuku / ADB only if essential and with explicit local user activation.
4. Evaluate multitouch, orientation changes, safety against accidental touches, and application compatibility.
5. Do not circumvent games' anti-cheat or device-integrity protections.

No internet, analytics, account, or cloud backend is required by Phase 1.
