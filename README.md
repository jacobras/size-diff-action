# size-diff-action
GitHub Action to calculate difference in a file's size, compared to the main branch

### Development

The project uses Kotlin for the logic, but the main Action entrypoint is in JS.

1. Build Kotlin using `gradlew kotlinUpgradeYarnLock :action-logic:jsNodeProductionRun`
2. Build dist using `rollup --config rollup.config.js`