# Kotlin Multiplatform (KMP) Skills & Best Practices

This document captures key learnings and patterns for working with this KMP codebase, ensuring efficient extension and maintenance.

## 1. UI & Responsiveness (Compose Multiplatform)

### Handling Different Screen Sizes
*   **Problem**: Mobile apps are portrait/narrow, while larger screens (tables) are landscape/wide. A simple `fillMaxSize()` layout often looks stretched or huge.
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

### Keyboard Management (iOS & Android)
*   **Problem**: Multi-line text inputs (like `OutlinedTextField`) keep the software keyboard open when clicking outside of them, which can block vital UI buttons (e.g., Save, Next) at the bottom of the screen. This is particularly noticeable on iOS.
*   **Solution**: Wrap your screen content in a layout (like a `Box`) and use `LocalFocusManager.current` combined with a pointer input tap detector to clear focus when the user taps outside the text field.
    ```kotlin
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) { ... }
    ```

## 2. Architecture & Dependencies

### Shared Logic
*   **ViewModels in KMP**:
    *   **Avoid**: Do NOT use `expect/actual` classes or `ViewModelFactory` boilerplate.
    *   **Use**: Official KMP Support (`androidx.lifecycle:lifecycle-viewmodel-compose`).
    *   **Implementation**:
        1.  Define ViewModel: `class MyVM : ViewModel()`.
        2.  Call in Compose: `val vm = viewModel { MyVM() }`.
*   **Navigation**:
    *   **Rule**: Use **Navigation 3** (`org.jetbrains.androidx.navigation3:navigation3-ui`) and related components. Do NOT use older `navigation-compose` artifacts as they cause linkage errors on iOS.
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

### Database Testing (Room KMP)
*   **Best Practice**: Separate common test logic from platform-specific setup to avoid `kotlin.test` lifecycle issues with `lateinit` properties.
*   **Common Test Class (`shared/src/commonTest/kotlin/.../YourDaoTest.kt`):**
    *   Create an `open class` (e.g., `open class ScriptDaoTest`).
    *   **NO `@Test` annotations** on the methods.
    *   Test methods should accept dependencies as parameters: `open fun testInsertion(database: AppDatabase, dao: ScriptDao)`.
    *   Declare `expect fun getInMemoryDatabase(): AppDatabase`.
*   **Android Tests (`shared/src/androidTest/kotlin/.../AndroidYourDaoTest.kt`):**
    *   **CRITICAL**: Use **Android Instrumented Tests** (`androidTest`), *not* Robolectric (`androidHostTest`). The `BundledSQLiteDriver` requires native libraries (`sqliteJni`) that Robolectric cannot load on the desktop JVM, leading to `UnsatisfiedLinkError`.
    *   Create a class extending the common base: `class AndroidScriptDaoTest : ScriptDaoTest()`.
    *   Declare local `private lateinit var database: AppDatabase` and `private lateinit var dao: ScriptDao`.
    *   Use `@Before` and `@After` (from `org.junit`) to manage the database lifecycle using `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)`.
    *   Override test methods, add `@Test` annotation, and call `super`: `@Test override fun testInsertion() = super.testInsertion(database, dao)`.
    *   Ensure `androidApp/build.gradle.kts` defines `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`.
    *   **Run**: `:androidApp:connectedDebugAndroidTest`.
