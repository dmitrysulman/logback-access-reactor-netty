plugins {
    kotlin("jvm")
    alias(libs.plugins.jreleaser)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val jreleaserFullRelease by tasks.existing {
    subprojects.forEach {
        val copyStagingDeployToRoot by it.tasks.existing
        dependsOn(copyStagingDeployToRoot)
    }
}