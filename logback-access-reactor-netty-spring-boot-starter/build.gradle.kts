plugins {
    alias(libs.plugins.conventions)
    alias(libs.plugins.kotlin.springPlugin)
    kotlin("kapt")
}

description = "Spring Boot Starter for Logback Access integration with Reactor Netty"

dependencies {
    api(project(":logback-access-reactor-netty"))

    implementation(libs.spring.boot.starter)
    implementation(libs.slf4j.api)

    provided(libs.spring.boot.starter.reactorNetty)

    kapt(libs.spring.boot.autoconfigureProcessor)
    kapt(libs.spring.boot.configurationProcessor)

    testImplementation(libs.assertj.core)
    testImplementation(libs.kotest.assertions.core.jvm)
    testImplementation(libs.mockk)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webflux)

    testRuntimeOnly(libs.junit.platformLauncher)
}
