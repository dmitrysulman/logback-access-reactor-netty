plugins {
    id("conventions")
}

description = "Logback Access integration with Reactor Netty"

dependencies {
    implementation(libs.logback.access.common)
    implementation(libs.reactorNetty.http)
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotest.assertions.core.jvm)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(libs.logback.classic)
    testImplementation(libs.logback.core)
    testImplementation(libs.mockk)

    testRuntimeOnly(libs.junit.platformLauncher)
}
