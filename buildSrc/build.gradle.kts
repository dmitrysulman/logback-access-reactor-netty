plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.jackson.bom))

    implementation(libs.dokka.gradlePlugin)
    implementation(libs.dokka.javadocPlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ktlintPlugin)
}