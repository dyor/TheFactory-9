package org.example.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.shared.BuildConfig

class GeminiClient {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }
    }

    suspend fun generateScript(prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        
        // Add a strong system instruction to ensure it outputs a YouTube Short script specifically constrained to 60 seconds
        val fullPrompt = "You are an expert YouTube Shorts scriptwriter. " +
            "Write a script for a 60-second YouTube Short based on the following prompt. " +
            "Include visual cues in brackets, e.g., [Visual: User pointing at screen].\n\nPrompt: $prompt"

        val requestBody = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = fullPrompt)
                    )
                )
            )
        )

        return try {
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            
            if (response.error != null) {
                "Error: ${response.error.message}"
            } else {
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to generate script."
            }
        } catch (e: Exception) {
            "Network Error: ${e.message}"
        }
    }
}
