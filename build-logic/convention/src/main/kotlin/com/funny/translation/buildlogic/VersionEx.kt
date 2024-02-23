package com.funny.translation.buildlogic

import org.gradle.api.artifacts.VersionCatalog

fun VersionCatalog.findVersionAsString(alias: String) = findVersion(alias).get().toString()

fun VersionCatalog.findVersionAsInt(alias: String) = findVersionAsString(alias).toInt()

// library
fun VersionCatalog.findLibraryAsString(alias: String) = findLibrary(alias).get().toString()