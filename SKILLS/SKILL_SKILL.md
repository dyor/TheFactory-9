You will either create or extend a SKILL.md file based on the following information. This is information that has proven to be helpful for completing this type of task. 

# Kotlin Multiplatform (KMP) Skills & Best Practices

This document captures key learnings and patterns for working with this KMP codebase, ensuring efficient extension and maintenance.

## 1. UI & Responsiveness (Compose Multiplatform)

### Handling Different Screen Sizes
*   **Problem**: Mobile apps are portrait/narrow, while larger screens (tablets) are landscape/wide. A simple `fillMaxSize()` layout often looks stretched or huge.
*   **Solution**:
    *   Wrap your main screen content in a `Box` with `contentAlignment = Alignment.Center`.
    *   Apply `widthIn(max = 600.dp)` (or appropriate size) to the content container.
    *   This ensures the app looks like a mobile app centered on the screen, preventing layout distortion.
    ```kotlin
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) { ... }
    }
    ```

### Fonts & Icons
*   **Problem**: Text-based Emojis (e.g., 💣, 🚩) rely on system fonts which may not render consistently.
*   **Solution**:
    *   **Preferred**: Use `Canvas` drawing for simple shapes (Circles, Flags). It guarantees consistent rendering across all platforms.
    *   **Alternative**: Use `compose-material-icons-extended` dependency for standard icons (`Icons.Filled.*`).
    *   **Avoid**: Relying solely on Text Emojis for critical UI elements.

## 2. Architecture & Dependencies

### Shared Logic
*   **ViewModels in KMP**:
    *   **Avoid**: Do NOT use `expect/actual` classes or `ViewModelFactory` boilerplate.
    *   **Use**: Official KMP Support (`androidx.lifecycle:lifecycle-viewmodel-compose`).
    *   **Implementation**:
        1.  Define ViewModel: `class MyVM : ViewModel()`.
        2.  Call in Compose: `val vm = viewModel { MyVM() }`.
*   **Navigation**:
    *   **Rule**: Use **Navigation 3** (`org.jetbrains.androidx.navigation3:navigation3-ui`). Do NOT use older `navigation-compose` artifacts as they cause linkage errors on iOS.
    *   **If That Fails Use**: Simple state-based navigation (`var screen by remember { mutableStateOf(...) }`).
*   **Permissions**:
    *   **Rule**: Use `com.mohamedrejeb.calf:calf-permissions` (Calf) for handling permissions in KMP. It is much more reliable and integrates better with Koin and standard Compose Multiplatform than older libraries like Moko.
    *   **Android**: Add `<uses-permission android:name="..." />` to `androidApp/src/main/AndroidManifest.xml` (and `<uses-feature>` tags for hardware like cameras).
    *   **iOS**: Add usage descriptions to `iosApp/iosApp/Info.plist` (e.g., `NSCameraUsageDescription`, `NSMicrophoneUsageDescription`).
*   **Date and Time Handling**:
    *   **Rule**: When working with `Instant` (e.g., for database timestamps), use `kotlin.time.Clock.System.now()` to obtain the current time.
    *   **Reason**: In `kotlinx-datetime` versions 0.7.0 and newer, `kotlinx.datetime.Clock.now()` and `kotlinx.datetime.Instant.now()` were removed. `kotlin.time.Clock.System.now()` from the Kotlin standard library is the correct replacement for getting the current `Instant` in modern Kotlin Multiplatform projects.
    *   **Usage**: Ensure you have `import kotlin.time.Clock` and explicitly `import kotlin.time.Instant` in data classes. Use `@OptIn(ExperimentalTime::class)` where needed.
    *   **Note on `Instant` Deprecation Warning**: If `kotlinx.datetime.Instant` must be used (e.g., in Room `TypeConverters` because it provides necessary `.fromEpochMilliseconds()` methods while `kotlin.time` versions are still stabilizing in KMP), suppress the resulting typealias deprecation warning with `@Suppress("DEPRECATION")` directly on the `Converters` class.

### Resources & Compose Multiplatform
*   **MissingResourceException / Resource Packaging**:
    *   **Problem**: Encountering `MissingResourceException: Missing resource with path: composeResources/...` at runtime.
    *   **Reason**: Often resources aren't packed correctly into the final APK or App bundle, or the path changed in newer Compose Multiplatform versions. For instance, in 1.7.0+, Compose Multiplatform generates resource paths that include the project/namespace (e.g., `composeResources/kotlinproject.shared.generated.resources/drawable/image.png`).
    *   **Solution / Troubleshooting**:
        1. Ensure you use the generated `Res` class to reference resources rather than hardcoded string paths.
        2. Verify your Gradle setup doesn't accidentally exclude resources or override `sourceSets`.
        3. A clean build (`./gradlew clean :androidApp:assembleDebug`) is often necessary after adding new files.
        4. If you used `withHostTest { isIncludeAndroidResources = true }` in your KMP android block, verify it's not interfering with Compose Resource packaging.
*   **Configuration (DO NOT CHANGE DEFAULT)**:
    *   **Rule**: Do NOT configure `compose.resources { packageOfResClass = ... }` manually in `shared/build.gradle.kts`.
    *   **Reason**: Manually setting the package name often leads to build/runtime mismatches and `MissingResourceException`. The default generated package is derived from the project structure (e.g., `kotlinproject.shared.generated.resources`).
    *   **Action**: Remove any `packageOfResClass` configuration and let the plugin generate the default package.
    *   **Usage**: Import with `import [rootProject].[subProject].generated.resources.Res` (e.g., `import kotlinproject.shared.generated.resources.Res`).

