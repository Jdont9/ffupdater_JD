package de.marmaro.krt.ffupdater.app

import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.app.impl.AppBase
import de.marmaro.krt.ffupdater.app.impl.Chromium
import de.marmaro.krt.ffupdater.app.impl.Cromite
import de.marmaro.krt.ffupdater.app.impl.DuckDuckGoAndroid
import de.marmaro.krt.ffupdater.app.impl.FFUpdater
import de.marmaro.krt.ffupdater.app.impl.FennecFdroid
import de.marmaro.krt.ffupdater.app.impl.FirefoxBeta
import de.marmaro.krt.ffupdater.app.impl.FirefoxKlar
import de.marmaro.krt.ffupdater.app.impl.FirefoxNightly
import de.marmaro.krt.ffupdater.app.impl.FirefoxRelease
import de.marmaro.krt.ffupdater.app.impl.Iceraven
import de.marmaro.krt.ffupdater.app.impl.Ironfox
import de.marmaro.krt.ffupdater.app.impl.Orbot
import de.marmaro.krt.ffupdater.app.impl.TorBrowser
import de.marmaro.krt.ffupdater.app.impl.TorBrowserAlpha
import de.marmaro.krt.ffupdater.app.impl.TrichromeLibrary
import de.marmaro.krt.ffupdater.app.impl.Vanadium
import de.marmaro.krt.ffupdater.app.impl.WebLibre

@Keep
enum class App {
    CHROMIUM,
    CROMITE,
    DUCKDUCKGO_ANDROID,
    FENNEC_FDROID,
    FFUPDATER,
    FIREFOX_BETA,
    FIREFOX_KLAR,
    FIREFOX_NIGHTLY,
    FIREFOX_RELEASE,
    ICERAVEN,
    IRONFOX,
    ORBOT,
    TOR_BROWSER,
    TOR_BROWSER_ALPHA,
    TRICHROME_LIBRARY, // must keep a lower ordinal than VANADIUM: installationChronology installs it first
    VANADIUM,
    WEBLIBRE;

    fun findImpl(): AppBase {
        return when (this) {
            CHROMIUM -> Chromium
            CROMITE -> Cromite
            DUCKDUCKGO_ANDROID -> DuckDuckGoAndroid
            FENNEC_FDROID -> FennecFdroid
            FFUPDATER -> FFUpdater
            FIREFOX_BETA -> FirefoxBeta
            FIREFOX_KLAR -> FirefoxKlar
            FIREFOX_NIGHTLY -> FirefoxNightly
            FIREFOX_RELEASE -> FirefoxRelease
            ICERAVEN -> Iceraven
            IRONFOX -> Ironfox
            ORBOT -> Orbot
            TOR_BROWSER -> TorBrowser
            TOR_BROWSER_ALPHA -> TorBrowserAlpha
            TRICHROME_LIBRARY -> TrichromeLibrary
            VANADIUM -> Vanadium
            WEBLIBRE -> WebLibre
        }
    }

    val installationChronology: Int
        get() {
            if (this == FFUPDATER) {
                return Int.MAX_VALUE
            }
            return this.ordinal
        }
}