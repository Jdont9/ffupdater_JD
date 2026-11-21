package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.BETTER_THAN_GOOGLE_CHROME
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.app.entity.Version
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.device.DeviceAbiExtractor
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.github.GithubConsumer
import de.marmaro.krt.ffupdater.settings.DeviceSettingsHelper

/**
 * https://github.com/ungoogled-software/ungoogled-chromium-android
 * https://api.github.com/repos/ungoogled-software/ungoogled-chromium-android/releases
 *
 * Releases carry the APKs "ChromePublic_arm64.apk" and "ChromePublic_arm.apk" (browser only, Android 10+,
 * no x86/x64 build). The git tag looks like "156.0.8078.79-1" (Chromium version + ungoogled revision).
 */
@Keep
object UngoogledChromium : AppBase() {
    override val app = App.UNGOOGLED_CHROMIUM
    override val packageName = "org.ungoogled.chromium.stable"
    override val title = R.string.ungoogled_chromium__title
    override val description = R.string.ungoogled_chromium__description
    override val installationWarning = R.string.ungoogled_chromium__warning
    override val downloadSource = "GitHub"
    override val icon = R.drawable.ic_logo_chromium
    override val minApiLevel = Build.VERSION_CODES.Q // Android 10 (API 29)
    override val supportedAbis = listOf(ABI.ARM64_V8A, ABI.ARMEABI_V7A)

    // SHA-256 of the signing certificate (O=ungoogled-software)
    override val signatureHash = "08b8740845f4354ddca77e5b31fe40dbd69645575b77deef2344d064c61d2f87"
    override val projectPage = "https://github.com/ungoogled-software/ungoogled-chromium-android"
    override val displayCategory = listOf(BETTER_THAN_GOOGLE_CHROME)
    override val hostnameForInternetCheck = "https://api.github.com"

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val fileName = findFileName()
        val result = GithubConsumer.findLatestRelease(
            repository = GithubConsumer.GithubRepo("ungoogled-software", "ungoogled-chromium-android", 0),
            isValidRelease = { !it.isPreRelease },
            isSuitableAsset = { it.name == fileName },
            requireReleaseDescription = false,
        )
        // tag example: "156.0.8078.79-1" or "v156.0.8078.79-1" -> versionName "156.0.8078.79"
        val version = result.tagName.removePrefix("v").split("-")[0]
        return LatestVersion(
            downloadUrl = result.url,
            version = Version(version),
            publishDate = result.releaseDate,
            exactFileSizeBytesOfDownload = result.fileSizeBytes,
            fileHash = null,
        )
    }

    private fun findFileName(): String {
        return when (DeviceAbiExtractor.findBestAbi(supportedAbis, DeviceSettingsHelper.prefer32BitApks)) {
            ABI.ARM64_V8A -> "ChromePublic_arm64.apk"
            ABI.ARMEABI_V7A -> "ChromePublic_arm.apk"
            else -> throw IllegalArgumentException("ABI is not supported")
        }
    }
}
