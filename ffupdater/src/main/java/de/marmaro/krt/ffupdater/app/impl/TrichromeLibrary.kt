package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory
import de.marmaro.krt.ffupdater.app.entity.InstallationStatus
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.app.entity.Version
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.gitlab.GitLabBranchConsumer
import de.marmaro.krt.ffupdater.settings.VanadiumSettings

/**
 * GrapheneOS Vanadium prebuilt: https://gitlab.com/grapheneos/platform_external_vanadium
 *
 * Vanadium is shipped as a Trichrome set: a shared library APK (this app) plus the browser APK
 * (see [Vanadium]). The library MUST be installed (or updated) BEFORE the browser APK, otherwise
 * installation of the browser fails. FFUpdater enforces this ordering during background updates via
 * [App.installationChronology] (this app's enum entry is declared before VANADIUM), but if you install
 * both manually/for the first time via "Add app", install this one first.
 *
 * [packageName] and [signatureHash] were confirmed from a real downloaded APK (v152.0.7977.54.0):
 * both TrichromeLibrary and Vanadium are signed with the same "CN=GrapheneOS" certificate.
 *
 * The GitLab branch to track (e.g. "17", "16-qpr2") is user-configurable, see [VanadiumSettings],
 * because GrapheneOS opens a new branch for every new Android/OS version and there is no reliable way
 * to auto-detect "the branch matching my device" from outside (branch names aren't strictly numeric,
 * e.g. "default" also exists). Update it in Settings > Vanadium when GrapheneOS moves to a new branch.
 *
 * !! Still TODO / not verified: [PATH_IN_REPO] - the directory layout under prebuilt/ has changed
 * between branches before (e.g. "arm64" on branch 16 vs "arm64-multilib" on branch 17). If downloads
 * start failing after changing the branch setting, check whether the path changed too by browsing
 * https://gitlab.com/grapheneos/platform_external_vanadium/-/tree/<branch>/prebuilt !!
 *
 * INSTALL-STATUS PIGGYBACKING: as a static shared library, this package can't be queried through the
 * public PackageManager API once installed (see [isStaticSharedLibrary] and CertificateVerifier.kt),
 * so the normal "is this app installed?" detection ([InstalledAppsCache]) would always report it as
 * NOT_INSTALLED and the background updater would never pick it up again after the first install. Since
 * [Vanadium] cannot run without a correctly-signed TrichromeLibrary already installed, [isInstalled] and
 * [getInstalledVersion] here simply delegate to Vanadium's (reliable) detection instead.
 *
 * NOT USER-VISIBLE / NOT SEPARATELY INSTALLABLE: [visibleToUser] and [installableByUser] are both false,
 * so this never appears on the main screen or in "Add app". Selecting or updating [Vanadium] transparently
 * installs/updates this library first - see the chaining logic in DownloadActivity (createIntent()).
 */
@Keep
object TrichromeLibrary : AppBase() {
    override val app = App.TRICHROME_LIBRARY
    override val packageName = "app.vanadium.trichromelibrary"
    override val title = R.string.trichrome_library__title
    override val description = R.string.trichrome_library__description
    override val installationWarning = R.string.trichrome_library__warning
    override val downloadSource = "GitLab"
    override val icon = R.drawable.ic_logo_trichrome_library
    override val minApiLevel = Build.VERSION_CODES.Q
    override val supportedAbis = listOf(ABI.ARM64_V8A)
    override val signatureHash = "c6adb8b83c6d4c17d292afde56fd488a51d316ff8f2c11c5410223bff8a7dbb3"
    override val projectPage = "https://gitlab.com/grapheneos/platform_external_vanadium"
    override val displayCategory = listOf(DisplayCategory.OTHER)
    override val hostnameForInternetCheck = "https://gitlab.com"
    override val isStaticSharedLibrary = true
    override val visibleToUser = false
    override val installableByUser = false

    private const val PROJECT_PATH = "grapheneos/platform_external_vanadium"
    private const val PATH_IN_REPO = "prebuilt/arm64-multilib/TrichromeLibrary.apk" // TODO verify per branch

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val branch = VanadiumSettings.androidBranch
        val commit = GitLabBranchConsumer.findLatestCommitOfBranch(PROJECT_PATH, branch)
        val downloadUrl = GitLabBranchConsumer.buildRawFileUrl(PROJECT_PATH, branch, PATH_IN_REPO)
        return LatestVersion(
            downloadUrl = downloadUrl,
            version = Version(commit.version),
            publishDate = commit.commitCreatedAt,
            exactFileSizeBytesOfDownload = null, // not exposed by the raw-file download, unlike GitHub/GitLab release assets
            fileHash = null,
        )
    }

    // See "INSTALL-STATUS PIGGYBACKING" in the class doc above.
    override suspend fun isInstalled(context: Context): InstallationStatus {
        return Vanadium.isInstalled(context)
    }

    override suspend fun isInstalledWithoutFingerprintVerification(packageManager: PackageManager): Boolean {
        return Vanadium.isInstalledWithoutFingerprintVerification(packageManager)
    }

    override suspend fun getInstalledVersion(packageManager: PackageManager): Version? {
        return Vanadium.getInstalledVersion(packageManager)
    }
}
