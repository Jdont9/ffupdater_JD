package de.marmaro.krt.ffupdater.app

import androidx.annotation.Keep
import io.github.g00fy2.versioncompare.Version

@Keep
object VersionCompareHelper {
    fun isAvailableVersionHigher(installedVersion: String, availableVersion: String): Boolean {
        return try {
            val installed = convertToVersion(installedVersion)
            val available = convertToVersion(availableVersion)
            available > installed || hasVersionSchemaChanged(installed, available)
        } catch (e: IllegalArgumentException) {
            installedVersion != availableVersion
        }
    }

    /**
     * Detects a change of the version schema, e.g. Tor Browser switched from 128.x.x to 14.x.x. In this case
     * the available version looks older, but it is the newer one.
     *
     * A big drop of the major version is NOT enough: e.g. a user tracking an older branch (installed 153.x,
     * available 140.x) must not be offered a downgrade. Therefore, the drop must be from a 3-digit major version
     * (Firefox/Chromium style numbers) to a 1-2 digit one.
     */
    private fun hasVersionSchemaChanged(installed: Version, available: Version): Boolean {
        return installed.major >= 100 && available.major < 100 && (installed.major - available.major) > 10
    }

    fun isAvailableVersionEqual(installedVersion: String, availableVersion: String): Boolean {
        return try {
            val installed = convertToVersion(installedVersion)
            val available = convertToVersion(availableVersion)
            available == installed
        } catch (e: IllegalArgumentException) {
            installedVersion == availableVersion
        }
    }

    private fun convertToVersion(rawVersion: String): Version {
        val cleanedUpVersion = rawVersion.replace(Regex("[a-zA-Z-]"), ".")
        return Version(cleanedUpVersion, true)
    }

}