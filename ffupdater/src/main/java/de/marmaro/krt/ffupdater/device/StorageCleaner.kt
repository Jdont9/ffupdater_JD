package de.marmaro.krt.ffupdater.device

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.FFUpdater.Companion.LOG_TAG
import de.marmaro.krt.ffupdater.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Keep
object StorageCleaner {
    suspend fun deleteApksOfNotInstalledApps(context: Context) {
        withContext(Dispatchers.IO) {
            val installedApps = InstalledAppsCache.getInstalledAppsWithCorrectSignature(context.applicationContext)
            App.values() //
                .filter { it !in installedApps } //
                .forEach { it.findImpl().deleteFileCache(context.applicationContext) }
            Log.i(LOG_TAG, "StorageCleaner: Deleted possible cached files of not installed apps")
        }
    }

    // Temp files are named "<UUID>", "<UUID>.apk", "<UUID>.zip", "<UUID>.part" (or "<UUID>apk", "<UUID>zip" in older versions).
    private val TEMP_FILE_REGEX = Regex(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(\\.?(apk|zip|part))?$"
    )
    private const val MAX_TEMP_FILE_AGE_MS = 60 * 60 * 1000L

    /**
     * If the process is killed during a download, the "finally" blocks never run and the partial
     * file stays in the download folder forever. Remove such orphaned files (older than one hour,
     * so a download running right now is never touched).
     */
    suspend fun deleteOrphanedTempFiles(context: Context) {
        withContext(Dispatchers.IO) {
            val folder = context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val now = System.currentTimeMillis()
            folder?.listFiles()
                ?.filter { it.isFile && TEMP_FILE_REGEX.matches(it.name) }
                ?.filter { now - it.lastModified() > MAX_TEMP_FILE_AGE_MS }
                ?.forEach {
                    Log.i(LOG_TAG, "StorageCleaner: Delete orphaned temp file ${it.absolutePath}")
                    it.delete()
                }
        }
    }
}
