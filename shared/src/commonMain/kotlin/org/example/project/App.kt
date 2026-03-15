package org.example.project

import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.painterResource

import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.film_noir
import org.example.project.data.local.AppDatabase
import org.example.project.domain.model.Script
import org.example.project.domain.model.ScriptStage
import org.example.project.ui.RecordingStudioScreen
import org.example.project.ui.EditingStudioScreen
import org.example.project.ui.EditingStudioViewModel
import org.example.project.ui.ArchivesScreen
import org.example.project.ui.ArchivesViewModel
import org.example.project.domain.rememberVideoPublisher
import org.example.project.ui.PublishingStudioScreen
import org.example.project.ui.PublishingStudioViewModel
import org.example.project.ui.RecordingStudioViewModel
import org.example.project.ui.WritersRoomScreen
import org.example.project.ui.WritersRoomViewModel


@Composable
fun HomeScreen(
    activeScript: Script?,
    onNavigateToWritersRoom: () -> Unit,
    onNavigateToRecordingStudio: () -> Unit,
    onNavigateToEditingStudio: () -> Unit,
    onNavigateToPublishingStudio: () -> Unit,
    onNavigateToArchives: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(16.dp).offset(y = 64.dp), // Position in the sky
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
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

                when (activeScript?.currentStage) {
                    ScriptStage.WRITERS_ROOM -> {
                        Button(onClick = onNavigateToWritersRoom, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Continue Writing") }
                    }
                    ScriptStage.RECORDING_STUDIO -> {
                        Button(onClick = onNavigateToRecordingStudio, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Go to Recording Studio") }
                    }
                    ScriptStage.EDITING_STUDIO -> {
                        Button(onClick = onNavigateToEditingStudio, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Go to Editing Studio") }
                    }
                    ScriptStage.PUBLISHING_STUDIO -> {
                        Button(onClick = onNavigateToPublishingStudio, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Go to Publishing Studio") }
                    }
                    else -> {
                        Button(onClick = onNavigateToWritersRoom, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Start New Script") }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToArchives, 
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
                ) { Text("Archives") }
            }
        }
    }
}

@Composable
fun DetailScreen(screenName: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(16.dp).offset(y = 64.dp), // Position in the sky
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("You are in the ${screenName}", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onBack) { Text("Go Home") }
            }
        }
    }
}


