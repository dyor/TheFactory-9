package org.example.project.domain

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

actual fun resolveVideoPath(path: String): String {
    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(path)) return path
    
    // If the path doesn't exist (likely because the app's UUID changed after a rebuild), 
    // extract the filename and search the current container's common directories.
    val fileName = path.substringAfterLast("/")
    
    val documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val currentDocsDir = documentPaths.firstOrNull() as? String
    if (currentDocsDir != null) {
        val docsPath = "$currentDocsDir/$fileName"
        if (fileManager.fileExistsAtPath(docsPath)) {
            return docsPath
        }
    }
    
    val tempDir = NSTemporaryDirectory()
    val tempPath = "$tempDir$fileName"
    if (fileManager.fileExistsAtPath(tempPath)) {
        return tempPath
    }
    
    // Fallback if not found, let it fail with the original path
    return path
}
