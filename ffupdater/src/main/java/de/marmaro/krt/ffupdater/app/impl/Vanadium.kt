package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.app.entity.Version
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.gitlab.GitLabBranchConsumer
import de.marmaro.krt.ffupdater.settings.VanadiumSettings

/**
 * GrapheneOS Vanadium prebuilt: https://gitlab.com/grapheneos/platform_external_vanadium
 *
 * IMPORTANT: this app depends on [TrichromeLibrary] being installed first (same certificate family,
 * shared native code). Trying to install this APK before the library APK will make installation fail.
 * FFUpdater enforces the ordering during background updates via [App.installationChronology]. If you
 * install manually for the first time, install TrichromeLibrary first.
 *
 * [packageName] and [signatureHash] were confirmed from a real downloaded APK (v152.0.7977.54.0):
 * both Vanadium and TrichromeLibrary are signed with the same "CN=GrapheneOS" certificate.
 *
 * The GitLab branch tracked (e.g. "17", "16-qpr2") is user-configurable in Settings > Vanadium, see
 * [VanadiumSettings] - GrapheneOS opens a new branch for every new Android/OS version, and there's no
 * reliable way to auto-detect the right one from outside the repo.
 *
 * Also note the GrapheneOS project's own caveat: Vanadium is built and hardened against GrapheneOS's
 * own kernel/allocator/system patches. Running it standalone on a non-GrapheneOS Android build is
 * possible but loses part of its hardening; Cromite is a reasonable alternative on stock/other ROMs.
 *
 * !! Still verify per branch: [VanadiumSettings.prebuiltPath] (see Settings > Vanadium) - the directory
 * layout under prebuilt/ has changed between branches before (e.g. "arm64" on branch 16 vs
 * "arm64-multilib" on branch 17). Update it in Settings when it changes. !!
 */
@Keep
object Vanadium : AppBase() {
    override val app = App.VANADIUM
    override val packageName = "app.vanadium.browser"
    override val title = R.string.vanadium__title
    override val description = R.string.vanadium__description
    override val installationWarning = R.string.vanadium__warning
    override val downloadSource = "GitLab"
    override val icon = R.drawable.ic_logo_vanadium
    override val minApiLevel = Build.VERSION_CODES.Q
    override val supportedAbis = listOf(ABI.ARM64_V8A)
    override val signatureHash = "c6adb8b83c6d4c17d292afde56fd488a51d316ff8f2c11c5410223bff8a7dbb3"
    override val projectPage = "https://gitlab.com/grapheneos/platform_external_vanadium"
    override val displayCategory = listOf(DisplayCategory.GOOD_SECURITY_BROWSER)
    override val hostnameForInternetCheck = "https://gitlab.com"

    private const val PROJECT_PATH = "grapheneos/platform_external_vanadium"

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val branch = VanadiumSettings.androidBranch
        val pathInRepo = "prebuilt/${VanadiumSettings.prebuiltPath}/TrichromeChrome.apk"
        val commit = GitLabBranchConsumer.findLatestCommitOfBranch(PROJECT_PATH, branch)
        val downloadUrl = GitLabBranchConsumer.buildRawFileUrl(PROJECT_PATH, branch, pathInRepo)
        return LatestVersion(
            downloadUrl = downloadUrl,
            version = Version(commit.version),
            publishDate = commit.commitCreatedAt,
            exactFileSizeBytesOfDownload = null,
            fileHash = null,
        )
    }
}
