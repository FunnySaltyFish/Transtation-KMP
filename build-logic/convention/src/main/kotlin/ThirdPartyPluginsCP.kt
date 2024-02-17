
import com.funny.translation.buildlogic.setupBuildKonfig
import com.funny.translation.buildlogic.setupLibres
import com.funny.translation.buildlogic.setupSqlDelight
import org.gradle.api.Plugin
import org.gradle.api.Project

class ThirdPartyPluginsCP : Plugin<Project> {
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
         *     // ...
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
            setupBuildKonfig()
            setupSqlDelight()
        }
    }
}