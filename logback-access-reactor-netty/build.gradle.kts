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

dependencies {
    implementation(libs.logbackAccessCommon)
    implementation(libs.reactorNettyHttp)

    testImplementation(libs.junitJupiter)

    testRuntimeOnly(libs.junitPlatformLauncher)
}
