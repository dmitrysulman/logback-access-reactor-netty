plugins {
    `kotlin-dsl`
    alias(libs.plugins.jreleaser)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
}

val copyStagingDeploy by tasks.registering(Copy::class) {
    from("../logback-access-reactor-netty/build/staging-deploy")
    into("build/staging-deploy")
}

tasks.jreleaserFullRelease {
    dependsOn(copyStagingDeploy)
}