package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.compose_multiplatform
import kotlinproject.shared.generated.resources.film_noir

@Composable
@Preview
fun App() {
    // Film Noir Color Palette
    val NoirColors = androidx.compose.material3.darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFFE0E0E0), // Light grey for primary elements
        onPrimary = androidx.compose.ui.graphics.Color(0xFF121212), // Dark grey/black text on primary
        primaryContainer = androidx.compose.ui.graphics.Color(0xFF303030), // Slightly lighter dark for containers
        onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        secondary = androidx.compose.ui.graphics.Color(0xFFBDBDBD), // Medium grey for secondary
        onSecondary = androidx.compose.ui.graphics.Color(0xFF000000),
        secondaryContainer = androidx.compose.ui.graphics.Color(0xFF424242),
        onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        background = androidx.compose.ui.graphics.Color(0xFF000000), // Pure black background (fallbacks)
        onBackground = androidx.compose.ui.graphics.Color(0xFFE0E0E0), // Light text on background
        surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E), // Dark grey for cards/surfaces
        onSurface = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    )

    MaterialTheme(
        colorScheme = NoirColors
    ) {
        var showContent by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(Res.drawable.film_noir),
                contentDescription = "Film Noir Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.7f // Dim the background slightly to ensure text is readable
            )
            
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { showContent = !showContent }) {
                    Text("Click me!")
                }
                AnimatedVisibility(showContent) {
                    val greeting = remember { Greeting().greet() }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                        Text("Compose: ${greeting}")
                    }
                }
            }
        }
    }
}