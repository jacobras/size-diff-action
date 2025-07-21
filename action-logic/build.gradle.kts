plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js {
        useCommonJs()
        binaries.executable()
        outputModuleName = "action"
    }

    sourceSets {
        jsMain.dependencies {
        }
    }
}