package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.film_noir
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*

// Navigation 3 imports

import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
// Removed: import kotlinx.serialization.modules.subclass
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.rememberPermissionState

import org.example.project.data.local.AppDatabase
import org.example.project.ui.WritersRoomScreen
import org.example.project.ui.WritersRoomViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Serializable // Make Screen keys serializable for Navigation 3
sealed class Screen : NavKey {
    @Serializable
    data object Home : Screen() { const val TITLE = "Home" } // Renamed title to TITLE
    @Serializable
    data object WritersRoom : Screen() { const val TITLE = "Writers Room" } // Renamed title to TITLE
    @Serializable
    data object RecordingStudio : Screen() { const val TITLE = "Recording Studio" } // Renamed title to TITLE
    @Serializable
    data object EditingStudio : Screen() { const val TITLE = "Editing Studio" } // Renamed title to TITLE
    @Serializable
    data object PublishingStudio : Screen() { const val TITLE = "Publishing Studio" } // Renamed title to TITLE
    @Serializable
    data object Archives : Screen() { const val TITLE = "Archives" } // Renamed title to TITLE
}

@Composable
fun App(database: AppDatabase? = null) {
    // Film Noir Color Palette
    val noirColors = MaterialTheme.colorScheme.copy(
        primary = Color(0xFFE0E0E0), // Light grey for primary elements
        onPrimary = Color(0xFF121212), // Dark grey/black text on primary
        primaryContainer = Color(0xFF303030), // Slightly lighter dark for containers
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFBDBDBD), // Medium grey for secondary
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF424242),
        onSecondaryContainer = Color(0xFFFFFFFF),
        background = Color(0xFF000000), // Pure black background (fallbacks)
        onBackground = Color(0xFFE0E0E0), // Light text on background
        surface = Color(0xFF1E1E1E), // Dark grey for cards/surfaces
        onSurface = Color(0xFFE0E0E0),
    )

    MaterialTheme(
        colorScheme = noirColors,
    ) {
        val config = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Home::class, Screen.Home.serializer())
                    subclass(Screen.WritersRoom::class, Screen.WritersRoom.serializer())
                    subclass(Screen.RecordingStudio::class, Screen.RecordingStudio.serializer())
                    subclass(Screen.EditingStudio::class, Screen.EditingStudio.serializer())
                    subclass(Screen.PublishingStudio::class, Screen.PublishingStudio.serializer())
                    subclass(Screen.Archives::class, Screen.Archives.serializer())
                }
            }
        }
        val backStack = rememberNavBackStack(config, Screen.Home)
        val onBack = { if (backStack.size > 1) backStack.removeLast() }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(Res.drawable.film_noir),
                contentDescription = "Film Noir Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.7f // Dim the background slightly to ensure text is readable
            )

            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                entryProvider = entryProvider {
                    entry<Screen.Home> {
                        HomeScreen(
                            onNavigateToWritersRoom = { backStack.add(Screen.WritersRoom) },
                            onNavigateToRecordingStudio = { backStack.add(Screen.RecordingStudio) },
                            onNavigateToEditingStudio = { backStack.add(Screen.EditingStudio) },
                            onNavigateToPublishingStudio = { backStack.add(Screen.PublishingStudio) },
                            onNavigateToArchives = { backStack.add(Screen.Archives) }
                        )
                    }
                    entry<Screen.WritersRoom> { destination ->
                        if (database != null) {
                            val viewModel = viewModel { WritersRoomViewModel(database.scriptDao()) }
                            WritersRoomScreen(
                                viewModel = viewModel,
                                onBack = onBack,
                            )
                        } else {
                            DetailScreen(
                                screenName = destination.TITLE + " (No DB)",
                                onBack = onBack,
                            )
                        }
                    }
                    entry<Screen.RecordingStudio> {
                        RecordingStudioScreen(
                            onBack = onBack,
                        )
                    }
                    entry<Screen.EditingStudio> { destination ->
                        DetailScreen(
                            screenName = destination.TITLE,
                            onBack = onBack,
                        )
                    }
                    entry<Screen.PublishingStudio> { destination ->
                        DetailScreen(
                            screenName = destination.TITLE,
                            onBack = onBack,
                        )
                    }
                    entry<Screen.Archives> { destination ->
                        DetailScreen(
                            screenName = destination.TITLE,
                            onBack = onBack,
                        )
                    }
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToWritersRoom: () -> Unit,
    onNavigateToRecordingStudio: () -> Unit,
    onNavigateToEditingStudio: () -> Unit,
    onNavigateToPublishingStudio: () -> Unit,
    onNavigateToArchives: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Welcome to Factory-9", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onNavigateToWritersRoom, modifier = Modifier.fillMaxWidth()) { Text("Writers Room") }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateToRecordingStudio, modifier = Modifier.fillMaxWidth()) { Text("Recording Studio") }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateToEditingStudio, modifier = Modifier.fillMaxWidth()) { Text("Editing Studio") }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateToPublishingStudio, modifier = Modifier.fillMaxWidth()) { Text("Publishing Studio") }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateToArchives, modifier = Modifier.fillMaxWidth()) { Text("Archives") }
            }
        }
    }
}

@Composable
fun DetailScreen(screenName: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("You are in the $screenName", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onBack) { Text("Go Home") }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordingStudioScreen(onBack: () -> Unit) {
    val cameraPermissionState = rememberPermissionState(Permission.Camera)
    val micPermissionState = rememberPermissionState(Permission.RecordAudio)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
        micPermissionState.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Recording Studio", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Text("Camera Permission: ${cameraPermissionState.status}", textAlign = TextAlign.Center)
                Text("Mic Permission: ${micPermissionState.status}", textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onBack) { Text("Go Home") }
            }
        }
    }
}
