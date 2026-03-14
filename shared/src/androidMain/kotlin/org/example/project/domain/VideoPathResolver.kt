package org.example.project.domain

actual fun resolveVideoPath(path: String): String {
    // On Android, absolute file paths within the app directory remain stable across rebuilds.
    return path
}
