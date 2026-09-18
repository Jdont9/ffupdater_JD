package de.marmaro.krt.ffupdater.settings

import android.app.Activity
import androidx.annotation.Keep

/**
 * Applies the user's chosen font (see FontOption / ForegroundSettings.fontOption) as a theme
 * overlay. Must be called on every Activity BEFORE setContentView(), otherwise the inflated
 * views won't pick up the overlay. When the option is FontOption.SYSTEM, this is a no-op and
 * the platform default font is used, exactly like before this feature existed.
 */
@Keep
object FontApplier {
    fun applyTo(activity: Activity) {
        ForegroundSettings.fontOption.themeOverlayRes?.let { overlay ->
            activity.theme.applyStyle(overlay, true)
        }
    }
}
