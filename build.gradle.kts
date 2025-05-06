plugins {
    kotlin("jvm")
    alias(libs.plugins.jreleaser)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

tasks.jreleaserFullRelease {
    subprojects.forEach {
        val copyStagingDeployToRoot by it.tasks.existing
        dependsOn(copyStagingDeployToRoot)
    }
}
