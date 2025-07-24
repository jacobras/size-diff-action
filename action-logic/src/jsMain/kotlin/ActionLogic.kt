import actions.cache.restoreCache
import actions.cache.saveCache
import actions.core.getInput
import actions.core.setOutput
import actions.github.getOctokit
import js.objects.unsafeJso
import js.promise.await
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import node.buffer.BufferEncoding
import node.fs.StatSyncFnSimpleOptions
import node.fs.readFileSync
import node.fs.statSync
import node.fs.writeFileSync
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
object ActionLogic {

    @OptIn(DelicateCoroutinesApi::class)
    @Suppress("unused")
    fun run(): Promise<Unit> = GlobalScope.promise {
        val path = getPath()
        val mainBranchRef = getMainBranchRef()
        val currentBranch = getCurrentBranchName()
        val isMainBranch = currentBranch == mainBranchRef

        val newSizeBytes = getFileSizeBytes(path)

        if (isMainBranch) {
            cacheNewFileSize(newSizeBytes)
            "Size stored ($newSizeBytes bytes). Diff will happen when this is run on a non-main branch."
        } else {
            val existingSizeBytes = readExistingSizeFromCache()
            val largeFiles = findLargeFiles()
            val summary = SummaryBuilder.buildDiff(
                path = path,
                existingSizeBytes = existingSizeBytes,
                newSizeBytes = newSizeBytes,
                largeFiles = largeFiles
            )
            setOutput("summary", summary)
        }
    }

    private suspend fun getPath(): String {
        val pathInput = getInput("path")
        val globber = actions.glob.create(pathInput)
        return globber.glob().await().first()
    }

    private fun getMainBranchRef(): String {
        val mainBranchName = getInput("main-branch-name")
        return "refs/heads/$mainBranchName"
    }

    private fun getCurrentBranchName(): String {
        return js("process.env.GITHUB_REF ") as String
    }

    private suspend fun readExistingSizeFromCache(): Long {
        restoreCache(
            paths = arrayOf(CACHE_FILENAME),
            primaryKey = CACHE_KEY
        )
        val existing = try {
            readFileSync(CACHE_FILENAME, BufferEncoding.utf8).toLong()
        } catch (_: dynamic) {
            -1L
        }
        return existing
    }

    private suspend fun cacheNewFileSize(fileSize: Long) {
        writeFileSync(CACHE_FILENAME, fileSize.toString())

        val runId = js("process.env.GITHUB_RUN_ID") as? String
        println("GitHub Actions run ID: $runId")
        println("New file size: $fileSize bytes")

        saveCache(
            paths = arrayOf(CACHE_FILENAME),
            key = "$CACHE_KEY-$runId",
            options = buildObject(),
            enableCrossOsArchive = true
        )
    }

    private fun getFileSizeBytes(path: String): Long {
        val options = buildObject<StatSyncFnSimpleOptions>()
        val file = statSync.invoke(path, options) ?: error("Cannot find the file at path $path")
        val fileSize = file.size
        return fileSize.toLong()
    }

    /**
     * Finds large files touched (added/modified) in the current PR.
     */
    private suspend fun findLargeFiles(): List<FileInfo> {
        val token = getInput("repo-token").ifEmpty {
            println("No repo-token passed in, not going to find large files")
            return emptyList()
        }
        val prNumber = actions.github.context.payload.pull_request?.number ?: return let {
            println("Not running in PR context, not going to find large files")
            emptyList()
        }
        val owner = actions.github.context.repo.owner
        val repo = actions.github.context.repo.repo

        val largeFileThresholdKb = getInput("large-file-threshold-kb").ifEmpty { "100" }.toLong()

        return try {
            val octokit = getOctokitWrapper(token)
            val response = octokit.rest.pulls.listFiles(unsafeJso {
                this.owner = owner
                this.repo = repo
                this.prNumber = prNumber
            }
            ).await()

            val files = response.data.filter { it.status in listOf("added", "modified") }
            files.mapNotNull {
                val fileSize = getFileSizeBytes(it.filename)

                if (fileSize > largeFileThresholdKb * ONE_MB_IN_KB) {
                    FileInfo(
                        filename = it.filename,
                        sizeBytes = fileSize
                    )
                } else {
                    null
                }
            }
        } catch (e: dynamic) {
            println("error: $e")
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private fun getOctokitWrapper(token: String): OctokitWrapper {
        return getOctokit(token) as OctokitWrapper
    }
}

private fun <T> buildObject(builder: T.() -> Unit = {}): T {
    return (js("{}") as T).apply(builder)
}

private const val PREFIX = "jacobras-size-diff-action"
private const val CACHE_FILENAME = "$PREFIX-size.txt"
private const val CACHE_KEY = "$PREFIX-main-file-size"
private const val ONE_MB_IN_KB = 1024