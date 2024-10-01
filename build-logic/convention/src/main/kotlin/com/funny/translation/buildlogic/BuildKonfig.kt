package com.funny.translation.buildlogic

import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

var debug = true
var configFlavor = "common"

fun configureBuildKonfigFlavorFromTasks(project: Project) {
    val configuredKey = "TranstationConfiguredBuildKonfigFlavor"
    if (System.getProperty(configuredKey, "false") == "true") {
        // println("module: ${project.name}, buildkonfig.flavor already configured")
        return
    }

    val startParameter = project.gradle.startParameter
    if (startParameter.projectProperties.containsKey("buildkonfig.flavor")) {
        // prefer cli parameter
        println("buildkonfig.flavor=${startParameter.projectProperties["buildkonfig.flavor"]}")
        return
    }

    val pattern = Regex("^:composeApp:(assemble|test|bundle|extractApksFor|package)(Google|Common)?(Release|Debug)?(|UnitTest|Exe|Dmg|Deb|Msi)\$")
    val runningTasks = project.gradle.startParameter.taskNames
    val matchingTask = runningTasks.find { it.matches(pattern) } ?: return

    val m = pattern.find(matchingTask) ?: return

    configFlavor = m.groupValues[2].ifBlank { "common" }.lowercase()
    val buildType = m.groupValues[3]
    // println("module: ${project.name}, match task=$matchingTask, flavor=$flavor, buildType=$buildType")
    when (buildType) {
        "Release" -> debug = false
        "Debug" -> debug = true
        // packageExe, packageDmg, packageDeb, packageMsi
        else -> debug = true
    }
    println("module: ${project.name}, flavor=$configFlavor, buildType=$buildType, debug=$debug")
    project.setProperty("buildkonfig.flavor", configFlavor)
    System.setProperty(configuredKey, "true")
}

fun Project.setupBuildKonfig() {
    pluginManager.apply("com.codingfeline.buildkonfig")


    configureBuildKonfigFlavorFromTasks(this)

    configure<BuildKonfigExtension> {
        objectName = "BuildConfig"
        // exposeObjectWithName = 'YourAwesomePublicConfig'

        defaultConfigs {
            println("module: ${project.name}, TranslationDebug=$debug, flavor=$configFlavor, buildkonfig.flavor=${System.getProperty("buildkonfig.flavor")}")
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", configFlavor)
            buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", libs.findVersion("project.versionName").get().toString())
            buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.findVersion("project.versionCode").get().toString())
            // DEBUG
            buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", debug.toString())
            val buildType = if (debug) "Debug" else "Release"
            buildConfigField(FieldSpec.Type.STRING,  "BUILD_TYPE", buildType)

            // 读取 rootProject 的 signing.properties 中的配置
            val localProperties = rootProject.file("signing.properties")
            var magicKey = "OpenSourceMagicKey"
            if (localProperties.exists()) {
                val properties = java.util.Properties()
                properties.load(localProperties.inputStream())
                magicKey = properties.getProperty("MAGIC_KEY", magicKey)
            } else {
                magicKey = "OpenSourceMagicKey"
            }
            buildConfigField(FieldSpec.Type.STRING, "MAGIC_KEY", magicKey)
        }

        defaultConfigs("common") {
            //buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "common")
        }

        defaultConfigs("google") {
            //buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "google")
        }
    }
}
