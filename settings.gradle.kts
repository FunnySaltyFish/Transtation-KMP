rootProject.name = "Transtation-KMP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://plugins.gradle.org/m2/")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo1.maven.org/maven2/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/jcenter/")
        maven("https://jitpack.io")
        maven("https://maven.google.com/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        google()
    }
}

// 添加自定义脚本
includeBuild("build-logic")

include(":composeApp")
include(":base-kmp")
include(":ai")
include(":login")

include(":android-code-editor:editor", ":android-code-editor:language-base", ":android-code-editor:language-universal")
include(":code-editor")

// https://www.jianshu.com/p/a6a221e04d30
// include(":local_repo:monet")


