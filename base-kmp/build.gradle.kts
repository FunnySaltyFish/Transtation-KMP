
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.ExperimentalComposeLibrary
import java.io.ByteArrayOutputStream
import java.util.Locale


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
//    alias(libs.plugins.libres)
    alias(libs.plugins.transtation.kmp.thirdpartylibs)
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

        // 指定 androidx.paging 的版本为 3.2.0
        val pagingVersion = "3.2.0"
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

                // JVM-specific dependencies like OkHttp and Retrofit
                api("com.squareup.okhttp3:okhttp:4.11.0")
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

                // androidx.paging
                implementation("androidx.paging:paging-runtime:$pagingVersion")
                implementation("androidx.paging:paging-common:$pagingVersion")

                api(libs.precompose)
                api(libs.precompose.viewmodel) // For ViewModel intergration

//                api("io.github.kevinnzou:compose-webview-multiplatform:1.8.0")
                api("io.github.qdsfdhvh:image-loader:1.7.1")

                implementation("com.mikepenz:multiplatform-markdown-renderer:0.10.0")

                // import org.json.JSONObject
                api("org.json:json:20210307")

                implementation(libs.rhino)
            }
        }

        androidMain.dependencies {
            api(libs.compose.ui.tooling.preview)
            api(libs.androidx.activity.compose)
            api(libs.compose.ui.android)

//            implementation(libs.rhino.android)
            implementation("com.github.getActivity:ToastUtils:12.0")

            // mmkv
            api(libs.mmkv)

            // libs/monet.aar
            implementation(project(":local_repo:monet"))

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
//                org.slf4j:slf4j-simple:2.0.3
                implementation("org.slf4j:slf4j-simple:2.0.3")
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

android {
    namespace = "com.funny.translation.kmp.base"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    dependencies {
        debugApi(libs.compose.ui.tooling)

        androidTestImplementation("androidx.test.ext:junit:1.1.3")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    }
}

compose.desktop {

}

//multiplatformResources {
//    multiplatformResourcesPackage = "com.funny.translation" // required
//}

//libres {
//    generatedClassName = "Res" // "Res" by default
//    generateNamedArguments = true // false by default
//    baseLocaleLanguageCode = "zh" // "en" by default
//    camelCaseNamesForAppleFramework = false // false by default
//}

buildkonfig {
    packageName = "com.funny.translation"
    objectName = "BuildConfig"
    // exposeObjectWithName = 'YourAwesomePublicConfig'

    defaultConfigs {
        buildConfigField(STRING, "FLAVOR", "common")
        buildConfigField(STRING, "VERSION_NAME", libs.versions.project.versionName.get())
        buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.versions.project.versionCode.get())
        buildConfigField(STRING, "BUILD_TYPE", "debug")
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

// 定义函数，用于输出 Hello, FunnyTranslation Open Source
fun printHello(exec: Exec) {
    // just print FunnyTranslation OpenSource
    // windows: cmd /c echo FunnyTranslation OpenSource
    // linux: sh -c echo FunnyTranslation OpenSource
    if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")) {
        exec.commandLine("cmd", "/c", "echo", "Hello FunnyTranslation-OpenSource")
    } else {
        exec.commandLine("sh", "-c", "echo", "Hello FunnyTranslation-OpenSource")
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