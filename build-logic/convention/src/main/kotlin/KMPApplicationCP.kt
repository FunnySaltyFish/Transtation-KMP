
import com.android.build.api.dsl.ApplicationExtension
import com.funny.translation.buildlogic.findVersionAsInt
import com.funny.translation.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

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

                compileSdk = libs.findVersionAsInt("android.compileSdk")
            }

            setupCommonKMP(android)
        }
    }
}