package com.funny.translation.buildlogic

import io.github.skeptick.libres.plugin.ResourcesPluginExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.setupLibres() {
    /**
     * libres {
     *     generatedClassName = "Res" // "Res" by default
     *     generateNamedArguments = true // false by default
     *     baseLocaleLanguageCode = "zh" // "en" by default
     *     camelCaseNamesForAppleFramework = false // false by default
     * }
     */

    pluginManager.apply("io.github.skeptick.libres")

    configure<ResourcesPluginExtension> {
        generatedClassName = "Res" // "Res" by default
        generateNamedArguments = true // false by default
        baseLocaleLanguageCode = "zh" // "en" by default
        camelCaseNamesForAppleFramework = false // false by default
    }
}