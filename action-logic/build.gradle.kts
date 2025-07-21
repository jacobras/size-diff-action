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
            implementation(kotlinWrappers.node)
            implementation(libs.humanReadable)
        }
    }
}