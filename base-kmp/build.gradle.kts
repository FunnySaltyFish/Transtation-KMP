import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
//    alias(libs.plugins.mokoResourcesGenerator)
//    alias(libs.plugins.libres)
    id("io.github.skeptick.libres") version "1.2.2"
}

kotlin {
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
                implementation(compose.material)
                implementation(compose.ui)
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // JVM-specific dependencies like OkHttp and Retrofit
                implementation("com.squareup.okhttp3:okhttp:4.11.0")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

                // kotlinx.serialization
                implementation(libs.kotlinx.serialization.json)
                implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0") {
                    // exclude org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.0.0
                    exclude("org.jetbrains.kotlinx", "kotlinx-serialization-core-jvm")
                }

                // androidx-jvm
                implementation(libs.androidx.annotation.jvm)

                implementation(libs.kotlin.reflect)

                // moko
//                implementation("dev.icerock.moko:resources-compose:0.23.0") // for compose multiplatform

                // libres
                implementation("io.github.skeptick.libres:libres-compose:1.2.1")

            }
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.rhino.android)
            implementation(libs.compose.ui.android)

            implementation("com.github.getActivity:ToastUtils:12.0")

        }

        val desktopMain by getting {
            kotlin.srcDir("src/desktopMain/java")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.rhino)
//                org.slf4j:slf4j-simple:2.0.3
                implementation("org.slf4j:slf4j-simple:2.0.3")
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

tasks.withType(JavaExec::class) {
//    systemProperty.s'java.awt.headless', true
//    systemProperty 'sun.arch.data.model', System.getProperty('sun.arch.data.model')
    systemProperties.apply {
        set("java.awt.headless", true)
        set("sun.arch.data.model", System.getProperty("sun.arch.data.model"))
    }
    jvmArgs("--add-exports", "java.base/sun.security.action=ALL-UNNAMED")
}