*   **iOS Tests (`shared/src/iosTest/kotlin/.../IosYourDaoTest.kt`):**
    *   **CRITICAL**: To create an in-memory database on iOS, you MUST use `Room.inMemoryDatabaseBuilder(factory = { AppDatabaseConstructor.initialize() })`. Using `Room.databaseBuilder(name = ":memory:", ...)` will throw an `IllegalArgumentException`.
    *   Create a class extending the common base: `class IosScriptDaoTest : ScriptDaoTest()`.
    *   Declare local `private lateinit var database: AppDatabase` and `private lateinit var dao: ScriptDao`.
    *   Use `@BeforeTest` and `@AfterTest` (from `kotlin.test`) to manage the database lifecycle using your `actual fun getInMemoryDatabase()`.
    *   Override test methods, add `@Test` annotation, and call `super`: `@Test override fun testInsertion() = super.testInsertion(database, dao)`.
    *   **Run**: `:shared:iosSimulatorArm64Test`.

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
#### Running Gradle Tasks (CRITICAL AI INSTRUCTION)
*   **Rule**: **NEVER** use the raw shell command `run_shell_command("./gradlew ...")` to execute Gradle builds, tests, or syncs unless strictly necessary for a very specific low-level reason. It often hangs, loses buffer output, and causes daemon lockups. 
*   **Solution**: **ALWAYS** use the dedicated IDE `gradle_build` tool (e.g. `gradle_build(commandLine = "assembleDebug")`) or `gradle_sync` tool. This integrates directly with the IDE's build system and provides clean, structured output and error reporting.
*   **KMP Android Instrumented Test Task**: The typical Gradle task for running Android Instrumented Tests in a KMP application module is `:androidApp:connectedDebugAndroidTest`. Avoid `:androidApp:androidTestDebug` as it may not be found.
*   **KMP iOS Simulator Test Task**: The typical Gradle task for running iOS Simulator Tests in a KMP shared module is `:shared:iosSimulatorArm64Test`.

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

*   **Target Hygiene**: Only include platforms in `kotlin { ... }` blocks that are fully supported by your dependencies.
    *   **Rule**: **We ONLY target Android and iOS in this project.** Do NOT add `jvm()`, `desktop`, `js`, or `wasmJs` targets, as this will break dependency resolution for the whole project.

*   **Version Catalog**: Update `gradle/libs.versions.toml` frequently but verify compatibility between Kotlin, Compose Multiplatform, and AndroidX libraries. Use `./gradlew :shared:assemble` to quickly check dependency resolution.

#### Room KMP Database Initialization (CRITICAL & CORRECTED)
*   **Problem**: Encountering `kotlin.IllegalStateException: Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number.` or `Cannot create a RoomDatabase without providing a SQLiteDriver via setDriver()` on iOS.
*   **Solution**: For Room 2.7.0+ in KMP, you must rely on KSP to generate the constructor for non-Android platforms, but you configure the driver via standard factory functions. For *simple, additive* schema changes (like adding new columns with default values), `AutoMigration` is the preferred and simplest approach. For more complex migrations (e.g., column renames, type changes), manual `Migration` classes might still be necessary.
    *   **Rule**: When making any schema changes (adding/removing entities, fields, or changing types), you MUST increment the `version` number in the `@Database` annotation.
    *   **Manual Migrations (The KMP Way)**:
        *   **CRITICAL RULE**: Do NOT write manual migrations using the old Android `SupportSQLiteDatabase` in `androidMain`. This is incompatible with KMP's `BundledSQLiteDriver` and will crash the iOS build or fail at runtime.
        *   Proper manual KMP migrations must be written in `commonMain` using `androidx.sqlite.SQLiteConnection` (part of the new KMP SQLite driver APIs) so they execute on both Android and iOS.
    *   **Development Fallback (Destructive Migration)**:
        *   During active development when the schema changes rapidly and preserving data isn't necessary, the safest and easiest way to prevent `IllegalStateException` crashes is to use `.fallbackToDestructiveMigration(dropAllTables = true)`.
        *   **Implementation**: Add this line directly to the `RoomDatabase.Builder` inside your `getDatabaseBuilder()` functions in both `androidMain` and `iosMain`.
    *   **AutoMigration Process (For Production Additive Changes)**:
        1.  **Configure `AppDatabase.kt`**: Set `version = [NEW_VERSION]`, `exportSchema = true`, and add `autoMigrations = [AutoMigration(from = [OLD_VERSION], to = [NEW_VERSION])]`. Ensure `TypeConverters` are correctly applied.
        2.  **Configure `build.gradle.kts`**: Ensure the `room { schemaDirectory(...) }` block is present and that `ksp` arguments for `room.schemaLocation` are passed for *all* KSP targets (`kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64`). Example for `kspAndroid` (other KSP targets are similar):
            ```kotlin
            dependencies {
                add("kspAndroid", libs.room.compiler) {
                    arg("room.schemaLocation", "$projectDir/src/commonMain/room/schemas")
                }
                // ... other ksp targets
            }
            ```
        3.  **Generate `OLD_VERSION.json` Schema**: Temporarily set `version = [OLD_VERSION]` in `AppDatabase.kt` and remove the `autoMigrations` block. Run a clean build (`./gradlew clean :androidApp:assembleDebug`) to generate the schema file for the old version. Verify its existence.
        4.  **Re-apply `AutoMigration` and Increment Version**: Set `version = [NEW_VERSION]` in `AppDatabase.kt` and re-add `autoMigrations = [AutoMigration(from = [OLD_VERSION], to = [NEW_VERSION])]`. Run another clean build to validate `AutoMigration` and generate `NEW_VERSION.json`.

    *   **Platform Implementations**: **Do NOT write `actual object AppDatabaseConstructor`.** By suppressing the missing actual warning, we tell KSP to generate the platform-specific actual implementations automatically during the build step.
    *   **Platform Factories**: Provide standard factory functions (e.g., `fun getDatabase(context: Any? = null): AppDatabase` — notice NO `expect/actual` keyword here) in `androidMain` and `iosMain` that use `Room.databaseBuilder<AppDatabase>(...).setDriver(...).build()`. You should **remove any explicit `.addMigrations()` calls if `AutoMigration` is used for that specific version range, and remove `.fallbackToDestructiveMigration()` as it is no longer needed/recommended.**
        *   **iOS specific**: You must explicitly call `.setDriver(BundledSQLiteDriver())` on the builder.