@Serializable // Make Screen keys serializable for Navigation 3
sealed class Screen : NavKey {
    @Serializable
    data object Splash : Screen() { const val TITLE = "Splash" }
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
        secondaryContainer = Color(0xFF242424), // Much darker grey, almost black
        onSecondaryContainer = Color(0xFFE0E0E0),
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
                    subclass(Screen.Splash::class, Screen.Splash.serializer())
                    subclass(Screen.Home::class, Screen.Home.serializer())
                    subclass(Screen.WritersRoom::class, Screen.WritersRoom.serializer())
                    subclass(Screen.RecordingStudio::class, Screen.RecordingStudio.serializer())
                    subclass(Screen.EditingStudio::class, Screen.EditingStudio.serializer())
                    subclass(Screen.PublishingStudio::class, Screen.PublishingStudio.serializer())
                    subclass(Screen.Archives::class, Screen.Archives.serializer())
                }
            }
        }
        val backStack = rememberNavBackStack(config, Screen.Splash)
        val onBack = { if (backStack.size > 1) backStack.removeLast() }

        LaunchedEffect(Unit) {
            delay(2000) // Show splash screen for 2 seconds
            backStack.add(Screen.Home)
            backStack.remove(Screen.Splash)
        }

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

            val activeScript by database?.scriptDao()?.getActiveScript()?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }
            
            // "Semi-truck" heavy bounce effect, now even slower for large cards
            val bounceSpec = spring<IntOffset>(
                dampingRatio = 0.7f,
                stiffness = 30f // Even lower stiffness for a much slower drop/pull
            )
            
            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                transitionSpec = {
                    val enter = slideInVertically(
                        animationSpec = bounceSpec,
                        initialOffsetY = { fullHeight -> -fullHeight }
                    )
                    val exit = slideOutVertically(
                        animationSpec = bounceSpec,
                        targetOffsetY = { fullHeight -> -fullHeight }
                    )
                    enter togetherWith exit
                },
                popTransitionSpec = {
                    val enter = slideInVertically(
                        animationSpec = bounceSpec,
                        initialOffsetY = { fullHeight -> -fullHeight }
                    )
                    val exit = slideOutVertically(
                        animationSpec = bounceSpec,
                        targetOffsetY = { fullHeight -> -fullHeight }
                    )
                    enter togetherWith exit
                },
                entryProvider = entryProvider {
                    entry<Screen.Splash> {
                        // Empty box so the background shows through entirely, creating the splash look
                        Box(Modifier.fillMaxSize())
                    }
                    entry<Screen.Home> {
                                HomeScreen(
                                    activeScript = activeScript,
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
                                        onNavigateToRecordingStudio = { _ ->
                                            backStack.add(Screen.RecordingStudio)
                                        }
                                    )
                                } else {
                                    DetailScreen(
                                        screenName = destination.TITLE + " (No DB)",
                                        onBack = onBack,
                                    )
                                }
                            }
                            entry<Screen.RecordingStudio> {
                                if (database != null) {
                                    val viewModel = viewModel { RecordingStudioViewModel(database.scriptDao()) }
                                    RecordingStudioScreen(
                                        viewModel = viewModel,
                                        onBack = onBack,
                                        onHome = {
                                            backStack.removeAll { true }
                                            backStack.add(Screen.Home)
                                        },
                                        onNavigateToEditingStudio = { backStack.add(Screen.EditingStudio) }
                                    )
                                } else {
                                    DetailScreen(
                                        screenName = Screen.RecordingStudio.TITLE + " (No DB)",
                                        onBack = onBack,
                                    )
                                }
                            }
                            entry<Screen.EditingStudio> {
                                if (database != null) {
                                    val viewModel = viewModel { EditingStudioViewModel(database.scriptDao()) }
                                    EditingStudioScreen(
                                        viewModel = viewModel,
                                        onBack = {
                                            backStack.removeLast()
                                            if (backStack.last() != Screen.RecordingStudio) {
                                                backStack.add(Screen.RecordingStudio)
                                            }
                                        },
                                        onHome = {
                                            backStack.removeAll { true }
                                            backStack.add(Screen.Home)
                                        },
                                        onNavigateToPublishingStudio = { backStack.add(Screen.PublishingStudio) }
                                    )
                                } else {
                                    DetailScreen(
                                        screenName = Screen.EditingStudio.TITLE + " (No DB)",
                                        onBack = onBack,
                                    )
                                }
                            }
                            entry<Screen.PublishingStudio> { destination ->
                                if (database != null) {
                                    val videoPublisher = rememberVideoPublisher()
                                    val viewModel = viewModel { PublishingStudioViewModel(database.scriptDao(), videoPublisher) }
                                    PublishingStudioScreen(
                                        viewModel = viewModel,
                                        onBack = {
                                            backStack.removeLast()
                                            if (backStack.last() != Screen.EditingStudio) {
                                                backStack.add(Screen.EditingStudio)
                                            }
                                        },
                                        onHome = {
                                            backStack.removeAll { true }
                                            backStack.add(Screen.Home)
                                        },
                                        onFinish = {
                                            backStack.removeAll { true }
                                            backStack.add(Screen.Home)
                                        }
                                    )
                                } else {
                                    DetailScreen(
                                        screenName = destination.TITLE + " (No DB)",
                                        onBack = onBack,
                                    )
                                }
                            }
                            entry<Screen.Archives> {
                                if (database != null) {
                                    val viewModel = viewModel { ArchivesViewModel(database.scriptDao()) }
                                    ArchivesScreen(
                                        viewModel = viewModel,
                                        onBack = onBack
                                    )
                                } else {
                                    DetailScreen(
                                        screenName = Screen.Archives.TITLE + " (No DB)",
                                        onBack = onBack,
                                    )
                                }
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
