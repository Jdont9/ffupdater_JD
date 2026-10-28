package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.MY_OTHER_APPS
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.app.entity.Version
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.github.GithubConsumer

/**
 * https://github.com/Jdont9/Asclepios
 * https://api.github.com/repos/Jdont9/Asclepios/releases
 *
 * One of the maintainer's own apps (Asclépios AV), shown in the "My other applications" section.
 * Releases are tagged "vX.Y.Z" (the installed versionName is "X.Y.Z") and carry an APK named "asclepios-av_vX.Y.apk".
 */
@Keep
object Asclepios : AppBase() {
    override val app = App.ASCLEPIOS
    override val packageName = "fr.jd.asclepios"
    override val title = R.string.asclepios__title
    override val description = R.string.asclepios__description
    override val downloadSource = "GitHub"
    override val icon = R.drawable.ic_logo_asclepios
    override val minApiLevel = Build.VERSION_CODES.O // Android 8.0 (API 26)
    override val supportedAbis = ALL_ABIS

    // SHA-256 of the signing certificate (CN=Julien D), taken from the signature of the installed v4.3.1 APK
    override val signatureHash = "6dd976dd3e77affeabcf033bb6ac3e07b85331d1ec601ff02778caef73eec1b6"
    override val projectPage = "https://github.com/Jdont9/Asclepios"
    override val displayCategory = listOf(MY_OTHER_APPS)
    override val hostnameForInternetCheck = "https://api.github.com"

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val result = GithubConsumer.findLatestRelease(
            repository = GithubConsumer.GithubRepo("Jdont9", "Asclepios", 0),
            isValidRelease = { !it.isPreRelease },
            isSuitableAsset = { it.nameStartsAndEndsWith("asclepios-av", ".apk") },
            requireReleaseDescription = false,
        )
        return LatestVersion(
            downloadUrl = result.url,
            // Git tags may be prefixed with "v" (e.g. "v4.3.1") but the installed versionName is "4.3.1".
            version = Version(result.tagName.removePrefix("v")),
            publishDate = result.releaseDate,
            exactFileSizeBytesOfDownload = result.fileSizeBytes,
            fileHash = null,
        )
    }
}
