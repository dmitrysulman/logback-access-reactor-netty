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
    implementation(libs.dokka.gradlePlugin)
    implementation(libs.dokka.javadocPlugin)
    implementation(libs.kotlin.gradlePlugin)
}