package org.example.project.domain

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

actual fun resolveVideoPath(path: String): String {
    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(path)) return path
    
    val fileName = path.substringAfterLast("/")
    
    // Check if the original path was in the tmp directory or Documents
    val isTmp = path.contains("/tmp/")
    
    if (isTmp) {
        val tempDir = NSTemporaryDirectory()
        val tempPath = "$tempDir$fileName"
        return tempPath // Return the valid path for the current container even if it doesn't exist yet!
    } else {
        val documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        val currentDocsDir = documentPaths.firstOrNull() as? String
        if (currentDocsDir != null) {
            val docsPath = "$currentDocsDir/$fileName"
            return docsPath // Return the valid path for the current container even if it doesn't exist yet!
        }
    }
    
    // Fallback
    return path
}
