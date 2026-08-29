package de.marmaro.krt.ffupdater.settings

import android.content.SharedPreferences
import androidx.annotation.Keep

/**
 * GrapheneOS opens a new branch on https://gitlab.com/grapheneos/platform_external_vanadium for every
 * new Android/OS release (e.g. "17", "16-qpr2", ...). There is no reliable way to auto-detect "the right
 * branch" from outside the repository (branch names aren't strictly numeric/orderable, e.g. "default"
 * also exists), so it's exposed as a user setting instead of a hardcoded constant.
 *
 * The directory layout under prebuilt/ has also changed between branches before (e.g. "arm64" on branch
 * 16 vs "arm64-multilib" on branch 17), independently of the branch number itself, so it's exposed as
 * its own setting too rather than being tied to the branch.
 */
@Keep
object VanadiumSettings {
    private lateinit var preferences: SharedPreferences

    /**
     * This function must be called from Application.onCreate() or this singleton can't be used
     */
    fun init(sharedPreferences: SharedPreferences) {
        preferences = sharedPreferences
    }

    const val DEFAULT_BRANCH = "17"
    const val DEFAULT_PREBUILT_PATH = "arm64-multilib"

    val androidBranch: String
        get() {
            val value = preferences.getString("vanadium__android_branch", DEFAULT_BRANCH)
            return value?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_BRANCH
        }

    val prebuiltPath: String
        get() {
            val value = preferences.getString("vanadium__prebuilt_path", DEFAULT_PREBUILT_PATH)
            return value?.trim()?.trim('/')?.takeIf { it.isNotEmpty() } ?: DEFAULT_PREBUILT_PATH
        }
}
