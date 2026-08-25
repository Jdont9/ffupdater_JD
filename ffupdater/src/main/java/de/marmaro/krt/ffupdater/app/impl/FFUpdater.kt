package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.OTHER
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.app.entity.Version
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.github.GithubConsumer

/**
 * Personal fork: points to Jdont9/ffupdater_JD instead of the upstream Tobi823/ffupdater, so
 * self-update checks use this fork's own GitHub Releases.
 * https://api.github.com/repos/Jdont9/ffupdater_JD/releases
 */
@Keep
object FFUpdater : AppBase() {
    override val app = App.FFUPDATER
    override val packageName = "de.marmaro.krt.ffupdater"
    override val title = R.string.app_name
    override val description = R.string.app_description
    override val downloadSource = "GitHub"
    override val icon = R.mipmap.ic_launcher
    override val minApiLevel = Build.VERSION_CODES.N
    override val supportedAbis = ALL_ABIS

    // Certificate SHA-256 digest of this fork's own signing key (CN=Julien), verified from the
    // signed release APK - not the original FFUpdater's certificate.
    override val signatureHash = "27c12c604f2b778b8fb2f1e6aefa81f1625a80d87a0fd553c3375e541e5804c3"
    override val installableByUser = false
    override val projectPage = "https://github.com/Jdont9/ffupdater_JD"
    override val displayCategory = listOf(OTHER)
    override val differentSignatureMessage = R.string.ffupdater__different_signature_message
    override val hostnameForInternetCheck = "https://api.github.com"

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val result = GithubConsumer.findLatestRelease(
            repository = GithubConsumer.GithubRepo("Jdont9", "ffupdater_JD", 0),
            isValidRelease = { !it.isPreRelease },
            isSuitableAsset = { it.name == "JDupdater-release.apk" },
            requireReleaseDescription = false,
        )
        return LatestVersion(
            downloadUrl = result.url,
            // Git tags are prefixed with "v" (e.g. "v83.0.0") but versionName in build.gradle isn't
            // ("83.0.0"). Strip the prefix so the version comparison matches correctly, otherwise
            // JDupdater always thinks an update is available even when it's already up to date.
            version = Version(result.tagName.removePrefix("v")),
            publishDate = result.releaseDate,
            exactFileSizeBytesOfDownload = result.fileSizeBytes,
            fileHash = null,
        )
    }
}