package de.marmaro.krt.ffupdater.app.impl.base

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.entity.LatestVersion

@Keep
interface VersionDisplay : InstalledVersionFetcher {
    @AnyThread
    suspend fun getDisplayInstalledVersion(context: Context): String {
        val version = getInstalledVersion(context.packageManager) ?: return ""
        // Note: version.buildDateTime (only set for Firefox Nightly, to tell apart two builds that share
        // the same version text) is intentionally not shown here. Its raw ISO-8601 form (e.g.
        // "2026-09-22T21:13:42") was unreadable and made the card wrap onto extra lines; the relative age
        // already shown next to the available version covers "how fresh is this" for the user.
        return context.getString(R.string.installed_version, version.versionText)
    }

    @AnyThread
    fun getDisplayAvailableVersion(context: Context, availableVersionResult: LatestVersion): String {
        val version = availableVersionResult.version
        return context.getString(R.string.available_version, version.versionText)
    }

}