# Project Overview: Factory-9

## Introduction
This is a **Kotlin Multiplatform (KMP)** project called "Factory-9". It targets **Android** and **iOS**. It uses **Compose Multiplatform** for sharing UI code across these platforms.

## Architecture & Modules

### Root Structure
*   `androidApp`: Android application module.
*   `iosApp`: Xcode project for the iOS application.
*   `shared`: The core shared module containing business logic and shared UI.
*   `gradle`: Contains version catalog (`libs.versions.toml`) and wrapper.

### The `shared` Module
The heart of the application. It is consumed by all other platform-specific modules.
*   **Source Sets:**
    *   `commonMain`: Code shared across all platforms (UI, ViewModels, Domain logic).
    *   `androidMain`: Android-specific implementations.
    *   `iosMain`: iOS-specific implementations.
*   **Key Files:**
    *   `App.kt`: The main Composable entry point for the shared UI.
    *   `Platform.kt`: Example of platform-specific code (expect/actual).
*   **Resources:**
    *   `composeResources`: Shared resources (images, strings, fonts, etc.) are located in `shared/src/commonMain/composeResources`. These are accessed via `Res` generated class.

### Platform Modules
1.  **Android (`:androidApp`)**
    *   Standard Android App structure.
    *   `minSdk`: 24, `compileSdk`: 36.
    *   Dependencies: `androidx.activity`, `compose`, and `:shared`.

2.  **iOS (`iosApp`)**
    *   Native Xcode project.
    *   Embeds the shared code as a framework (`Shared.framework`).
    *   Entry point: `iOSApp.swift` (typically).
    *   **Run:** Open `iosApp/iosApp.xcodeproj` in Xcode and run on a Simulator/Device.

## Dependency Management
*   Dependencies are managed in `gradle/libs.versions.toml`.
*   **Adding a dependency:**
    1.  Add the version and library alias in `libs.versions.toml`.
    2.  Sync Gradle.
    3.  Add implementation in the `build.gradle.kts` of the target module (usually `shared`'s `commonMain` or specific platform source set).

## Development Workflow / "Skill"
To efficiently extend this codebase:
1.  **Shared UI First:** Always try to implement UI in `shared/src/commonMain/kotlin/org/example/project/App.kt` or new composables in that package.
2.  **Platform Divergence:** If you need platform-specific behavior (e.g., Camera, Permissions, SQL Drivers), use the `expect` keyword in `commonMain` and implement `actual` in `androidMain`, `iosMain`, etc.
3.  **Resources:** Put shared resources (images, strings) in `shared/src/commonMain/composeResources` (or similar, check `compose.components.resources` usage). **Use snake_case for filenames.**
4.  **Testing:**
    *   Unit tests go in `commonTest`.
    *   Platform tests go in `androidTest`, `iosTest`, etc.
    *   **Validation:** Run `./gradlew :shared:testDebugUnitTest` frequently.

## Common Tasks
*   **Update Kotlin Version:** Edit `kotlin` version in `libs.versions.toml`.
*   **Add a Screen:** Create a new Composable file in `shared/commonMain` and call it from `App()`.
*   **Fix Sync Issues:** Run `./gradlew --refresh-dependencies` or "Sync Project with Gradle Files" in the IDE.

## Code Style
*   Follow standard Kotlin coding conventions.
*   Use Material3 for UI components where possible.
*   **Responsive UI:** Use `Box(contentAlignment = Alignment.Center)` with `widthIn(max = 600.dp)` for main content to support broader screens if needed.
*   **Aesthetic Requirements:** Ensure UI styling matches the specified Film Noir / Classic Old School Movie Theme for Factory-9.

## Prompting
* When I say "Execute", that means that I have validated the work and I am ready for you to continue with the `GUIDE.md`.
* Ensure that the `GUIDE.md` periodically invokes the `CALCULATOR.md` to determine how complete the project is.
* **CRITICAL**: Always begin your response with the current active Phase and Step from `GUIDE.md` formatted exactly like `[Phase X - Step Y]`. You determine the active phase and step by finding the first unchecked `- [ ]` task in `GUIDE.md` and looking at its parent headers.

## Skills & Best Practices
For more specific technical guidance (e.g., creating run configs, working with Room, or Navigation), refer to **`SKILL.md`**.