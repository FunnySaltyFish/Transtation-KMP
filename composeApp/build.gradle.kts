
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.libres)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.sqlDelight)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xmulti-platform")
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    jvm("desktop") {
    }
    
    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":base-kmp"))
                implementation(project(":ai"))
                implementation(project(":login"))
            }
        }

        androidMain.dependencies {

        }

        val desktopMain by getting {
            dependencies {
            }
        }
    }
}

android {
    namespace = "com.funny.translation.kmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.funny.translation.kmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.funny.translation.kmp"
            packageVersion = "1.0.0"
        }
    }
}

libres {
    generatedClassName = "Res" // "Res" by default
    generateNamedArguments = true // false by default
    baseLocaleLanguageCode = "zh" // "en" by default
    camelCaseNamesForAppleFramework = false // false by default
}

buildkonfig {
    packageName = "com.funny.translation.login"
    objectName = "BuildConfig"
    // exposeObjectWithName = 'YourAwesomePublicConfig'

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "common")
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", libs.versions.project.versionName.get())
        buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.versions.project.versionCode.get())
    }

    defaultConfigs("common") {
        buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "common")
    }

    defaultConfigs("google") {
        buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "google")
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.funny.translation.database")
        }
    }
}
