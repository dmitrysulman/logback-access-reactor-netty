plugins {
    id("conventions")
}

description = "Spring Boot Starter for Logback Access integration with Reactor Netty"

dependencies {
    api(project(":logback-access-reactor-netty"))

    implementation(libs.slf4j.api)
}