// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" apply false
    id("app.cash.paparazzi") version "1.3.5" apply false
    id("com.github.triplet.play") version "3.13.0" apply false
}

tasks.register("installGitHook") {
    val hookFile = file(".git/hooks/pre-commit")
    val scriptFile = file("scripts/pre-commit")
    outputs.file(hookFile)
    inputs.file(scriptFile)
    doLast {
        if (hookFile.exists()) hookFile.delete()
        val relative = hookFile.parentFile.toPath().relativize(scriptFile.toPath())
        Runtime.getRuntime().exec(arrayOf("ln", "-s", relative.toString(), hookFile.absolutePath)).waitFor()
    }
}

// Run installGitHook automatically on first build
tasks.matching { it.name == "prepareKotlinBuildScriptModel" }.configureEach {
    dependsOn("installGitHook")
}

tasks.register("updateReadmeScreenshots") {
    dependsOn(":app:updateReadmeScreenshots", ":wear:updateReadmeScreenshots")
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    detekt {
        buildUponDefaultConfig = true // preconfigure defaults
        allRules = false // activate all available (even unstable) rules.
    }

}
