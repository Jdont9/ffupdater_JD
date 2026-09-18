package de.marmaro.krt.ffupdater.settings

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import de.marmaro.krt.ffupdater.R

/**
 * Available fonts for the "foreground__font_preference" setting.
 * SYSTEM has no theme overlay - it just leaves the platform default font untouched.
 * To add a new font: drop the .ttf/.otf into res/font/, add a themeOverlayRes style in
 * styles.xml (see font_overlay__monte_carlo), add an entry here, and add it to
 * arrays.xml's foreground__font_preference__entries/__values.
 */
@Keep
enum class FontOption(
    val preferenceValue: String,
    @StringRes val displayNameRes: Int,
    @StyleRes val themeOverlayRes: Int?,
) {
    SYSTEM("system", R.string.settings__foreground__font_preference__system, null),
    MONTE_CARLO(
        "monte_carlo",
        R.string.settings__foreground__font_preference__monte_carlo,
        R.style.font_overlay__monte_carlo,
    ),
    ;

    companion object {
        fun fromPreferenceValue(value: String?): FontOption {
            return entries.firstOrNull { it.preferenceValue == value } ?: SYSTEM
        }
    }
}
