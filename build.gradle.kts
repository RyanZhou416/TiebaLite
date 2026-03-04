// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.squareup.wire) apply false

    alias(libs.plugins.dependency.analysis) apply false
}

if (providers.gradleProperty("depAnalysis").orNull == "true") {
    pluginManager.apply("com.autonomousapps.dependency-analysis")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
