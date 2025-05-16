plugins {
    kotlin("jvm")
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.jreleaser)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

group = "io.github.dmitrysulman"

dependencies {
    dokka(project("logback-access-reactor-netty"))
}

tasks.jreleaserFullRelease {
    subprojects.forEach {
        val copyStagingDeployToRoot by it.tasks.existing
        dependsOn(copyStagingDeployToRoot)
    }
}

tasks.jar {
    enabled = false
}

jreleaser {
    release {
        github {
            skipTag = true
            skipRelease = true
            token = "-"
        }
    }
    signing {
        setActive("ALWAYS")
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive("ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    applyMavenCentralRules = true
                    checksums = false
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}