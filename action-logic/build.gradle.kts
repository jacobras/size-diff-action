plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js {
        useCommonJs()
        nodejs {
            binaries.executable()
        }
        outputModuleName = "actionLogic"
    }

    sourceSets {
        jsMain.dependencies {
            implementation(kotlinWrappers.actions.cache)
            implementation(kotlinWrappers.actions.core)
            implementation(kotlinWrappers.node)
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.humanReadable)
        }
    }
}