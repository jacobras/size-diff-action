# Size Diff Action

GitHub Action to track file size changes in PRs. It can track a single file and/or list all modified large files in a
PR. For example, here's how it looks tracking an Android app's debug APK size:

![](docs/screenshot.png)

This will also show increases caused by adding/updating third-party dependencies.

Passing in a file to track is optional since v2, here's how it looks only listing the large modified files in a PR:

![](docs/screenshot_large_files_only.png)

## Usage

Add `jacobras/size-diff-action@v2` with these parameters:

| Parameter              | Required?  | Default  | Description                                                                        |
|------------------------|------------|----------|------------------------------------------------------------------------------------|
| `path`                 | _optional_ |          | File to track the size of (can be a glob pattern).                                 |
| `main-branch-name`     | _optional_ | `"main"` | Name of your main branch.                                                          |
| `repo-token`           | _optional_ |          | Pass in `${{ secrets.GITHUB_TOKEN }}` to print large files added/modified in a PR. |
| `large-file-threshold` | _optional_ | `"100"`  | Threshold (in kilobytes) on what to consider (and list) a "large file".            |

The output is a summary that can be posted as a comment to PRs. For example, to track an Android app's debug APK size:

```yml
- name: Calculate APK size difference
  id: size-diff
  uses: jacobras/size-diff-action@v2
  with:
    path: app/build/outputs/apk/debug/app-debug.apk # any file here to track
    repo-token: ${{ secrets.GITHUB_TOKEN }} # to list large modified files in a PR
    large-file-threshold-kb: 50 # optional, defaults to 100 kB

- name: Comment APK size difference
  uses: marocchino/sticky-pull-request-comment@v2
  with:
    header: size-diff
    message: ${{ steps.size-diff.outputs.summary }}
```

_Specify the `main-branch-name` parameter if your main branch is not called "main."_

> [!IMPORTANT]
> Make sure to add the `pull-requests: write` permission for the comment poster.
> See https://github.com/marocchino/sticky-pull-request-comment for more details.

<details>
<summary>Example: tracking a Kotlin/JS library's production JS size</summary>

```yml
- name: Build production JS file for size comparison
  run: ./gradlew compileProductionExecutableKotlinJs

- name: Calculate JS size difference
  id: size-diff
  uses: jacobras/size-diff-action@v2
  with:
    path: build/js/packages/composeApp/kotlin/Human-Readable.js

- name: Comment JS size difference
  uses: marocchino/sticky-pull-request-comment@v2
  with:
    header: size-diff
    message: ${{ steps.size-diff.outputs.summary }}
```

</details>

## Limitations/known issues

1. Tracks only one file in `path`.
2. File size is stored in GH Action's cache. If it's not used for 7 days, it gets deleted, so the main branch will have
   to run again to write the main (golden) file size.
3. Because this uses GH Action's cache internally, it only works for branches that are branched off the main branch
   where the file size has been cached.
4. Doesn't work on Windows runners. Temporary file with size gets created, but `saveCache()` can't find it for whatever
   reason (help welcome). Workaround: upload your file in an artifact, then in a job running on ubuntu-latest, download
   it and run the size diff action.

## Q & A

#### Why does the comment show "Previous size: -1 byte"?

That happens when the action hasn't been run on the main/develop branch yet. Make sure to check the `main-branch-name`
parameter. It could also mean the cache was deleted, which happens by GitHub when an entry hasn't been touched for 7
days.

#### Does this commit the file size on every change?

No, the file size is stored in GitHub Action's cache, so no continuous commits are needed/done.

## Contributing

### Publishing the Action

The project is mostly Kotlin, with just the main entrypoint written in JS.

1. Build JS from Kotlin module using `gradlew kotlinUpgradeYarnLock :action-logic:jsNodeProductionRun`
2. Build full JS dist using `rollup --config rollup.config.js`
3. Push /dist.