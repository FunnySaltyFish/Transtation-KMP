package com.funny.translation.buildlogic

import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.setupBuildKonfig() {
    pluginManager.apply("com.codingfeline.buildkonfig")

    configure<BuildKonfigExtension> {
        objectName = "BuildConfig"
        // exposeObjectWithName = 'YourAwesomePublicConfig'

        defaultConfigs {
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "common")
            buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", libs.findVersion("project.versionName").get().toString())
            buildConfigField(FieldSpec.Type.INT, "VERSION_CODE", libs.findVersion("project.versionCode").get().toString())
            // DEBUG
            val debug = System.getProperty("TranslationDebug")?.toBoolean() ?: true
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