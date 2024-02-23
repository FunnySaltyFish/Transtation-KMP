plugins {
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.transtation.kmp.thirdpartyplugins)
    alias(libs.plugins.transtation.kmp.library)
}

kotlin {
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {

        }
        commonMain.dependencies {
            implementation(project(":base-kmp"))
            implementation(libs.jtokkit)
        }
        desktopMain.dependencies {

        }
    }
}

val NAMESPACE = "com.funny.translation.ai"

android {
    namespace = NAMESPACE
}

buildkonfig {
    packageName = NAMESPACE
}