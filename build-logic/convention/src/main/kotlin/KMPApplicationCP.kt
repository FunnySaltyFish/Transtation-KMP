
import com.android.build.api.dsl.ApplicationExtension
import com.funny.translation.buildlogic.findVersionAsInt
import com.funny.translation.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KMPApplicationCP: Plugin<Project> {
    override fun apply(target: Project) {

        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            val android = extensions.getByType(ApplicationExtension::class.java).apply {
                defaultConfig {
                    multiDexEnabled = true
                    targetSdk = libs.findVersionAsInt("android.targetSdk")
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                }

                dependencies {
                    add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.1.3")
                }

                compileSdk = libs.findVersionAsInt("android.compileSdk")
            }

            setupCommonKMP(android)
        }
    }
}