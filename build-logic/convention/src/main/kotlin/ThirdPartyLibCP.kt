
import com.funny.translation.buildlogic.setupLibres
import org.gradle.api.Plugin
import org.gradle.api.Project

class ThirdPartyLibCP : Plugin<Project> {
    override fun apply(target: Project) {
        /**
         * libres {
         *     generatedClassName = "Res" // "Res" by default
         *     generateNamedArguments = true // false by default
         *     baseLocaleLanguageCode = "zh" // "en" by default
         *     camelCaseNamesForAppleFramework = false // false by default
         * }
         *
         * buildkonfig {
         *     packageName = "com.funny.translation"
         *     objectName = "BuildConfig"
         *     // exposeObjectWithName = 'YourAwesomePublicConfig'
         *
         *     defaultConfigs {
         *         buildConfigField(STRING, "FLAVOR", "common")
         *         buildConfigField(STRING, "VERSION_NAME", libs.versions.project.versionName.get())
         *         buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.versions.project.versionCode.get())
         *         buildConfigField(STRING, "BUILD_TYPE", "debug")
         *     }
         *
         *     defaultConfigs("common") {
         *         buildConfigField(STRING, "FLAVOR", "common")
         *     }
         *
         *     defaultConfigs("google") {
         *         buildConfigField(STRING, "FLAVOR", "google")
         *     }
         * }
         *
         * sqldelight {
         *     databases {
         *         create("Database") {
         *             packageName.set("com.funny.translation.database")
         *         }
         *     }
         * }
         */

        with(target) {
            setupLibres()
        }
    }
}