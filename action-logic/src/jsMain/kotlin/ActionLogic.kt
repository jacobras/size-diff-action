import actions.cache.restoreCache
import actions.cache.saveCache
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
    fun buildSummary(path: String): Promise<String> = GlobalScope.promise {
        restoreCache(
            paths = arrayOf(CACHE_FILENAME),
            primaryKey = CACHE_KEY
        )
        val existing = readFileSync(CACHE_FILENAME, BufferEncoding.utf8)
        println(existing)

        val options = buildObject<StatSyncFnSimpleOptions>()
        val file = statSync.invoke(path, options) ?: error("Cannot find the file at path $path")
        val fileSize = file.size

        writeFileSync(CACHE_FILENAME, fileSize.toString())

        val runId = js("process.env.GITHUB_RUN_ID") as? String
        println("GitHub Actions run ID: $runId")

        saveCache(
            paths = arrayOf(CACHE_FILENAME),
            key = "$CACHE_KEY-$runId",
            options = buildObject(),
            enableCrossOsArchive = true
        )

        return@promise "The file at $path is: $fileSize bytes, was: $existing. Run ID: $runId"
    }
}

private fun <T> buildObject(): T {
    return js("{}") as T
}

private const val PREFIX = "jacobras-size-diff-action"
private const val CACHE_FILENAME = "$PREFIX-size.txt"
private const val CACHE_KEY = "$PREFIX-main-file-size"
