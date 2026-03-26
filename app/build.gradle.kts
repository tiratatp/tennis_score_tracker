plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.paparazzi")
    id("com.github.triplet.play")
}

fun gitTagCount(): Int {
    val result =
        providers.exec {
            commandLine("git", "tag", "--list", "v*")
        }.standardOutput.asText.get().trim()
    return if (result.isEmpty()) 1 else result.lines().size
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
        versionCode = gitTagCount() * 100 + 1
        versionName = gitVersionName()
    }

    flavorDimensions += "sport"
    productFlavors {
        create("tennis") {
            dimension = "sport"
            applicationId = "com.nuttyknot.tennisscoretracker"
            buildConfigField("String", "SPORT", "\"TENNIS\"")
            buildConfigField("String", "CAPABILITY_PHONE_APP", "\"tennis_score_tracker_phone\"")
        }
        create("badminton") {
            dimension = "sport"
            applicationId = "com.nuttyknot.badmintonscoretracker"
            buildConfigField("String", "SPORT", "\"BADMINTON\"")
            buildConfigField("String", "CAPABILITY_PHONE_APP", "\"badminton_score_tracker_phone\"")
        }
        create("pickleball") {
            dimension = "sport"
            applicationId = "com.nuttyknot.pickleballscoretracker"
            buildConfigField("String", "SPORT", "\"PICKLEBALL\"")
            buildConfigField("String", "CAPABILITY_PHONE_APP", "\"pickleball_score_tracker_phone\"")
        }
    }

    androidComponents {
        onVariants { variant ->
            val sportOffset =
                when {
                    variant.flavorName?.contains("tennis") == true -> 0
                    variant.flavorName?.contains("badminton") == true -> 1
                    variant.flavorName?.contains("pickleball") == true -> 2
                    else -> 0
                }
            variant.outputs.forEach { output ->
                output.versionCode.set(gitTagCount() * 100 + sportOffset * 10 + 1)
            }
        }
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
        buildConfig = true
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

play {
    track.set("internal")
    defaultToAppBundles.set(true)
}

dependencies {
    implementation("androidx.core:core-splashscreen:1.0.1")
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
tasks.register("installAll") {
    dependsOn("installTennisDebug", "installBadmintonDebug", "installPickleballDebug")
    group = "application"
    description = "Installs all flavor debug APKs"
}
tasks.register("testDebugUnitTest") {
    dependsOn("testTennisDebugUnitTest", "testBadmintonDebugUnitTest", "testPickleballDebugUnitTest")
    group = "verification"
    description = "Runs unit tests for all flavor debug builds"
}

// Ensure tests and linters run before the app is installed for debugging
tasks.whenTaskAdded {
    val match = Regex("install(Tennis|Badminton|Pickleball)Debug").matchEntire(name)
    if (match != null) {
        val flavor = match.groupValues[1]
        dependsOn("test${flavor}DebugUnitTest", "ktlintCheck", "lint${flavor}Debug", "detekt")
    }
}

android.productFlavors.forEach { flavor ->
    tasks.register<Copy>("update${flavor.name.replaceFirstChar { it.uppercase() }}Screenshots") {
        dependsOn("recordPaparazzi${flavor.name.replaceFirstChar { it.uppercase() }}Debug")
        from("src/test/snapshots/images") {
            include("*ScoreLandscape*_matchover.png")
            include("*ScorePortrait*_inmatch.png")
            include("*HelpScreenshotTest*_help.png")
            include("*MatchSummaryPortrait*_matchover.png")
            include("*Tablet7*_inmatch.png")
            include("*Tablet10*_inmatch.png")
            include("*SettingsPortrait*_defaultsettings.png")
            include("*SettingsTablet10*_defaultsettings.png")
            include("*HelpTablet10*_help.png")
            rename(".*ScoreLandscape.*_matchover\\.png", "score-landscape.png")
            rename(".*ScorePortrait.*_inmatch\\.png", "score-portrait.png")
            rename(".*HelpScreenshotTest.*_help\\.png", "help.png")
            rename(".*MatchSummaryPortrait.*_matchover\\.png", "match-summary.png")
            rename(".*Tablet7.*_inmatch\\.png", "score-tablet-7.png")
            rename(".*Tablet10.*_inmatch\\.png", "score-tablet-10.png")
            rename(".*SettingsPortrait.*_defaultsettings\\.png", "settings.png")
            rename(".*SettingsTablet10.*_defaultsettings\\.png", "settings-tablet-10.png")
            rename(".*HelpTablet10.*_help\\.png", "help-tablet-10.png")
        }
        into("${rootProject.projectDir}/screenshots/${flavor.name}")
    }
}
