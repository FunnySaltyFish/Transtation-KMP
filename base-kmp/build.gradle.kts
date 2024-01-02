
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.ExperimentalComposeLibrary


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
    compilerOptions {
        freeCompilerArgs.add("-Xmulti-platform")
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }


    jvm("desktop") {

    }
    
    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.compose.bom))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // JVM-specific dependencies like OkHttp and Retrofit
                implementation("com.squareup.okhttp3:okhttp:4.11.0")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")

                // kotlinx.serialization
                implementation(libs.kotlinx.serialization.json)
                implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0") {
                    // exclude org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.0.0
                    exclude("org.jetbrains.kotlinx", "kotlinx-serialization-core-jvm")
                }
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.collections.immutable)

                // androidx-jvm
                implementation(libs.androidx.annotation.jvm)

                // moko
//                implementation("dev.icerock.moko:resources-compose:0.23.0") // for compose multiplatform

                // libres
                implementation(libs.libres.compose)

                // sqlDelight
                implementation(libs.sqldelight.primitive.adapters)
                implementation(libs.sqldelight.coroutines.extensions)

                api(libs.precompose)
                api(libs.precompose.viewmodel) // For ViewModel intergration

                api("io.github.kevinnzou:compose-webview-multiplatform:1.8.0")
                api("io.github.qdsfdhvh:image-loader:1.7.1")
            }
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.rhino.android)
            implementation(libs.compose.ui.android)

            implementation("com.github.getActivity:ToastUtils:12.0")

            // sqldelight
            implementation(libs.sqldelight.android.driver)
            // mmkv
            implementation(libs.mmkv)

            // libs/monet.aar
            implementation(files("libs/monet.aar"))

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
        }

        val desktopMain by getting {
            kotlin.srcDir("src/desktopMain/java")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.rhino)
//                org.slf4j:slf4j-simple:2.0.3
                implementation("org.slf4j:slf4j-simple:2.0.3")
                implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

                // sqldelight
                implementation(libs.sqldelight.driver)
            }
        }

        commonTest.dependencies {
            // depends on commonMain
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.test.junit)

            // moko
//            implementation("dev.icerock.moko:resources-test:0.23.0")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        debugImplementation(libs.compose.ui.tooling)

        androidTestImplementation("androidx.test.ext:junit:1.1.3")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    }
}

compose.desktop {

}

//multiplatformResources {
//    multiplatformResourcesPackage = "com.funny.translation" // required
//}

libres {
    generatedClassName = "Res" // "Res" by default
    generateNamedArguments = true // false by default
    baseLocaleLanguageCode = "zh" // "en" by default
    camelCaseNamesForAppleFramework = false // false by default
}

buildkonfig {
    packageName = "com.funny.translation"
    objectName = "BuildConfig"
    // exposeObjectWithName = 'YourAwesomePublicConfig'

    defaultConfigs {
        buildConfigField(STRING, "FLAVOR", "common")
        buildConfigField(STRING, "VERSION_NAME", libs.versions.project.versionName.get())
        buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.versions.project.versionCode.get())
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