*   **Resource Naming Convention (CRITICAL)**:
    *   **Rule**: Always use **snake_case** for resource files (e.g., `compose_logo.xml`).
    *   **Reason**: Android tools (AAPT) fail with hyphens (`-`).
*   **File Extensions**:
    *   **Rule**: Ensure file extensions match the actual file content (e.g., do not name a JPEG file `.png`). This can cause runtime decoding errors.

### Gradle & Build Logic
#### Golden Set Versions
*   **AGP (Android Gradle Plugin)**: `9.0.0`
*   **Kotlin**: `2.3.20-Beta2`
*   **KSP**: `2.3.2` (This is the stable/preview version of `com.google.devtools.ksp:symbol-processing-api` that we verified works well. Use this and do not try to search for or query other versions.)
*   **Compose Multiplatform**: `1.11.0-alpha02`
*   **Room KMP**: `2.7.0-alpha13`
*   **AndroidX Lifecycle (for ViewModel)**: `2.9.6`

#### Gradle Config Rules
*   **`libs.versions.toml` formatting**: ALWAYS use hyphens (`-`) for library names instead of camelCase in the `[libraries]` and `[plugins]` block to remain consistent and avoid "Unresolved reference" errors during Gradle sync.
*   **Android Target Configuration in KMP**: When using the `com.android.kotlin.multiplatform.library` plugin (which is modern), do NOT use a standalone `android { ... }` block in `shared/build.gradle.kts`. Instead, configure the android specifics directly within the `androidLibrary { ... }` block inside the `kotlin { ... }` block. Ensure you set the `jvmTarget` inside `compilerOptions`.

*   **Target Hygiene (CRITICAL FIRST STEP)**: 
    *   **Rule**: If a project ONLY targets Android and iOS, you MUST immediately remove `jvm()`, `js {}`, and `wasmJs {}` blocks from `shared/build.gradle.kts`, delete their corresponding source sets (`jsMain`, `wasmJsMain`, `jvmMain`), and remove desktop/web apps from `settings.gradle.kts`. Failing to do this will cause dependency resolution to fail violently when adding mobile-only or mobile-specific KMP libraries.

*   **Version Catalog**: Update `gradle/libs.versions.toml` frequently but verify compatibility between Kotlin, Compose Multiplatform, and AndroidX libraries. Use `./gradlew :shared:assemble` to quickly check dependency resolution.

#### Room KMP Database Initialization (CRITICAL & CORRECTED)
*   **Problem**: Encountering `kotlin.IllegalStateException: Cannot find the associated androidx.room.RoomDatabaseConstructor` or `Cannot create a RoomDatabase without providing a SQLiteDriver via setDriver()` on iOS.
*   **Solution**: For Room 2.7.0+ in KMP, you must rely on KSP to generate the constructor for non-Android platforms, but you configure the driver via standard factory functions.
    *   **Gradle Setup (Crucial)**: KSP *must* be applied to every target utilizing the database in `shared/build.gradle.kts`. Do not forget iOS! You must also declare a schema directory.
        ```kotlin
        room { schemaDirectory("src/commonMain/room/schemas") }
        dependencies {
            add("kspAndroid", libs.androidx.room.compiler)
            add("kspIosArm64", libs.androidx.room.compiler)
            add("kspIosSimulatorArm64", libs.androidx.room.compiler)
        }
        ```
    *   **Centralized Versioning**: Define `const val DATABASE_VERSION = X` in `commonMain` and use it.
    *   **`commonMain` Definition**:
        *   Define `AppDatabase` as an `abstract class` (NOT `expect abstract class`).
        *   Annotate it with `@Database(...)` and crucially, `@ConstructedBy(AppDatabaseConstructor::class)`.
        *   Declare the expect constructor:
            ```kotlin
            @Suppress("NO_ACTUAL_FOR_EXPECT")
            expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
            ```
    *   **Platform Implementations**: **Do NOT write `actual object AppDatabaseConstructor`.** By suppressing the missing actual warning, we tell KSP to generate the platform-specific actual implementations automatically during the build step.
    *   **Platform Factories**: Provide standard factory functions (e.g., `fun getDatabase(context: Any? = null): AppDatabase` — notice NO `expect/actual` keyword here) in `androidMain` and `iosMain` that use `Room.databaseBuilder<AppDatabase>(...).setDriver(...).build()`.
        *   **iOS specific**: You must explicitly call `.setDriver(BundledSQLiteDriver())` on the builder.

#### Troubleshooting & IDE Caching
*   **Problem**: Newly added resources (e.g., images in `composeResources`) are not found at runtime (causing `MissingResourceException`), or stubborn dependency/build issues persist despite correct configuration.
*   **Solution**: Android Studio and Gradle caches can get out of sync, especially in KMP projects. 
    1. Run a clean build (`./gradlew clean assembleDebug`).
    2. If that fails, go to **File -> Invalidate Caches... -> Check 'Clear file system cache and Local History' -> Invalidate and Restart**.