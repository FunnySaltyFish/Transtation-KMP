
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

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
                addProjectDependencies()
            }
        }

        androidMain.dependencies {
            addProjectDependencies()

            // sqldelight
            implementation(libs.sqldelight.android.driver)

            // 图片选择器
            implementation(platform("cn.qhplus.emo:bom:2023.08.00"))
            implementation("cn.qhplus.emo:photo-coil")
            // 图片裁剪
            implementation("com.github.yalantis:ucrop:2.2.6")

            // CameraX core library using the camera2 implementation
            val camerax_version = "1.3.1"
            // If you want to additionally use the CameraX View class
            implementation("androidx.camera:camera-view:${camerax_version}")
            implementation("androidx.camera:camera-camera2:${camerax_version}")
            // If you want to additionally use the CameraX Lifecycle library
            implementation("androidx.camera:camera-lifecycle:${camerax_version}")
        }

        val desktopMain by getting {
            dependencies {
                addProjectDependencies()
                
                // sqldelight
                implementation(libs.sqldelight.driver)
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

fun KotlinDependencyHandler.addProjectDependencies() {
    implementation(project(":base-kmp"))
    implementation(project(":ai"))
    implementation(project(":login"))
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
    packageName = "com.funny.translation.translate"
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
