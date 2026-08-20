package de.marmaro.krt.ffupdater.network.gitlab

import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.network.exceptions.InvalidApiResponseException
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.file.FileDownloader

/**
 * Some GitLab projects (e.g. GrapheneOS/platform_external_vanadium) don't publish "Releases" with
 * downloadable assets. Instead, prebuilt APKs are committed directly into the repository on a branch
 * (one branch per Android/OS version, e.g. "17", "16-qpr2", ...) under a fixed path.
 *
 * This consumer resolves the HEAD commit of such a branch, which is used:
 * - to build the direct "raw file" download URL for a given file path on that branch
 * - to derive a comparable "version" for FFUpdater, either from the commit title (GrapheneOS commits
 *   their prebuilt updates with a title like "version 140.0.7339.51.0") or, as a fallback, from the
 *   commit's short SHA + date.
 *
 * @see <a href="https://docs.gitlab.com/api/branches/">GitLab Branches API</a>
 */
@Keep
object GitLabBranchConsumer {

    private val VERSION_IN_COMMIT_TITLE = Regex("""version\s+([0-9]+(?:\.[0-9]+)+)""", RegexOption.IGNORE_CASE)

    @MainThread
    @Throws(NetworkException::class)
    suspend fun findLatestCommitOfBranch(projectPath: String, branch: String): Result {
        val encodedProject = java.net.URLEncoder.encode(projectPath, "UTF-8")
        val url = "https://gitlab.com/api/v4/projects/$encodedProject/repository/branches/$branch"
        val json = FileDownloader.downloadAsJsonObject(url)

        val commit = json.getAsJsonObject("commit")
            ?: throw InvalidApiResponseException("GitLab branch response for '$branch' has no 'commit' object.")

        val shortId = commit.get("short_id")?.asString
            ?: throw InvalidApiResponseException("GitLab commit response has no 'short_id'.")
        val createdAt = commit.get("created_at")?.asString ?: ""
        val title = commit.get("title")?.asString ?: ""

        val version = VERSION_IN_COMMIT_TITLE.find(title)?.groupValues?.get(1)
            ?: "0.0.0.0-$shortId" // fallback: no parseable version in the commit title

        return Result(
            branch = branch,
            commitShortId = shortId,
            commitCreatedAt = createdAt,
            version = version,
        )
    }

    /**
     * Builds the direct, unauthenticated "raw file" download URL for a file inside a public GitLab
     * repository. This works the same way as clicking "Download" on a blob page in the GitLab UI and
     * does not require going through the API (so no need to worry about API rate limiting for the
     * download itself, only for the branch/version lookup above).
     */
    fun buildRawFileUrl(projectPath: String, branch: String, pathInRepo: String): String {
        return "https://gitlab.com/$projectPath/-/raw/$branch/$pathInRepo?inline=false"
    }

    @Keep
    data class Result(
        val branch: String,
        val commitShortId: String,
        val commitCreatedAt: String,
        val version: String,
    )
}
