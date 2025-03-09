
import org.jetbrains.compose.ExperimentalComposeLibrary
import java.io.ByteArrayOutputStream
import java.util.Locale


plugins {
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.transtation.kmp.thirdpartyplugins)
    alias(libs.plugins.transtation.kmp.library)
}

kotlin {
    sourceSets {
        // 指定 androidx.paging 的版本为 3.2.0
        val pagingVersion = libs.versions.pagingVersion.get()
        configurations.all {
            resolutionStrategy {
                force("androidx.paging:paging-runtime:$pagingVersion")
                force("androidx.paging:paging-common:$pagingVersion")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project.dependencies.platform(libs.compose.bom))
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.ui)
                @OptIn(ExperimentalComposeLibrary::class)
                api(compose.components.resources)
                api(compose.materialIconsExtended)

                api("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")

                // JVM-specific dependencies like OkHttp and Retrofit
                api("com.squareup.okhttp3:okhttp:4.12.0")
                api("com.squareup.retrofit2:retrofit:2.9.0")

                // kotlinx.serialization
                api(libs.kotlinx.serialization.json)
                implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0") {
                    // exclude org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.0.0
                    exclude("org.jetbrains.kotlinx", "kotlinx-serialization-core-jvm")
                }
                api(libs.kotlin.reflect)
                api(libs.kotlinx.collections.immutable)
                api(libs.kotlin.coroutines)

                // androidx-jvm
                api(libs.androidx.annotation.jvm)

                // libres
                api(libs.libres.compose)

                // sqlDelight
                api(libs.sqldelight.primitive.adapters)
                api(libs.sqldelight.coroutines.extensions)
                api(libs.sqldelight.androidx.paging3.extensions)

                // kmp paging
                api(libs.kmp.paging.common)
                api(libs.kmp.paging.compose.common)

                api(libs.precompose)
                api(libs.jetbrains.lifecycle.viewmodel) // For ViewModel intergration

//                api("io.github.kevinnzou:compose-webview-multiplatform:1.8.0")
                api("io.github.qdsfdhvh:image-loader:1.7.1")

                implementation("com.mikepenz:multiplatform-markdown-renderer:0.10.0")

                // import org.json.JSONObject
                api("org.json:json:20210307")

                implementation("com.github.fengzhizi715.okhttp-logging-interceptor:core:v1.1.4")

                implementation(libs.rhino)

                implementation("ca.gosyer:kotlin-multiplatform-appdirs:1.1.1")

                api("com.materialkolor:material-kolor:2.0.1")

                // Provides the SketchZoomAsyncImage component adapted to the Sketch v4+ image loader (recommended)
                api("io.github.panpf.zoomimage:zoomimage-compose-sketch4:1.1.2")

                val sketchVersion = "4.0.4"
                // Provides the core functions of Sketch as well as singletons and extension
                // functions that rely on singleton implementations
                api("io.github.panpf.sketch4:sketch-compose:$sketchVersion")
                // Provides the ability to load network images
                api("io.github.panpf.sketch4:sketch-http:$sketchVersion")
                // ok http support
                api("io.github.panpf.sketch4:sketch-http-okhttp:$sketchVersion")

            }
        }

        androidMain.dependencies {
            api(libs.compose.ui.tooling.preview)
            api(libs.androidx.activity.compose)
            api(libs.androidx.appcompat)
            api(libs.compose.ui.android)

            // androidx.paging
//            implementation(libs.androidx.paging.runtime)
//            implementation(libs.androidx.paging.common)

//            implementation(libs.rhino.android)
            implementation("com.github.getActivity:ToastUtils:12.0")

            // mmkv
            api(libs.mmkv)

            // Markwon
            val markwon_version = "4.6.2"
            implementation("io.noties.markwon:core:$markwon_version")
            implementation("io.noties.markwon:ext-strikethrough:$markwon_version")
            implementation("io.noties.markwon:ext-tables:$markwon_version")
            implementation("io.noties.markwon:html:$markwon_version")
            implementation("io.noties.markwon:image-coil:$markwon_version")
            implementation("io.noties.markwon:linkify:$markwon_version")
            implementation("me.saket:better-link-movement-method:2.2.0")

            val accompanist_version = "0.32.0"
            api("com.google.accompanist:accompanist-systemuicontroller:$accompanist_version")
            api("com.google.accompanist:accompanist-pager-indicators:$accompanist_version")
            api("com.google.accompanist:accompanist-permissions:$accompanist_version")

            // 刘海屏适配
            api("com.github.smarxpan:NotchScreenTool:0.0.1")

            implementation("androidx.biometric:biometric:1.2.0-alpha05")

        }

        val desktopMain by getting {
            kotlin.srcDir("src/desktopMain/java")
            dependencies {
                api(compose.desktop.currentOs)

                implementation("org.slf4j:slf4j-simple:2.0.4")

                implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

                implementation("com.github.Dansoftowner:jSystemThemeDetector:3.6")
            }

            configurations.commonMainApi {
                exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
            }
        }

        commonTest.dependencies {
            // depends on commonMain
            api(libs.kotlin.test)
            api(libs.kotlin.test.junit)
        }
    }
}

val NAMESPACE = "com.funny.translation.kmp.base"

android {
    namespace = NAMESPACE

    dependencies {
        androidTestImplementation(libs.androidx.test.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }
}

compose.desktop {

}



buildkonfig {
    packageName = "com.funny.translation"
}

// 定义函数，用于输出 Hello, Transtation-OpenSource
fun printHello(exec: Exec) {
    if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")) {
        exec.commandLine("cmd", "/c", "echo", "Hello Transtation-OpenSource")
    } else {
        exec.commandLine("sh", "-c", "echo", "Hello Transtation-OpenSource")
    }
}

tasks.register<Exec>("encryptFunnyJs") {
    // 如果 funny_sign_v1_release 存在，则用它
    val release = File(rootDir, "funny_sign_v1_release_template.js")
    println("release.exists() = " + release.exists())

    if (release.exists()) {
        val filePath = release.absolutePath
        val targetFilePath = rootDir.resolve("base-kmp/src/commonMain/resources/assets/funny_sign_v1_release.js").absolutePath
        val versionCode = libs.versions.project.versionCode.get()
        commandLine("node", rootProject.file("encrypt_funny_js.js"), filePath, targetFilePath, versionCode)
    } else {
        printHello(this)
    }

    standardOutput = ByteArrayOutputStream()
    doLast {
        println(standardOutput.toString())
    }
}

tasks.register<Exec>("signApk") {
    // 执行根目录下的 sign_new_key.py
    val signNewKey = rootProject.file("sign_new_key.py")

    if (signNewKey.exists()) {
        commandLine("python", signNewKey.path, rootDir.path)
    } else {
        printHello(this)
    }
}