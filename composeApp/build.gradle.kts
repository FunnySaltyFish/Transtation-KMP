
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
                implementation("me.saket.telephoto:zoomable:0.11.2")
            }
        }

        androidMain.dependencies {
            // sqldelight
            implementation(libs.sqldelight.android.driver)
            // 图片选择器
            implementation(project.dependencies.platform("cn.qhplus.emo:bom:2024.09.00"))
            implementation("cn.qhplus.emo:photo-coil") {
                exclude("androidx.compose.ui", "ui-test-junit4")
//                ui-test-android
                exclude("androidx.compose.ui", "ui-test-android")
            }
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
        val propFile = File("signing.properties")
        if (propFile.exists()) {
            create("release") {
                // 如果需要打 release 包，请在项目根目录下自行添加此文件
                /**
                 *  STORE_FILE=yourAppStroe.keystore
                 *  STORE_PASSWORD=yourStorePwd
                 *  KEY_ALIAS=yourKeyAlias
                 *  KEY_PASSWORD=yourAliasPwd
                 */
                val props = Properties()
                val reader =
                    BufferedReader(InputStreamReader(FileInputStream(propFile), "utf-8"))
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
        val usedSigningConfig = kotlin.runCatching { signingConfigs.getByName("release") }
            .getOrDefault(signingConfigs.getByName("debug"))
        getByName("release") {
            // 临时可调试
            isDebuggable = false
            // 开启代码混淆
            isMinifyEnabled = true
            // 移除无用的 resource 文件
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = usedSigningConfig
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            signingConfig = usedSigningConfig
        }
    }

    // variants
    productFlavors {
        flavorDimensions.add("default")

        create("common") {
            dimension = "default"

        }
        // google
        create("google") {
            dimension = "default"
            versionNameSuffix = "-google"
            applicationIdSuffix = ".google"
        }
    }

    // 其他配置...
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this
            // println("output: $output")
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
        // 下面的注释是我踩的坑
        // 这个 MainClass 一定要写到类全名，如果有包名一定要写全，否则打包后会找不到入口，运行直接无效果
        mainClass = "MainKt"
        javaHome = "D:/Environment/jdk17"

        val now = System.currentTimeMillis()

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "Transtation"
            packageVersion = libs.versions.project.versionName.get()
            // Description 中如果有中文，需要指定字符集为 UTF-8; Java 17 以上默认字符集为 UTF-8
            description = "译站 | Transtation"
            copyright = "©2024 FunnySaltyFish. All rights reserved."
            outputBaseDir.set(projectDir.resolve("release"))
            // 如果运行报错 java.lang.ClassNotFoundException: java.sql.DriverManager
            // 你需要下面这行
            modules("java.sql")
            // javax.script
            modules("java.scripting")
            // 加上 -Dfile.encoding=UTF-8，这样 slf4j 输出的日志就不会乱码了
            jvmArgs += listOf("-Dfile.encoding=UTF-8")

            windows {
                iconFile.set(rootDir.resolve("composeApp/src/desktopMain/kotlin/resources/icon.ico"))
                // 是否创建桌面快捷方式
                shortcut = true
                // 是否创建开始菜单
                menu = true
                // 升级的 UUID，注意必须要满足 UUID 格式，不能乱起
                upgradeUuid = "6968d1f5-ff2e-11ee-a22d-b07d64123c7e"
                // Windows 下的版本号需要满足
                // MAJOR.MINOR.BUILD
                // 其中 MAJOR 和 MINOR 必须是 0-255，BUILD 必须是 0-65535
                // 这里加上了当前时间的毫秒数是因为，如果版本号一样，打包后会提示已经安装了，无法安装
                packageVersion = "${libs.versions.project.versionName.get()}${now%1000}"

                exePackageVersion = packageVersion

            }
        }

        buildTypes {
            release {
                // 配置 proguard 的规则
                proguard {
                    configurationFiles.setFrom(files("proguard-rules.pro"))
                }
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
        if (name.startsWith("assemble") && !name.endsWith("Test") && !name.contains("Desktop")) {
            // 打包前先加密下 Js
            dependsOn(":base-kmp:encryptFunnyJs")
            // 结束后签个名
            finalizedBy(signApkTask)
        }
    }
}

//tasks.whenTaskAdded {
//    val task = this
//    if(task.name == "jsLegacyBrowserTest" || task.name == "jsLegacyNodeTest") {
//        task.enabled = false
//    }
//}