
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
        }
        desktopMain.dependencies {

        }
    }
}

val NAMESPACE= "com.funny.translation.login"

android {
    namespace = NAMESPACE
}

buildkonfig {
    packageName = NAMESPACE
}