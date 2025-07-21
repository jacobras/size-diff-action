import actions.cache.saveCache
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import node.fs.StatSyncFnSimpleOptions
import node.fs.statSync
import node.fs.writeFileSync
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
object ActionLogic {

    @OptIn(DelicateCoroutinesApi::class)
    @Suppress("unused")
    fun buildSummary(path: String): Promise<String> = GlobalScope.promise {
        val options = buildObject<StatSyncFnSimpleOptions>()
        val file = statSync.invoke(path, options) ?: error("Cannot find the file at path $path")
        val fileSize = file.size

        writeFileSync(CACHE_FILENAME, fileSize.toString())

        saveCache(arrayOf(CACHE_FILENAME), CACHE_KEY, buildObject(), buildObject())

        return@promise "The file at $path is: $fileSize bytes!"
    }
}

private fun <T> buildObject(): T {
    return js("{}") as T
}

private const val PREFIX = "jacobras-size-diff-action"
private const val CACHE_FILENAME = "$PREFIX-size.txt"
private const val CACHE_KEY = "$PREFIX-main-file-size"
