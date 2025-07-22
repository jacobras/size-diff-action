import actions.cache.restoreCache
import actions.cache.saveCache
import actions.core.getInput
import actions.core.setOutput
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
        val path = getInput("path")
        val diffSize = getInput("diffSize").toBoolean()
        println("diffSize input: $diffSize")

        val newSizeBytes = measureNewSizeFromFile(path)

        val summary = if (diffSize) {
            val existingSizeBytes = readExistingSizeFromCache()
            SummaryBuilder.buildDiff(
                path = path,
                existingSizeBytes = existingSizeBytes,
                newSizeBytes = newSizeBytes
            )
        } else {
            cacheNewFileSize(newSizeBytes)
            "Size stored. Diff will happen when diffSize is set to true"
        }
        setOutput("summary", summary)
    }

    private suspend fun readExistingSizeFromCache(): Long {
        val cacheResult = restoreCache(
            paths = arrayOf(CACHE_FILENAME),
            primaryKey = CACHE_KEY
        )
        println(cacheResult)
        val existing = try {
            readFileSync(CACHE_FILENAME, BufferEncoding.utf8).toLong()
        } catch (e: dynamic) {
            println("Failed to read existing file: $e")
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

        saveCache(
            paths = arrayOf(CACHE_FILENAME),
            key = "$CACHE_KEY-$runId",
            options = buildObject(),
            enableCrossOsArchive = true
        )
    }
}

private fun <T> buildObject(): T {
    return js("{}") as T
}

private const val PREFIX = "jacobras-size-diff-action"
private const val CACHE_FILENAME = "$PREFIX-size.txt"
private const val CACHE_KEY = "$PREFIX-main-file-size"
