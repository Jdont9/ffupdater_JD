package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.GOOD_PRIVACY_BROWSER
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.app.entity.Version
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.github.GithubConsumer

/**
 * https://github.com/FaFre/WebLibre
 * https://api.github.com/repos/FaFre/WebLibre/releases
 *
 * A privacy-focused Android browser built from scratch on Mozilla's Gecko engine (not a Firefox fork).
 *
 * Includes alpha releases (as requested), not just stable ones: `isValidRelease = { true }` accepts
 * both, and `irrelevantReleasesBetweenRelevant = 1` forces the paginated /releases API instead of
 * /releases/latest, since GitHub's "latest" endpoint never returns a release marked as prerelease
 * (which alpha builds are) - without this, alphas would silently never be picked up.
 *
 * !! Not verified: the WebLibre project mentions stable and alpha can be installed "side by side" as
 * separate apps, which suggests the alpha build might use a different applicationId/package name and/or
 * signing key than the stable build. This entry assumes both channels share the same package name
 * (eu.weblibre.gecko) and certificate. If installing an alpha update ever fails with a signature or
 * package-name mismatch, that's the likely cause - the fix would be to detect the channel from the
 * asset/tag name and adjust packageName/signatureHash accordingly, or split into two separate App
 * entries (stable vs alpha) similar to how Tor Browser / Tor Browser Alpha are separate here. !!
 */
@Keep
object WebLibre : AppBase() {
    override val app = App.WEBLIBRE
    override val packageName = "eu.weblibre.gecko"
    override val title = R.string.weblibre__title
    override val description = R.string.weblibre__description
    override val installationWarning = R.string.weblibre__warning
    override val downloadSource = "GitHub"
    override val icon = R.drawable.ic_logo_weblibre
    override val minApiLevel = Build.VERSION_CODES.O
    override val supportedAbis = listOf(ABI.ARM64_V8A)
    override val signatureHash = "8f526e1e53d6bd4dfbf4f4b93c2a91ecb5cb8da5e14ad94c2570e1e3c713527f"
    override val projectPage = "https://github.com/FaFre/WebLibre"
    override val displayCategory = listOf(GOOD_PRIVACY_BROWSER)
    override val hostnameForInternetCheck = "https://api.github.com"

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val result = GithubConsumer.findLatestRelease(
            repository = GithubConsumer.GithubRepo("FaFre", "WebLibre", 1),
            isValidRelease = { true }, // include alpha (prerelease) builds too
            isSuitableAsset = { it.name.endsWith("arm64-v8a-release.apk") },
            requireReleaseDescription = false,
        )
        return LatestVersion(
            downloadUrl = result.url,
            version = Version(result.tagName.removePrefix("v")),
            publishDate = result.releaseDate,
            exactFileSizeBytesOfDownload = result.fileSizeBytes,
            fileHash = null,
        )
    }
}
