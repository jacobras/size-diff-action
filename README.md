# Size Diff Action

GitHub Action to calculate difference in a file's size, compared to the main branch

## Installation

TODO...

### Publishing

I couldn't get the entire project working in Kotlin, so it's a mix. The logic is in Kotlin,
but the main Action entrypoint is in JS.

1. Build JS from Kotlin module using `gradlew kotlinUpgradeYarnLock :action-logic:jsNodeProductionRun`
2. Build full JS dist using `rollup --config rollup.config.js`