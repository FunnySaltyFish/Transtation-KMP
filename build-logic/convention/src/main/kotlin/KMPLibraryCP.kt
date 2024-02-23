
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.funny.translation.buildlogic.findVersionAsInt
import com.funny.translation.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KMPLibraryCP: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            val android = extensions.getByType(LibraryExtension::class.java).apply {
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                compileSdk = libs.findVersionAsInt("android.compileSdk")
            }

            setupCommonKMP(android)
        }
    }
}



fun Project.setupCommonKMP(
    android: CommonExtension<*, *, *, *, *>
) {
    with(pluginManager) {
        apply("kotlin-multiplatform")
        apply("org.jetbrains.compose")
    }

    val kotlin = extensions.getByType(KotlinMultiplatformExtension::class.java)
    kotlin.apply {
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
    }

    android.apply {
        namespace = "com.funny.translation.kmp"


        sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        sourceSets["main"].res.srcDirs("src/androidMain/res")
        sourceSets["main"].resources.srcDirs("src/commonMain/resources")

        defaultConfig {
            minSdk = libs.findVersionAsInt("android.minSdk")
            resourceConfigurations.addAll(arrayOf("zh-rCN", "en"))
            ndk.abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a"))
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        dependencies {
            add("debugImplementation", libs.findLibrary("compose.ui.tooling").get())
        }
    }
}