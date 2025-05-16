plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

dependencies {
    // To make libs accessible from conventions.gradle.kts
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // Override the old Jackson version that comes with dokka
    // to fix READ_DATE_TIMESTAMPS_AS_NANOSECONDS on Jreleaser tasks
    // https://github.com/dependency-check/DependencyCheck/issues/6192
    implementation(platform(libs.jackson.bom))

    implementation(libs.dokka.javadoc.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.kotlin.plugin)
    implementation(libs.ktlint.plugin)
}