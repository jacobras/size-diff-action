import kotlin.js.Promise

// https://github.com/octokit/octokit.js
@JsNonModule
internal external interface OctokitWrapper {
    val rest: Rest

    interface Rest {
        val pulls: Pulls

        interface Pulls {
            fun listFiles(options: ListFilesOptions): Promise<ListFilesResponse>
        }
    }
}

internal external interface ListFilesOptions {
    var owner: String
    var repo: String

    @JsName("pull_number")
    var prNumber: Number
}

internal external interface ListFilesResponse {
    val data: Array<ListFilesResponseFileInfo>
}

internal external interface ListFilesResponseFileInfo {
    val filename: String
    val status: String
}