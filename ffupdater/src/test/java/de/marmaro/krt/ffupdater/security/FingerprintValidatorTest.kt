package de.marmaro.krt.ffupdater.security

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_SIGNATURES
import android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
import android.content.pm.Signature
import android.content.pm.SigningInfo
import de.marmaro.krt.ffupdater.BaseTest
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.security.cert.CertificateException
import kotlin.io.path.createTempFile
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class FingerprintValidatorTest : BaseTest() {

    @MockK
    lateinit var packageManager: PackageManager

    @MockK
    lateinit var signature: Signature

    @MockK
    lateinit var secondSignature: Signature

    @MockK
    lateinit var signingInfo: SigningInfo

    companion object {
        lateinit var signatureBytes: ByteArray
        lateinit var secondSignatureBytes: ByteArray

        // SHA-256 of src/test/resources/.../SecondAppSignature.bin (self-signed test certificate, not a real app)
        const val secondSignatureFingerprint = "1e9e2a995b9e3a2eec8a2b557d9b122c2a4e831d645a2e6d83b5999ba36c8233"

        @Suppress("SpellCheckingInspection")
        const val signatureFingerprint = "a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04"
        val file = createTempFile("FIREFOX_RELEASE__2021_04_30__244384144", ".apk").toFile()!!

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            val path = "src/test/resources/de/marmaro/krt/ffupdater/security/FirefoxReleaseAppSignature.bin"
            signatureBytes = File(path).readBytes()
            secondSignatureBytes =
                File("src/test/resources/de/marmaro/krt/ffupdater/security/SecondAppSignature.bin").readBytes()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            file.delete()
        }
    }

    @Test
    fun checkApkFile_withCorrectSignature_returnValid() {
        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.signatures = arrayOf(signature)
        every { signature.toByteArray() } returns signatureBytes
        every {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(file.absolutePath, GET_SIGNATURES)
        } returns packageInfo

        val actual =
            runBlocking { FingerprintValidator.checkApkFile(packageManager, file, App.FIREFOX_RELEASE.findImpl()) }
        assertTrue(actual.isValid)
        assertEquals(signatureFingerprint, actual.hexString)
        assertEquals(signatureFingerprint, App.FIREFOX_RELEASE.findImpl().signatureHash)
    }

    @Test
    fun checkApkFile_withWrongApp_returnInvalid() {
        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.signatures = arrayOf(signature)
        every { signature.toByteArray() } returns signatureBytes
        every {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(file.absolutePath, GET_SIGNATURES)
        } returns packageInfo

        val actual = runBlocking { FingerprintValidator.checkApkFile(packageManager, file, App.WEBLIBRE.findImpl()) }
        assertFalse(actual.isValid)
    }

    @Test
    fun checkApkFile_withIncorrectSignature_throwException() {
        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.signatures = arrayOf(signature)
        every { signature.toByteArray() } returns Random.nextBytes(938)
        every {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(file.absolutePath, GET_SIGNATURES)
        } returns packageInfo

        assertThrows(CertificateException::class.java) {
            runBlocking {
                FingerprintValidator.checkApkFile(packageManager, file, App.FIREFOX_RELEASE.findImpl())
            }
        }
    }

    @Test
    fun checkInstalledApp_withFirefoxRelease_returnValid() {
        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.signatures = arrayOf(signature)
        every { signature.toByteArray() } returns signatureBytes
        every {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(App.FIREFOX_RELEASE.findImpl().packageName, GET_SIGNATURES)
        } returns packageInfo
        val actual = runBlocking {
            FingerprintValidator.checkInstalledApp(packageManager, App.FIREFOX_RELEASE.findImpl())
        }
        assertTrue(actual.isValid)
        assertEquals(signatureFingerprint, actual.hexString)
        assertEquals(signatureFingerprint, App.FIREFOX_RELEASE.findImpl().signatureHash)
    }

    @Test
    fun checkInstalledApp_withInvalidCertificate_throwException() {
        val packageInfo = PackageInfo()
        @Suppress("DEPRECATION")
        packageInfo.signatures = arrayOf(signature)
        every { signature.toByteArray() } returns Random.nextBytes(938)
        every {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(App.FIREFOX_RELEASE.findImpl().packageName, GET_SIGNATURES)
        } returns packageInfo

        assertThrows(CertificateException::class.java) {
            runBlocking {
                FingerprintValidator.checkInstalledApp(packageManager, App.FIREFOX_RELEASE.findImpl())
            }
        }
    }

    /**
     * Simulates Android 9+ where the package manager returns the signing certificate history.
     * Caller must call [unmockkObject] for [DeviceSdkTester] afterward.
     */
    private fun mockSigningCertificateHistory(vararg history: Signature) {
        mockkObject(DeviceSdkTester)
        every { DeviceSdkTester.supportsAndroid13T33() } returns false
        every { DeviceSdkTester.supportsAndroid9P28() } returns true
        every { signature.toByteArray() } returns signatureBytes
        every { secondSignature.toByteArray() } returns secondSignatureBytes
        every { signingInfo.hasMultipleSigners() } returns false
        every { signingInfo.signingCertificateHistory } returns arrayOf(*history)
        val packageInfo = PackageInfo()
        packageInfo.signingInfo = signingInfo
        every {
            @Suppress("DEPRECATION")
            packageManager.getPackageArchiveInfo(file.absolutePath, GET_SIGNING_CERTIFICATES)
        } returns packageInfo
    }

    @Test
    fun checkApkFile_withRotatedKeyAndStoredCertificateInLineage_returnValid() {
        try {
            // the developer rotated the key: the stored (old) certificate is the first, the new one the last entry
            mockSigningCertificateHistory(signature, secondSignature)
            val actual = runBlocking {
                FingerprintValidator.checkApkFile(packageManager, file, App.FIREFOX_RELEASE.findImpl())
            }
            assertTrue(actual.isValid)
            assertEquals(signatureFingerprint, actual.hexString)
        } finally {
            unmockkObject(DeviceSdkTester)
        }
    }

    @Test
    fun checkApkFile_withSingleUnknownCertificate_returnInvalid() {
        try {
            mockSigningCertificateHistory(secondSignature)
            val actual = runBlocking {
                FingerprintValidator.checkApkFile(packageManager, file, App.FIREFOX_RELEASE.findImpl())
            }
            assertFalse(actual.isValid)
            assertEquals(secondSignatureFingerprint, actual.hexString)
        } finally {
            unmockkObject(DeviceSdkTester)
        }
    }

    @Test
    fun checkApkFile_withMultipleSigners_throwException() {
        try {
            mockSigningCertificateHistory(signature)
            every { signingInfo.hasMultipleSigners() } returns true
            assertThrows(IllegalStateException::class.java) {
                runBlocking {
                    FingerprintValidator.checkApkFile(packageManager, file, App.FIREFOX_RELEASE.findImpl())
                }
            }
        } finally {
            unmockkObject(DeviceSdkTester)
        }
    }
}