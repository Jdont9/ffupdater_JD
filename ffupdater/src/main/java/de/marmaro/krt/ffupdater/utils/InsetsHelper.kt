package de.marmaro.krt.ffupdater.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

/**
 * Keeps the view clear of the system bars (edge-to-edge) by adding the size of the bars to the ORIGINAL margins
 * of the view.
 *
 * The original margins are read only once. The listener is called multiple times (e.g. when the keyboard or a
 * dialog appears), so adding the insets to the current margins would make the margins grow with every call.
 */
fun View.applySystemBarInsetsAsMargins(top: Boolean, bottom: Boolean) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams
    val initialTopMargin = params?.topMargin ?: 0
    val initialBottomMargin = params?.bottomMargin ?: 0
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = initialTopMargin + if (top) bars.top else 0
            bottomMargin = initialBottomMargin + if (bottom) bars.bottom else 0
        }
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
