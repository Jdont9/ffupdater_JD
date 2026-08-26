package de.marmaro.krt.ffupdater.app.impl.base

import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory
import de.marmaro.krt.ffupdater.device.ABI

@Keep
interface AppAttributes {
    val app: App
    val packageName: String
    val title: Int
    val description: Int
    val installationWarning: Int?
    val downloadSource: String
    val icon: Int
    val minApiLevel: Int
    val supportedAbis: List<ABI>
    val signatureHash: String
    val installableByUser: Boolean
    val projectPage: String
    val eolReason: Int?
    val displayCategory: List<DisplayCategory>
    val fileNameInZipArchive: String?
    val differentSignatureMessage: Int
    val hostnameForInternetCheck: String
    val isStaticSharedLibrary: Boolean
    // If false, this app is never shown to the user (main screen, "Add app"), but is still tracked
    // normally (background updates, install-status detection). Used for apps that only exist as an
    // internal prerequisite of another app, e.g. TrichromeLibrary for Vanadium.
    val visibleToUser: Boolean

    fun isEol() = (eolReason != null)
}