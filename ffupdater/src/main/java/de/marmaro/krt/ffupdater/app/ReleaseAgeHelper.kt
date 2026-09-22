package de.marmaro.krt.ffupdater.app

import androidx.annotation.Keep
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import java.time.DateTimeException
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Flags apps whose latest known release is old enough that the user should consider switching away from
 * them, independently of the per-app static [de.marmaro.krt.ffupdater.app.impl.base.AppAttributes.eolReason]
 * (which is a permanent, manually curated "this app is dead" flag - see [docs/deprecated_browsers.md]).
 * This one is computed from the release date JDupdater already fetched, so it reacts automatically without
 * the maintainer having to notice and hardcode anything.
 */
@Keep
object ReleaseAgeHelper {
    /** Apps whose latest known release is at least this old are considered stale/potentially obsolete. */
    const val STALE_AFTER_DAYS = 90L

    /**
     * Age of the release in days, or null if it can't be determined: no publish date at all (e.g. Vanadium
     * and TrichromeLibrary, which are tracked by GrapheneOS branch, not by a dated release) or a date that
     * can't be parsed.
     */
    fun ageInDays(latestVersion: LatestVersion): Long? {
        val publishDate = latestVersion.publishDate ?: return null
        return try {
            val releaseDate = ZonedDateTime.parse(publishDate, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            Duration.between(releaseDate, ZonedDateTime.now()).toDays()
        } catch (e: DateTimeException) {
            null
        }
    }

    /** False when the age is unknown: an app is only flagged as stale on positive, known evidence. */
    fun isStale(latestVersion: LatestVersion): Boolean {
        return (ageInDays(latestVersion) ?: return false) >= STALE_AFTER_DAYS
    }
}
