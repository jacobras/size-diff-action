import actions.cache.restoreCache
import actions.cache.saveCache
import actions.core.getInput
import actions.core.setOutput
import js.promise.await
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
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

        val newSizeBytes = measureNewSizeFromFile(path)

        val summary = if (!isMainBranch) {
            val existingSizeBytes = readExistingSizeFromCache()
            SummaryBuilder.buildDiff(
                path = path,
                existingSizeBytes = existingSizeBytes,
                newSizeBytes = newSizeBytes
            )
        } else {
            cacheNewFileSize(newSizeBytes)
            "Size stored ($newSizeBytes bytes). Diff will happen when this is run on a non-main branch."
        }
        setOutput("summary", summary)
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

    private fun measureNewSizeFromFile(path: String): Long {
        val options = buildObject<StatSyncFnSimpleOptions>()
        val file = statSync.invoke(path, options) ?: error("Cannot find the file at path $path")
        val fileSize = file.size
        return fileSize.toLong()
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
}

private fun <T> buildObject(builder: T.() -> Unit = {}): T {
    return (js("{}") as T).apply(builder)
}

private const val PREFIX = "jacobras-size-diff-action"
private const val CACHE_FILENAME = "$PREFIX-size.txt"
private const val CACHE_KEY = "$PREFIX-main-file-size"
