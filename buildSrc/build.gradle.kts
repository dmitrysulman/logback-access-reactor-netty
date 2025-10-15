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

    implementation(libs.dokka.javadoc.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.kotlin.plugin)
    implementation(libs.ktlint.plugin)
}