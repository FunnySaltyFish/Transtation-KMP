

plugins {
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.transtation.kmp.thirdpartyplugins)
    alias(libs.plugins.transtation.kmp.library)
}

kotlin {
    sourceSets {
        val desktopMain by getting
        androidMain.dependencies {
            implementation(project(":android-code-editor:editor"))
            implementation(project(":android-code-editor:language-base"))
            implementation(project(":android-code-editor:language-universal"))
        }
        commonMain.dependencies {
            implementation(project(":base-kmp"))
        }
        desktopMain.dependencies {

        }
    }
}

val NAMESPACE = "com.funny.translation.codeeditor"

android {
    namespace = NAMESPACE
}

buildkonfig {
    packageName = NAMESPACE
}