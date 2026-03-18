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
    return base * 10 + 1 // phone suffix
}

fun gitVersionName(): String {
    val result =
        providers.exec {
            commandLine("git", "describe", "--tags", "--always")
        }.standardOutput.asText.get().trim()
    return result.removePrefix("v").ifEmpty { "0.1" }
}

android {
    namespace = "com.nuttyknot.tennisscoretracker"
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
        minSdk = 26
        targetSdk = 35
        versionCode = gitVersionCode()
        versionName = gitVersionName()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-text-google-fonts")

    implementation("androidx.navigation:navigation-compose:2.8.9")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Shared module
    implementation(project(":shared"))

    // Wear OS Data Layer
    implementation("com.google.android.gms:play-services-wearable:19.0.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
tasks.register<Exec>("run") {
    dependsOn("installDebug")
    group = "application"
    description = "Installs and runs the application"
    commandLine("adb", "shell", "am", "start", "-n", "${android.namespace}/.MainActivity")
}

// Ensure tests and linters run before the app is installed for debugging
tasks.whenTaskAdded {
    if (name == "installDebug") {
        dependsOn("testDebugUnitTest", "ktlintCheck", "lintDebug", "detekt")
    }
}

tasks.register<Copy>("updateReadmeScreenshots") {
    dependsOn("recordPaparazziDebug")
    from("src/test/snapshots/images") {
        include("*ScoreLandscape*_matchover.png")
        include("*Portrait*_inmatch.png")
        include("*_help.png")
        include("*MatchSummaryPortrait*_matchover.png")
        include("*Tablet7*_inmatch.png")
        include("*Tablet10*_inmatch.png")
        rename(".*ScoreLandscape.*_matchover\\.png", "score-landscape.png")
        rename(".*Portrait.*_inmatch\\.png", "score-portrait.png")
        rename(".*_help\\.png", "help.png")
        rename(".*MatchSummaryPortrait.*_matchover\\.png", "match-summary.png")
        rename(".*Tablet7.*_inmatch\\.png", "score-tablet-7.png")
        rename(".*Tablet10.*_inmatch\\.png", "score-tablet-10.png")
    }
    into("${rootProject.projectDir}/screenshots")
}
