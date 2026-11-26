package com.mpi.klass

/**
 * Runtime secrets/config populated on Android from Secrets-backed BuildConfig fields
 * in MainActivity.onCreate(). Keeps `.env` / GitHub Actions Secrets cleanly isolated in `app`
 * while letting `shared` Android implementations read them safely without hardcoded credentials.
 */
object KlassRuntimeConfig {
    var geminiApiKey: String = ""
    var recaptchaSiteKey: String = ""
    var firestoreDatabaseId: String = ""
    var appUpdateUrl: String = ""
    var installedVersionName: String = ""
    var onManualCheckForUpdate: (() -> Unit)? = null
}

object KlassUpdateUtils {

    private val githubRepoOrReleasesRegex = Regex(
        """^https?://github\.com/([^/]+)/([^/?#]+?)(?:\.git)?(?:/releases(?:/latest)?)?/?$""",
        RegexOption.IGNORE_CASE,
    )
    private val githubReleaseTagRegex = Regex(
        """^https?://github\.com/([^/]+)/([^/]+)/releases/tag/([^/?#]+)/?$""",
        RegexOption.IGNORE_CASE,
    )
    private val githubBlobRegex = Regex(
        """^https?://github\.com/([^/]+)/([^/]+)/blob/(.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private val driveFilePathRegex = Regex(
        """^https?://drive\.google\.com/file/d/([^/?#]+).*""",
        RegexOption.IGNORE_CASE,
    )
    private val driveIdQueryRegex = Regex(
        """^https?://drive\.google\.com/(?:open|uc)\?.*[?&]?id=([^&#]+).*""",
        RegexOption.IGNORE_CASE,
    )

    fun normalizeUpdateFeedUrl(rawUrl: String): String {
        val clean = rawUrl.trim().trim('"').trim()
        if (clean.isEmpty()) return ""

        githubReleaseTagRegex.matchEntire(clean)?.let { m ->
            val owner = m.groupValues[1]
            val repo = m.groupValues[2]
            val tag = m.groupValues[3]
            return "https://api.github.com/repos/$owner/$repo/releases/tags/$tag"
        }

        githubRepoOrReleasesRegex.matchEntire(clean)?.let { m ->
            val owner = m.groupValues[1]
            val repo = m.groupValues[2]
            return "https://api.github.com/repos/$owner/$repo/releases/latest"
        }

        githubBlobRegex.matchEntire(clean)?.let { m ->
            val owner = m.groupValues[1]
            val repo = m.groupValues[2]
            val rest = m.groupValues[3]
            return "https://raw.githubusercontent.com/$owner/$repo/$rest"
        }

        return normalizeApkDownloadUrl(clean)
    }

    fun normalizeApkDownloadUrl(rawUrl: String): String {
        val clean = rawUrl.trim().trim('"').trim()
        if (clean.isEmpty()) return ""

        driveFilePathRegex.matchEntire(clean)?.let { m ->
            val fileId = m.groupValues[1]
            return "https://drive.usercontent.google.com/download?id=$fileId&export=download&confirm=t"
        }

        driveIdQueryRegex.matchEntire(clean)?.let { m ->
            val fileId = m.groupValues[1]
            return "https://drive.usercontent.google.com/download?id=$fileId&export=download&confirm=t"
        }

        if (clean.contains("dropbox.com", ignoreCase = true) && clean.contains("dl=0")) {
            return clean.replace("dl=0", "dl=1")
        }

        return clean
    }

    fun compareSemanticVersions(remoteTag: String, installedVersion: String): Int {
        fun numbers(value: String): List<Long> =
            Regex("""\d+""").findAll(value).mapNotNull { it.value.toLongOrNull() }.toList()
        val remote = numbers(remoteTag)
        val installed = numbers(installedVersion)
        for (index in 0 until maxOf(remote.size, installed.size)) {
            val comparison = (remote.getOrElse(index) { 0L }).compareTo(installed.getOrElse(index) { 0L })
            if (comparison != 0) return comparison
        }
        return 0
    }
}
