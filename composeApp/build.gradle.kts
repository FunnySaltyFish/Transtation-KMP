
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.transtation.kmp.thirdpartyplugins)
    alias(libs.plugins.transtation.kmp.application)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                addProjectDependencies()
                implementation("org.burnoutcrew.composereorderable:reorderable:0.7.4")
                implementation(compose.components.resources)
            }
        }

        androidMain.dependencies {
            // sqldelight
            implementation(libs.sqldelight.android.driver)

            // 图片选择器
            implementation(project.dependencies.platform("cn.qhplus.emo:bom:2024.03.00"))
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
                // sqldelight
                implementation(libs.sqldelight.driver)

                implementation("javazoom:jlayer:1.0.1")
            }
        }
    }
}

val NAMESPACE = "com.funny.translation"

android {
    namespace = NAMESPACE

    // mainDebug
    sourceSets["debug"].res.srcDirs("src/androidMainDebug/res")

    defaultConfig {
        applicationId = NAMESPACE
        versionCode = libs.versions.project.versionCode.get().toInt()
        versionName = libs.versions.project.versionName.get()
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

    // 其他配置...
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this
            println("output: $output")
            if (output is ApkVariantOutputImpl) {
                // 确定输出文件名
                val today = Date()
                val formatter = SimpleDateFormat("yyyyMMddHHmm")
                val fileName = "Transtation_${variant.flavorName}_${variant.buildType.name}_${variant.versionName}_${formatter.format(today)}.APK"

                output.outputFileName = fileName

                // 打包完成后做的一些事,复制apk到指定文件夹
//                variant.assembleProvider.get().doLast {
//                // 打包完成后复制到的目录
//                val outputFileDir = "${project.projectDir.absolutePath}/release/"
//                    val out = File(outputFileDir)
//                    project.copy {
//                        variant.outputs.forEach { file ->
//                            from(file.outputFile)
//                            into(out)
//                        }
//                    }
//                }
            }
        }
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

buildkonfig {
    packageName = "com.funny.translation.translate"
}

sqldelight {
    databases {
        getByName("Database") {
            packageName = "com.funny.translation.translate.database"
            dependency(project(":base-kmp"))
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
            finalizedBy(signApkTask)
        }
    }
}
