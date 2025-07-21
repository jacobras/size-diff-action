import node.fs.StatSyncFnSimpleOptions
import node.fs.statSync

@OptIn(ExperimentalJsExport::class)
@JsExport
object ActionLogic {

    @Suppress("unused")
    fun buildSummary(path: String): String {
        val options = buildObject<StatSyncFnSimpleOptions>()
        val file = statSync.invoke(path, options) ?: error("Cannot find the file at path $path")
        val fileSize = file.size
        return "The file at $path is: $fileSize bytes!"
    }
}

private fun <T> buildObject(): T {
    return js("{}") as T
}