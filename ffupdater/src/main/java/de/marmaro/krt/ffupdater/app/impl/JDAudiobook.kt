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
 * https://github.com/Jdont9/JDAudiobook
 * https://api.github.com/repos/Jdont9/JDAudiobook/releases
 *
 * One of the maintainer's own apps, shown in the "My other applications" section. Releases are tagged
 * "vX.Y.Z" and carry an APK named "JDAudiobook-X.Y.Z.apk" (plus other assets, e.g. checksums).
 */
@Keep
object JDAudiobook : AppBase() {
    override val app = App.JD_AUDIOBOOK
    override val packageName = "fr.jd.audiobooks"
    override val title = R.string.jd_audiobook__title
    override val description = R.string.jd_audiobook__description
    override val downloadSource = "GitHub"
    override val icon = R.drawable.ic_logo_jd_audiobook
    override val minApiLevel = Build.VERSION_CODES.O // Android 8.0 (API 26)
    override val supportedAbis = ALL_ABIS // pure Kotlin/Compose app, no native libraries

    // SHA-256 of the signing certificate (CN=Julien D), taken from the signature of the installed v1.5.2 APK
    override val signatureHash = "6dd976dd3e77affeabcf033bb6ac3e07b85331d1ec601ff02778caef73eec1b6"
    override val projectPage = "https://github.com/Jdont9/JDAudiobook"
    override val displayCategory = listOf(MY_OTHER_APPS)
    override val hostnameForInternetCheck = "https://api.github.com"

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val result = GithubConsumer.findLatestRelease(
            repository = GithubConsumer.GithubRepo("Jdont9", "JDAudiobook", 0),
            isValidRelease = { !it.isPreRelease },
            isSuitableAsset = { it.nameStartsAndEndsWith("JDAudiobook-", ".apk") },
            requireReleaseDescription = false,
        )
        return LatestVersion(
            downloadUrl = result.url,
            // Git tags are prefixed with "v" (e.g. "v1.5.2") but the installed versionName is "1.5.2".
            version = Version(result.tagName.removePrefix("v")),
            publishDate = result.releaseDate,
            exactFileSizeBytesOfDownload = result.fileSizeBytes,
            fileHash = null,
        )
    }
}
