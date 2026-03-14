package org.example.project.domain

/**
 * Resolves a video path to handle platform-specific storage changes (e.g. iOS UUID changes on rebuild).
 */
expect fun resolveVideoPath(path: String): String
