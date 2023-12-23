plugins {
    `kotlin-dsl`
    alias(libs.plugins.androidLibrary)
}

android {
    compileSdk = Versions.compileSdkVersion
    buildToolsVersion = Versions.buildToolsVersion

    defaultConfig {
        minSdk = Versions.minSdkVersion
        targetSdk = Versions.targetSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
        }
    }

    signingConfigs {
        create("release") {
            // 如果需要打release包，请在项目根目录下自行添加此文件
            /**
             *  STORE_FILE=yourAppStroe.keystore
            STORE_PASSWORD=yourStorePwd
            KEY_ALIAS=yourKeyAlias
            KEY_PASSWORD=yourAliasPwd
             */
            val props = Properties()
            val propFile = File("signing.properties")
            if (propFile.exists()) {
                propFile.reader().use { reader ->
                    props.load(reader)
                }

                storeFile = file(props.getProperty("STORE_FILE"))
                storePassword = props.getProperty("STORE_PASSWORD")
                keyAlias = props.getProperty("KEY_ALIAS")
                keyPassword = props.getProperty("KEY_PASSWORD")

                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["release"]
        }
        getByName("debug") {
            isMinifyEnabled = false
            signingConfig = signingConfigs["release"]
        }
    }

    flavorDimensions("default")
    productFlavors {
        create("common") {
            dimension("default")
            if (buildFeatures.buildConfig) {
                buildConfigField("String", "FLAVOR", "\"common\"")
            }
        }
        create("google") {
            dimension("default")
            if (buildFeatures.buildConfig) {
                buildConfigField("String", "FLAVOR", "\"google\"")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeCompilerVersion
    }
}