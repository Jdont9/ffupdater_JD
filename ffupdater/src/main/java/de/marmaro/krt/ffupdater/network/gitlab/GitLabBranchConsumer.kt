package de.marmaro.krt.ffupdater.network.gitlab

import androidx.annotation.Keep
import androidx.annotation.MainThread
import com.google.gson.JsonArray
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
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

    /**
     * If [pathInRepo] is set, only commits that touched this file are considered. This matters because
     * the HEAD of the branch is often a commit that doesn't update the prebuilt APKs at all (config,
     * docs, ...), and its title then contains no "version X" (which produced a bogus "0.0.0.0-<sha>"
     * version and a permanent false "update available").
     */
    @MainThread
    @Throws(NetworkException::class)
    suspend fun findLatestCommitOfBranch(projectPath: String, branch: String, pathInRepo: String? = null): Result {
        if (pathInRepo != null) {
            return findLatestCommitTouchingPath(projectPath, branch, pathInRepo)
        }
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

    @MainThread
    @Throws(NetworkException::class)
    private suspend fun findLatestCommitTouchingPath(projectPath: String, branch: String, pathInRepo: String): Result {
        val encodedProject = java.net.URLEncoder.encode(projectPath, "UTF-8")
        val encodedBranch = java.net.URLEncoder.encode(branch, "UTF-8")
        val encodedPath = java.net.URLEncoder.encode(pathInRepo, "UTF-8")
        val url = "https://gitlab.com/api/v4/projects/$encodedProject/repository/commits" +
                "?ref_name=$encodedBranch&path=$encodedPath&per_page=20"
        val commits: JsonArray = try {
            JsonParser.parseString(FileDownloader.downloadString(url)).asJsonArray
        } catch (e: JsonParseException) {
            throw InvalidApiResponseException("GitLab commits response for '$branch' is not valid JSON.")
        } catch (e: IllegalStateException) {
            throw InvalidApiResponseException("GitLab commits response for '$branch' is not a JSON array.")
        }
        if (commits.size() == 0) {
            throw InvalidApiResponseException("GitLab has no commit for '$pathInRepo' on branch '$branch'.")
        }

        // newest first: take the first commit whose title contains a version
        for (element in commits) {
            val commit = element.asJsonObject
            val title = commit.get("title")?.asString ?: ""
            val version = VERSION_IN_COMMIT_TITLE.find(title)?.groupValues?.get(1) ?: continue
            return Result(
                branch = branch,
                commitShortId = commit.get("short_id")?.asString ?: "",
                commitCreatedAt = commit.get("created_at")?.asString ?: "",
                version = version,
            )
        }

        // none of the last commits has a parseable title: fall back to the newest commit for this file
        val newest = commits[0].asJsonObject
        val shortId = newest.get("short_id")?.asString
            ?: throw InvalidApiResponseException("GitLab commit response has no 'short_id'.")
        return Result(
            branch = branch,
            commitShortId = shortId,
            commitCreatedAt = newest.get("created_at")?.asString ?: "",
            version = "0.0.0.0-$shortId",
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
