plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spring.io/milestone")
    }
    maven {
        url = uri("https://repo.spring.io/snapshot")
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations.all {
    // Check for updates every build
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    implementation(libs.logbackAccessCommon)
    implementation(libs.reactorNettyHttp)
    implementation(libs.slf4jApi)

    testImplementation(libs.junitJupiter)

    testRuntimeOnly(libs.junitPlatformLauncher)
}
