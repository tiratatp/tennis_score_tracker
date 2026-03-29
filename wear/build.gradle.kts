plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.paparazzi")
    id("com.github.triplet.play")
}

fun gitTagCount(): Int {
    val result =
        providers
            .exec {
                commandLine("git", "tag", "--list", "v*")
            }.standardOutput
            .asText
            .get()
            .trim()
    return if (result.isEmpty()) 1 else result.lines().size
}

fun gitVersionName(): String {
    val result =
        providers
            .exec {
                commandLine("git", "describe", "--tags", "--always")
            }.standardOutput
            .asText
            .get()
            .trim()
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
        versionCode = gitTagCount() * 100 + 2
        versionName = gitVersionName()
    }

    flavorDimensions += "sport"
    productFlavors {
        create("tennis") {
            dimension = "sport"
            applicationId = "com.nuttyknot.tennisscoretracker"
            buildConfigField("String", "CAPABILITY_PHONE_APP", "\"tennis_score_tracker_phone\"")
        }
        create("badminton") {
            dimension = "sport"
            applicationId = "com.nuttyknot.badmintonscoretracker"
            buildConfigField("String", "CAPABILITY_PHONE_APP", "\"badminton_score_tracker_phone\"")
        }
        create("pickleball") {
            dimension = "sport"
            applicationId = "com.nuttyknot.pickleballscoretracker"
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
                output.versionCode.set(gitTagCount() * 100 + sportOffset * 10 + 2)
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
    track.set("wear:internal")
    defaultToAppBundles.set(true)
    val saJson = System.getenv("PLAY_STORE_SERVICE_ACCOUNT_JSON")
    if (!saJson.isNullOrBlank()) {
        val saFile = layout.buildDirectory.file("service-account.json")
        serviceAccountCredentials.set(
            saFile.map { f ->
                f.also {
                    it.asFile.apply {
                        parentFile.mkdirs()
                        writeText(saJson)
                    }
                }
            },
        )
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.wear.compose:compose-material3:1.5.6")
    implementation("androidx.wear.compose:compose-foundation:1.5.6")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    implementation("androidx.wear:wear:1.4.0")
    implementation("androidx.wear:wear-ongoing:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
}

// Ensure linters run before the wear app is installed for debugging
tasks.whenTaskAdded {
    val match = Regex("install(Tennis|Badminton|Pickleball)Debug").matchEntire(name)
    if (match != null) {
        val flavor = match.groupValues[1]
        dependsOn("ktlintCheck", "lint${flavor}Debug", "detekt")
    }
}

tasks.register<Copy>("updateReadmeScreenshots") {
    dependsOn("recordPaparazziTennisDebug")
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
