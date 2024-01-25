
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties


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
        freeCompilerArgs.addAll("-Xmulti-platform", "-Xexpect-actual-classes")
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
                implementation("org.burnoutcrew.composereorderable:reorderable:0.7.4")
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

            implementation("com.github.princekin-f:EasyFloat:2.0.4")
            implementation("com.github.thomhurst:RoundImageView:1.0.2")

            // 应用更新
            implementation("com.github.azhon:AppUpdate:3.0.6")
        }

        val desktopMain by getting {
            dependencies {
                addProjectDependencies()
                
                // sqldelight
                implementation(libs.sqldelight.driver)

                implementation("javazoom:jlayer:1.0.1")
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

    // mainDebug
    sourceSets["debug"].res.srcDirs("src/androidMainDebug/res")

    defaultConfig {
        applicationId = "com.funny.translation.kmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.project.versionCode.get().toInt()
        versionName = libs.versions.project.versionName.get()
        resourceConfigurations.addAll(arrayOf("zh-rCN", "en"))
        multiDexEnabled = true
        ndk.abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a"))
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            // 如果需要打 release 包，请在项目根目录下自行添加此文件
            /**
             *  STORE_FILE=yourAppStroe.keystore
             *  STORE_PASSWORD=yourStorePwd
             *  KEY_ALIAS=yourKeyAlias
             *  KEY_PASSWORD=yourAliasPwd
             */
            val props = Properties()
            val propFile = File("signing.properties")
            if (propFile.exists()) {
                val reader = BufferedReader(InputStreamReader(FileInputStream(propFile), "utf-8"))
                props.load(reader)

                storeFile = file(props["STORE_FILE"] as String)
                storePassword = props["STORE_PASSWORD"] as String
                keyAlias = props["KEY_ALIAS"] as String
                keyPassword = props["KEY_PASSWORD"] as String

                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        getByName("release") {
            // 临时可调试
            isDebuggable = true
            // 开启代码混淆
            isMinifyEnabled = true
            // Zipalign 优化
            isZipAlignEnabled = true
            // 移除无用的 resource 文件
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    implementation(project(":code-editor"))
}

compose.desktop {
    application {
        mainClass = "MainKt"
        javaHome = "D:/Environment/jdk17"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "Transtation"
            packageVersion = libs.versions.project.versionName.get()
            description = "译站 | Transtation"
            copyright = "©2024 FunnySaltyFish. All rights reserved."
            outputBaseDir.set(projectDir.resolve("release"))

            windows {
                iconFile.set(rootDir.resolve("composeApp/src/desktopMain/kotlin/resources/icon.ico"))
            }
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
        // DEBUG
        val debug = System.getenv("TranslationDebug")?.toBoolean() ?: true
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", debug.toString())
        val buildType = if (debug) "Debug" else "Release"
        buildConfigField(FieldSpec.Type.STRING,  "BUILD_TYPE", buildType)
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

afterEvaluate {
    // debug 和 release 时执行加密代码
    val signApkTask = project(":base-kmp").tasks.named("signApk")

    tasks.withType<Task> {
        if (name.startsWith("assemble") && !name.endsWith("Test")) {
            // 打包前先加密下 Js
            dependsOn(":base-kmp:encryptFunnyJs")
            // 结束后签个名
            // finalizedBy(signApkTask)
        }
    }
}
