package de.marmaro.krt.ffupdater.network

import android.content.Context
import android.os.Parcel
import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.FFUpdater.Companion.LOG_TAG
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.settings.ForegroundSettings
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Caches the latest known version of every app.
 *
 * This is kept both in memory (for the lifetime of the process) AND mirrored to disk
 * (SharedPreferences), because on Android the app's process gets killed in the background very often -
 * not just on reboot - so an in-memory-only cache was empty almost every time the app was reopened. That
 * forced a full round-trip to every app's update source (GitHub/GitLab/F-Droid/...) on basically every
 * "cold" open, which is what made the app list slow to load. Reading the small disk-backed cache back is
 * effectively instant compared to that, and correctness is unaffected since the same freshness
 * thresholds (configurable in Settings, ForegroundSettings.recentCacheDuration/offlineCacheDuration)
 * still apply to whatever is loaded, however it got there.
 */
@Keep
object LatestVersionCache {
    private const val PREFS_NAME = "latest_version_cache"
    private const val KEY_VERSION_PREFIX = "version_"
    private const val KEY_TIMESTAMP_PREFIX = "timestamp_"

    private val dataCache: ConcurrentHashMap<App, LatestVersion> = ConcurrentHashMap()
    private val cacheAge: ConcurrentHashMap<App, Long> = ConcurrentHashMap() // epoch millis

    @Volatile
    private var loadedFromDisk = false
    private val loadLock = Any()

    fun cache(context: Context, app: App, latestVersion: LatestVersion) {
        val now = System.currentTimeMillis()
        dataCache[app] = latestVersion
        cacheAge[app] = now
        persistToDisk(context, app, latestVersion, now)
    }

    fun getRecent(context: Context, app: App): LatestVersion? {
        return getCached(context, app, ForegroundSettings.recentCacheDuration)
    }

    fun getOld(context: Context, app: App): LatestVersion? {
        return getCached(context, app, ForegroundSettings.offlineCacheDuration)
    }

    fun clear(context: Context) {
        dataCache.clear()
        cacheAge.clear()
        prefs(context).edit().clear().apply()
    }

    private fun getCached(context: Context, app: App, threshold: Duration): LatestVersion? {
        loadFromDiskIfNecessary(context)
        val timestamp = cacheAge[app] ?: return null
        val cached = dataCache[app] ?: return null
        val age = Duration.ofMillis(System.currentTimeMillis() - timestamp)
        if (age > threshold) {
            return null
        }
        return cached
    }

    // The in-memory maps are the source of truth once populated; on the first access after process
    // start, seed them from disk so a cache filled by a previous (since-killed) process is reused.
    private fun loadFromDiskIfNecessary(context: Context) {
        if (loadedFromDisk) return
        synchronized(loadLock) {
            if (loadedFromDisk) return
            val preferences = prefs(context)
            for (app in App.values()) {
                val serialized = preferences.getString(KEY_VERSION_PREFIX + app.name, null) ?: continue
                val timestamp = preferences.getLong(KEY_TIMESTAMP_PREFIX + app.name, -1L)
                if (timestamp < 0) continue
                val latestVersion = deserialize(serialized) ?: continue
                dataCache[app] = latestVersion
                cacheAge[app] = timestamp
            }
            loadedFromDisk = true
        }
    }

    private fun persistToDisk(context: Context, app: App, latestVersion: LatestVersion, timestamp: Long) {
        try {
            prefs(context).edit()
                .putString(KEY_VERSION_PREFIX + app.name, serialize(latestVersion))
                .putLong(KEY_TIMESTAMP_PREFIX + app.name, timestamp)
                .apply()
        } catch (e: Exception) {
            // Persisting is a best-effort optimization for the next cold start; never let a failure here
            // affect the (already successful, in-memory) result of the current update check.
            Log.w(LOG_TAG, "LatestVersionCache: Failed to persist cache for ${app.name}.", e)
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun serialize(latestVersion: LatestVersion): String {
        val parcel = Parcel.obtain()
        try {
            parcel.writeParcelable(latestVersion, 0)
            return Base64.encodeToString(parcel.marshall(), Base64.NO_WRAP)
        } finally {
            parcel.recycle()
        }
    }

    private fun deserialize(data: String): LatestVersion? {
        val parcel = Parcel.obtain()
        return try {
            val bytes = Base64.decode(data, Base64.NO_WRAP)
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            @Suppress("DEPRECATION")
            parcel.readParcelable(LatestVersion::class.java.classLoader)
        } catch (e: Exception) {
            // Corrupt/incompatible cache entry (e.g. after an app update changed the Parcelable shape):
            // treat it as a cache miss rather than crashing.
            null
        } finally {
            parcel.recycle()
        }
    }
}
