plugins {
    id("conventions")
}

dependencies {
    implementation(libs.logbackAccessCommon)
    implementation(libs.reactorNettyHttp)
    implementation(libs.slf4jApi)

    testImplementation(libs.junitJupiter)
    testImplementation(libs.logbackClassic)

    testRuntimeOnly(libs.junitPlatformLauncher)
}
