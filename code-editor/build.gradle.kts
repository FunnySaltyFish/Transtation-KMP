

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.transtation.kmp.thirdpartyplugins)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll("-Xmulti-platform", "-Xexpect-actual-classes")
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            // include(":android-code-editor:editor", ":android-code-editor:language-base", ":android-code-editor:language-universal")
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

android {
    namespace = "com.funny.translation.codeeditor"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

buildkonfig {
    packageName = "com.funny.translation.codeeditor"
}