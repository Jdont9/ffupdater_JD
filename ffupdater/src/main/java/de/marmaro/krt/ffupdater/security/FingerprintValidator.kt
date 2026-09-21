package de.marmaro.krt.ffupdater.security

import android.content.pm.PackageManager
import android.content.pm.Signature
import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.app.impl.AppBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory

/**
 * Validation of downloaded and installed application.
 */
@Keep
object FingerprintValidator {

    /**
     * Validate the SHA256 fingerprint of the certificate of the downloaded application as APK file.
     * Takes about 1s -> blocking UI thread is not ok -> suspend
     *
     * @param file APK file
     * @param app  app
     * @return the fingerprint of the app and if it matched with the stored fingerprint
     */
    suspend fun checkApkFile(packageManager: PackageManager, file: File, app: AppBase): FingerprintValidatorResult {
        return withContext(Dispatchers.Default) {
            val signatures = PackageManagerUtil.getPackageArchiveSignatures(packageManager, file.absolutePath)
            verifySignatures(signatures, app)
        }
    }

    /**
     * Validate the SHA256 fingerprint of the certificate of the installed application.
     * Takes about 1ms -> blocking UI thread is ok because thread switching is expensive
     *
     * @param app app
     * @return the fingerprint of the app and if it matched with the stored fingerprint
     * @see [Example on how to generate the certificate fingerprint](https://stackoverflow.com/a/22506133)
     *
     * @see [Another example](https://gist.github.com/scottyab/b849701972d57cf9562e)
     */
    suspend fun checkInstalledApp(packageManager: PackageManager, app: AppBase): FingerprintValidatorResult {
        val signatures = PackageManagerUtil.getInstalledAppSignatures(packageManager, app)
        return verifySignatures(signatures, app)
    }

    /**
     * The app is valid if the stored (pinned) certificate is the signing certificate or one of the older
     * certificates in the signing certificate lineage. The lineage is cryptographically verified by Android:
     * a new signing key can only be part of it if the owner of the old key authorized it (key rotation).
     * Therefore, a developer who rotated the signing key does not break the updates.
     *
     * @param signatures certificate history: oldest first, the current signing certificate is the last entry
     * @return the stored fingerprint if it is valid, otherwise the fingerprint of the current certificate
     */
    @Throws(CertificateException::class, NoSuchAlgorithmException::class)
    private suspend fun verifySignatures(signatures: List<Signature>, appDetail: AppBase): FingerprintValidatorResult {
        val fingerprints = signatures.map { getFingerprintOfSignature(it) }
        if (appDetail.signatureHash in fingerprints) {
            return FingerprintValidatorResult(isValid = true, hexString = appDetail.signatureHash)
        }
        return FingerprintValidatorResult(isValid = false, hexString = fingerprints.last())
    }

    private suspend fun getFingerprintOfSignature(signature: Signature): String {
        return withContext(Dispatchers.Default) {
            val certificate = signature.toByteArray().inputStream().buffered().use { stream ->
                CertificateFactory.getInstance("X509").generateCertificate(stream)
            }
            val fingerprint = MessageDigest.getInstance("SHA-256").digest(certificate.encoded)
            fingerprint.joinToString("") {
                String.format("%02x", (it.toInt() and 0xFF))
            }
        }
    }
}