plugins {
    id("conventions")
}

dependencies {
    implementation(libs.logback.access.common)
    implementation(libs.reactorNetty.http)
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.logback.classic)
    testImplementation(libs.logback.core)

    testRuntimeOnly(libs.junit.platformLauncher)
}
