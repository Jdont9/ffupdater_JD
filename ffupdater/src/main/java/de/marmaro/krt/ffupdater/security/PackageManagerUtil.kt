package de.marmaro.krt.ffupdater.security

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_SIGNATURES
import android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import de.marmaro.krt.ffupdater.app.impl.AppBase
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

@Keep
object PackageManagerUtil {

    /**
     * Returns the certificates of the APK file: the certificate history (oldest first, the current signing
     * certificate is the last entry). The list contains more than one entry when the developer rotated the
     * signing key (APK Signature Scheme v3) and the APK proves this rotation with a signing certificate lineage.
     */
    @Suppress("DEPRECATION")
    @MainThread
    @Throws(FileNotFoundException::class, IllegalStateException::class)
    suspend fun getPackageArchiveSignatures(pm: PackageManager, path: String): List<Signature> {
        return withContext(Dispatchers.Default) {
            val file = File(path)
            check(file.exists()) { "File '$path' does not exists." }
            val signatures = mutableListOf<() -> List<Signature>?>()
            if (DeviceSdkTester.supportsAndroid13T33()) {
                signatures.add { extractSignatures(pm.getPackageArchiveInfo(path, getPackageInfoFlags())) }
            }
            if (DeviceSdkTester.supportsAndroid9P28()) {
                signatures.add { extractSignatures(pm.getPackageArchiveInfo(path, GET_SIGNING_CERTIFICATES)) }
            }
            signatures.add { extractSignatures(pm.getPackageArchiveInfo(path, GET_SIGNATURES)) }
            signatures.firstNotNullOf { it() }
        }
    }

    /**
     * Same as [getPackageArchiveSignatures], but for the installed app.
     */
    @Suppress("DEPRECATION")
    @SuppressLint("PackageManagerGetSignatures")
    suspend fun getInstalledAppSignatures(pm: PackageManager, app: AppBase): List<Signature> {
        return withContext(Dispatchers.Default) {
            try {
                val signatures = mutableListOf<() -> List<Signature>?>()
                if (DeviceSdkTester.supportsAndroid13T33()) {
                    signatures.add { extractSignatures(pm.getPackageInfo(app.packageName, getPackageInfoFlags())) }
                }
                if (DeviceSdkTester.supportsAndroid9P28()) {
                    signatures.add { extractSignatures(pm.getPackageInfo(app.packageName, GET_SIGNING_CERTIFICATES)) }
                }
                signatures.add { extractSignatures(pm.getPackageInfo(app.packageName, GET_SIGNATURES)) }
                signatures.firstNotNullOf { it() }
            } catch (e: PackageManager.NameNotFoundException) {
                throw RuntimeException("app.packageName is not whitelisted in AndroidManifest.xml", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getPackageInfoFlags(): PackageManager.PackageInfoFlags {
        return PackageManager.PackageInfoFlags.of(GET_SIGNING_CERTIFICATES.toLong())
    }

    @Suppress("DEPRECATION")
    private fun extractSignatures(packageInfo: PackageInfo?): List<Signature>? {
        if (DeviceSdkTester.supportsAndroid9P28()) {
            packageInfo?.signingInfo?.let {
                return extractSignatures(it)
            }
        }

        packageInfo?.signatures?.let {
            return extractSignatures(it)
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Throws(IllegalStateException::class)
    private fun extractSignatures(signingInfo: SigningInfo): List<Signature> {
        check(!signingInfo.hasMultipleSigners()) { "Multiple signers are not allowed." }
        // The history is NOT a list of multiple signers: it contains the current signing certificate and (after a
        // key rotation) the older certificates from which the current one proved to be the legitimate successor.
        val history = signingInfo.signingCertificateHistory
        check(history != null && history.isNotEmpty()) { "Signatures must not be empty." }
        return history.map { checkNotNull(it) }
    }

    /**
     * Android before 9 (API 28) has no signing certificate lineage, so more than one signature means
     * that multiple signers signed the APK. This is not supported.
     */
    @Throws(IllegalStateException::class)
    private fun extractSignatures(signatures: Array<Signature>): List<Signature> {
        check(signatures.isNotEmpty()) { "Signatures must not be empty." }
        check(signatures.size == 1) { "Found multiple signatures." }
        return listOf(signatures[0])
    }

    suspend fun getInstalledAppVersionName(pm: PackageManager, packageName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                pm.getPackageInfo(packageName, 0)?.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    suspend fun isAppInstalled(pm: PackageManager, packageName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                pm.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}
