plugins {
    id("conventions")
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

    testImplementation(libs.spring.boot.starter.test)
}
