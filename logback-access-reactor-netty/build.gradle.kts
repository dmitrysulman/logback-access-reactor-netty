plugins {
    id("conventions")
}

description = "Logback Access integration with Reactor Netty"

dependencies {
    implementation(libs.logback.access.common)
    implementation(libs.reactorNetty.http)
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.logback.classic)
    testImplementation(libs.logback.core)

    testRuntimeOnly(libs.junit.platformLauncher)

    implementation("com.hierynomus:sshj:0.39.0")
}
