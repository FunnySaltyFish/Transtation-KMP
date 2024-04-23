package com.funny.translation.buildlogic

import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

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

    val pattern = Regex("^:composeApp:(assemble|test|bundle|extractApksFor|package)(\\w*)(Release|Debug)?(|UnitTest|Exe|Dmg|Deb|Msi)\$")
    val runningTasks = project.gradle.startParameter.taskNames
    val matchingTask = runningTasks.find { it.matches(pattern) } ?: return

    val m = pattern.find(matchingTask) ?: return

    val flavor = m.groupValues[2]
    val buildType = m.groupValues[3]
    // println("module: ${project.name}, match task=$matchingTask, flavor=$flavor, buildType=$buildType")
    val envKey = "TranslationDebug"
    when (buildType) {
        "Release" -> System.setProperty(envKey, "false")
        "Debug" -> System.setProperty(envKey, "true")
        // packageExe, packageDmg, packageDeb, packageMsi
        else -> System.setProperty(envKey, "true")
    }
    val buildkonfigFlavor = "common"
    println("module: ${project.name}, flavor=$flavor, buildType=$buildType; final buildkonfig.flavor=$buildkonfigFlavor")
    project.setProperty("buildkonfig.flavor", buildkonfigFlavor)
    System.setProperty(configuredKey, "true")
}

fun Project.setupBuildKonfig() {
    pluginManager.apply("com.codingfeline.buildkonfig")

    configure<BuildKonfigExtension> {
        objectName = "BuildConfig"
        configureBuildKonfigFlavorFromTasks(this@setupBuildKonfig)
        // exposeObjectWithName = 'YourAwesomePublicConfig'

        defaultConfigs {
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "common")
            buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", libs.findVersion("project.versionName").get().toString())
            buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.findVersion("project.versionCode").get().toString())
            // DEBUG
            val debug = System.getProperty("TranslationDebug", "true") == "true"
            println("module: ${project.name}, TranslationDebug=$debug")
            buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", debug.toString())
            val buildType = if (debug) "Debug" else "Release"
            buildConfigField(FieldSpec.Type.STRING,  "BUILD_TYPE", buildType)
        }

        defaultConfigs("common") {
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "common")
        }

        defaultConfigs("google") {
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "google")
        }
    }
}