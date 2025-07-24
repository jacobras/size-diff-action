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
    jvm()

    sourceSets {
        jsMain.dependencies {
            implementation(kotlinWrappers.actions.cache)
            implementation(kotlinWrappers.actions.core)
            implementation(kotlinWrappers.actions.glob)
            implementation(kotlinWrappers.node)
            implementation(libs.kotlin.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertK)
        }
    }
}