#### Troubleshooting & IDE Caching
*   **Problem**: Not seeing all the code modules, or project files look broken.
*   **Solution**: **Ensure Android Studio project view is changed from "Android View" to "Project View"** using the dropdown in the top-left of the Project tool window. This is required to see all KMP directories like `shared` and `iosApp`.
*   **Problem**: Newly added resources (e.g., images in `composeResources`) are not found at runtime (causing `MissingResourceException`), or stubborn dependency/build issues persist despite correct configuration.
*   **Solution**: Android Studio and Gradle caches can get out of sync, especially in KMP projects. 
    1. Run a clean build (`./gradlew clean assembleDebug`).
    2. If that fails, go to **File -> Invalidate Caches... -> Check 'Clear file system cache and Local History' -> Invalidate and Restart**.

#### iOS Build & Xcode Troubleshooting (CRITICAL)
*   **Problem**: You get vague Kotlin iOS linkage or compilation errors (e.g., `linkDebugFrameworkIosSimulatorArm64` fails, `Cannot infer a bundle ID`, etc.) and running Gradle commands or grepping errors doesn't give a clear reason.
*   **Solution**: The issue is often native Xcode configuration, most commonly missing Provisioning Profiles or Team Signing. **Do not randomly execute gradle commands to diagnose iOS native errors.**
*   **Steps to Fix**:
    1.  Stop the agent execution and ask the User to open the project natively.
    2.  User Action: Right-click the `iosApp/iosApp` folder and select "Open in Finder" (or navigate via terminal).
    3.  User Action: Double-click `iosApp.xcodeproj` to open it in Xcode.
    4.  User Action: Select the `iosApp` target -> go to the **Signing & Capabilities** tab.
    5.  User Action: Configure the "Team" (select the Apple Developer team).
*   **Rule**: If iOS builds fail mysteriously, stop and explicitly ask the user to open Xcode to diagnose the real issue and fix signing.

#### iOS Deployment Troubleshooting
*   **Problem**: Deployment fails with the error `The developer disk image could not be mounted on this device.`
*   **Solution**: The run configuration is currently pointing to a physical iPhone that is either disconnected or locked. 
    *   **User Action**: Change the deployment target in Android Studio / Xcode to a running iOS Simulator, or ensure the physical device is plugged in and unlocked.
