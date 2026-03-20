plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.paparazzi")
}

fun gitVersionCode(): Int {
    val result =
        providers.exec {
            commandLine("git", "tag", "--list", "v*")
        }.standardOutput.asText.get().trim()
    val base = if (result.isEmpty()) 1 else result.lines().size
    return base * 10 + 2 // wear suffix
}

fun gitVersionName(): String {
    val result =
        providers.exec {
            commandLine("git", "describe", "--tags", "--always")
        }.standardOutput.asText.get().trim()
    return result.removePrefix("v").ifEmpty { "0.1" }
}

android {
    namespace = "com.nuttyknot.tennisscoretracker.wear"
    compileSdk = 35

    val keystoreFilePath = System.getenv("KEYSTORE_FILE")
    signingConfigs {
        if (keystoreFilePath != null && file(keystoreFilePath).exists()) {
            create("release") {
                storeFile = file(keystoreFilePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "com.nuttyknot.tennisscoretracker"
        minSdk = 30
        // targetSdk 35 breaks Paparazzi 1.3.5 screenshot tests
        // (NoSuchMethodError: StaticLayout$Builder.setUseBoundsForWidth)
        targetSdk = 34
        versionCode = gitVersionCode()
        versionName = gitVersionName()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                try {
                    signingConfigs.getByName("release")
                } catch (_: UnknownDomainObjectException) {
                    signingConfigs.getByName("debug")
                }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes +=
                setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/DEPENDENCIES",
                    "/META-INF/LICENSE*",
                    "/META-INF/NOTICE*",
                    "/META-INF/*.kotlin_module",
                    "/kotlin/**",
                    "/DebugProbesKt.bin",
                    "/*.properties",
                )
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.wear.compose:compose-material3:1.0.0-alpha32")
    implementation("androidx.wear.compose:compose-foundation:1.4.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    implementation("androidx.wear:wear:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}

// Ensure linters run before the wear app is installed for debugging
tasks.whenTaskAdded {
    if (name == "installDebug") {
        dependsOn("ktlintCheck", "lintDebug", "detekt")
    }
}

tasks.register<Copy>("updateReadmeScreenshots") {
    dependsOn("recordPaparazziDebug")
    from("src/test/snapshots/images") {
        include("*_watch.png")
        rename(".*_watch\\.png", "watch.png")
    }
    from("src/test/snapshots/images") {
        include("*_help.png")
        rename(".*_help\\.png", "watch-help.png")
    }
    from("src/test/snapshots/images") {
        include("*_matchover.png")
        rename(".*_matchover\\.png", "watch-match-win.png")
    }
    into("${rootProject.projectDir}/screenshots")
}
