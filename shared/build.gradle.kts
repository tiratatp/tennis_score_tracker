plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nuttyknot.tennisscoretracker.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    flavorDimensions += "sport"
    productFlavors {
        create("tennis") { dimension = "sport" }
        create("badminton") { dimension = "sport" }
        create("pickleball") { dimension = "sport" }
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
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20231013")
}

tasks.whenTaskAdded {
    val match = Regex("assemble(Tennis|Badminton|Pickleball)Debug").matchEntire(name)
    if (match != null) {
        val flavor = match.groupValues[1]
        dependsOn("test${flavor}DebugUnitTest")
    }
}
