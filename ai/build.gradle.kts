import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.libres)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.sqlDelight)
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

        }
        commonMain.dependencies {
            implementation(project(":base-kmp"))
//            om.knuddels:jtokkit:0.6.1
            implementation("com.knuddels:jtokkit:0.6.1")
        }
        desktopMain.dependencies {

        }
    }
}

android {
    namespace = "com.funny.translation.ai"
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

libres {
    generatedClassName = "Res" // "Res" by default
    generateNamedArguments = true // false by default
    baseLocaleLanguageCode = "zh" // "en" by default
    camelCaseNamesForAppleFramework = false // false by default
}

buildkonfig {
    packageName = "com.funny.translation.ai"
    objectName = "BuildConfig"
    // exposeObjectWithName = 'YourAwesomePublicConfig'

    defaultConfigs {
        buildConfigField(STRING, "FLAVOR", "common")
        buildConfigField(STRING, "VERSION_NAME", libs.versions.project.versionName.get())
        buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.versions.project.versionCode.get())
        // debug
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    }

    defaultConfigs("common") {
        buildConfigField(STRING, "FLAVOR", "common")
    }

    defaultConfigs("google") {
        buildConfigField(STRING, "FLAVOR", "google")
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.funny.translation.database")
        }
    }
}