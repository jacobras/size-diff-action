# Size Diff Action

GitHub Action to calculate difference in a file's size, compared to the main branch

## Installation

TODO...

## Contributing

### Publishing the Action

The project is mostly Kotlin, with just the main entrypoint written in JS.

1. Build JS from Kotlin module using `gradlew kotlinUpgradeYarnLock :action-logic:jsNodeProductionRun`
2. Build full JS dist using `rollup --config rollup.config.js`
3. Push /dist.