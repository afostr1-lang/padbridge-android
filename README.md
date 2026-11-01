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

## Controller Studio upgrade (work in progress)

- **Controller Lab** shows live foreground joystick axes and recent controller button input. It saves dead-zone (0–40%) and vertical inversion preferences separately for each detected controller.
- **Mapping Studio** selects a saved game, adds a virtual touch target for a chosen controller button, drags it around a preview canvas, and stores normalized positions (0–1). Tap a target to select and delete it.
- **Profiles** still support renaming a profile. The mapping editor currently stores one layout per game; multiple independent layouts and runtime input mapping are future steps.
- Calibration preferences are not yet applied to game input. Mapping layout preview is **not an in-game overlay** and does **not** dispatch touchscreen events.
- Existing games in `padbridge.db` are preserved; mappings use their own `padbridge-mappings.db`, and calibration is saved in private app preferences.

### Controller test
Pair your Bluetooth gamepad in Android Settings. Open Controller in PadBridge, press each button, move each stick and both triggers; verify the reported key names and axis values. Adjust a dead zone, save, leave the screen, and return to confirm persistence. Add a game, open Map, add A/B targets, drag them, switch tabs and return to verify stored placements.

### Android 16 next milestone
A production mapping engine will need foreground/background gamepad input feasibility tests, an explicit AccessibilityService for supported gesture dispatch, overlay permission and robust user-enabled activation. No such permission is currently requested or enabled. Do not interpret a successful cloud build as proof of game compatibility.

## Premium teal/cyan redesign

PadBridge now uses an original gaming-focused look: deep navy backgrounds, teal/cyan highlights, rounded utility cards, clear status indicators, and six screens (Home, Games, Lab, Map, Profiles, Setup). The design follows familiar controller-mapper usability patterns without reproducing third-party artwork or branding.

### Supported today
- Home: controller status, saved game count, active touch target count and shortcuts.
- Games: discover installed apps, configure, launch and remove with a removal confirmation.
- Lab: foreground button diagnostics, analog stick visualizers, calibration settings.
- Map: saved target positioning with grid canvas, game selection, edit and delete.
- Profiles: rename stored profile and jump to that game's editor.
- Setup: guided pairing/calibration/mapping instructions and implementation status.

### Still not implemented
The mapping screen is a saved layout editor, not an Android system overlay. It does not inject touches while another game is running. AccessibilityService, controller interception outside PadBridge, touch gesture accuracy, orientation-specific profiles, and runtime activation all require separate Android 16 implementation and device validation.
