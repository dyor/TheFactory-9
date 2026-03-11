## Project Overview
Factory-9 is a Kotlin Multiplatform application. It helps KMP App Builders accomplish streamlining the creation and publication of YouTube Short educational videos about the app.
Aesthetic: Film noir-classic old school movie theme.
Core Architecture: Kotlin Multiplatform (Android, iOS), Jetpack Compose / Compose Multiplatform, Room, Ktor, Koin, Coil, Compose Navigation 3, Calf permissions.

## Phase 1: Foundation & Infrastructure
Goal: Initialize the stack and establish core dependencies.

### Step 1: Project Setup
- [x] **User Action**: Run `git init`.
- [x] **Agent Action**: Configure `build.gradle.kts` with required dependencies (Room, Ktor, Koin, Coil, Compose Navigation 3, Calf permissions).
- [x] **Validation**: Ensure project builds and runs on Android and iOS then run `git add . && git commit -m "Phase 1 started"`.
- [x] **Agent Action**: Remove `desktopApp`, `jvm`, `webApp`, `js`, and `wasmJs` references from `settings.gradle.kts` and `build.gradle.kts`. This project ONLY targets Android and iOS.
- [x] **Validation**: Ensure project builds and runs on Android and iOS then run `git add . && git commit -m "Desktop, web, and wasm targets removed"`.
- [x] **User Action**: Add `film_noir.png` to codebase at `shared/src/commonMain/composeResources/drawable/film_noir.png`.
- [ ] **Agent Action**: Set `film_noir.png` as background image in `App.kt` immediately to verify resource loading.
- [ ] **Agent Action**: Adjust application style and theme based on `film_noir.png` aesthetic.
- [ ] **Validation**: Validate that the app builds and runs with the background image then run `git add . && git commit -m "Phase 1 complete"`.

## Phase 2: Core Features & Logic
Goal: Implement the primary business logic, integrations (e.g., AI interop), and local database.

### Step 1: Data Models & Persistence
- [ ] **Agent Action**: Implement base Entities, DAOs, and Database config with Room. 
- [ ] **Agent Action**: Create unit tests for Data Layer.
- [ ] **Validation**: Run unit tests for Data layer.

### Step 2: Main User Interface
- [ ] **Agent Action**: Implement Compose Navigation 3 with 6 navigation nodes: Home, Writers Room, Recording Studio, Editing Studio, Publishing Studio, and Archives.
- [ ] **Agent Action**: Create core UI screens and ViewModels for each of these screens. Start by just having 5 buttons on the Home screen (one for each of the other pages) and then a Home button on the other screens. 
- [ ] **Validation**: Manual testing of UI states.

## Phase 3: Hardware / Native Integrations (Production)
Goal: Implement device-specific features (Camera, Audio, Location, etc.).

### Step 1: Permissions
- [ ] **Agent Action**: Configure cross-platform permission requests using `calf-permissions` (com.mohamedrejeb.calf:calf-permissions).
- [ ] **Validation**: User grants permissions on Android and iOS.

### Step 2: Native Implementations
### Step 2.1: Gemini/Network and Room Implementation
- [ ] **Agent Action**: Implement `expect`/`actual` when needed for native capabilities.
- [ ] **Agent Action**: For the Writer's Room, implement a Gemini client that prompts Gemini 2.5 Flash for a 60-second script for a user provided prompt. To save time while building, including a default prompt of "Write a script for YouTube short that is designed to teach people how to create compelling YouTube shorts."
- [ ] **Agent Action**: Present the script on the screen and allow the user to edit and save it in the local Room database.
- [ ] **Validation**: Ensure that the described functionality works on Android and iOS and `git commit -m "Phase 3: Writer's Room complete"`
### Step 2.2: Camera/Video Implementation
- [ ] **Agent Action**: Allow the user to navigate to the Recording Studio after they save a script. 
- [ ] **Agent Action**: For the Recording Studio, show a front-facing camera view with a start button on the bottom half of the screen, and on the top half of the screen show the script.
- [ ] **Agent Action**: When the button on the camera view is tapped start a 5 second countdown then start teleprompting the script while recording the user. Include 3 lines at a time on the teleprompter and advance them so that we finish the script in 60 seconds.
- [ ] **Agent Action**: When the recording is done, allow user to re-record. Also include navigation to go back (to Writer's Room) and forward (to Editing Studio).
- [ ] **Validation**: Ensure that the described functionality works on Android and iOS and `git commit -m "Phase 3: Recording Studio complete"`
- [ ] **Agent Action**: For the Editing Studio, allow the user to mark sections of the video for removal (e.g., where there was white space or where they made a mistake). Include a Save button that stares the modified video and a Restore button that returns the original video.
- [ ] **Agent Action**: Include navigation for returning to the Editing Studio and advancing to the Publishing room.
- [ ] **Validation**: Ensure that the described functionality works on Android and iOS and `git commit -m "Phase 3: Editing Studio complete"`
### Step 2.3: YouTube Integration
- [ ] **Agent Action**: Provide an option to publish the video on YouTube shorts. It is ok to use simple shortcuts - like saving this video to the native Photo app and opening YouTube (where the user can upload the video from their native Photo app). 
- [ ] **Validation**: Ensure that the described functionality works on Android and iOS and `git commit -m "Phase 3: Publishing Studio complete"`

## Phase 4: The Final Cut (Cleanup & Optimization)
Goal: Polish, optimize, and prepare for production.

- [ ] **Agent Action**: Remove hardcoded values and mock data.
- [ ] **Agent Action**: Enforce naming conventions and clean up resources.
- [ ] **Agent Action**: Remove or minimize debug logging.
- [ ] **User Action**: Full regression test of the application.
- [ ] **Validation**: `git add . && git commit -m "Phase 4: Final Cut"`