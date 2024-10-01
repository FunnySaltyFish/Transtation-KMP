plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false


    alias(libs.plugins.libres) apply false
    alias(libs.plugins.buildKonfig) apply false
    alias(libs.plugins.sqlDelight) apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven/") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/google/") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter/") }
        maven { url = uri("https://jitpack.io") }
        maven("https://jogamp.org/deployment/maven")
    }
}
