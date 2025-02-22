
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.funny.translation.buildlogic.findVersionAsInt
import com.funny.translation.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

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
    android: CommonExtension<*, *, *, *, *, *>
) {
    with(pluginManager) {
        apply("kotlin-multiplatform")
        apply("org.jetbrains.compose")
        // Since Compose 1.6.10, Kotlin 2.0.0
        apply("org.jetbrains.kotlin.plugin.compose")
    }

    val kotlin = extensions.getByType(KotlinMultiplatformExtension::class.java)
    kotlin.apply {
        androidTarget { }

        jvm("desktop")

        // from https://youtrack.jetbrains.com/issue/KT-61573/Emit-the-compilation-warning-on-expect-actual-classes.-The-warning-must-mention-that-expect-actual-classes-are-in-Beta#focus=Comments-27-10358357.0-0
        targets.configureEach {
            compilations.configureEach {
                compileTaskProvider.get().compilerOptions {
                    freeCompilerArgs.addAll("-Xmulti-platform", "-Xexpect-actual-classes")
                }
            }
        }
    }



    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    android.apply {
        namespace = "com.funny.translation.kmp"


//        sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//        sourceSets["main"].res.srcDirs("src/androidMain/res")
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