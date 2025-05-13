plugins {
    kotlin("jvm")
    alias(libs.plugins.jreleaser)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

group = "io.github.dmitrysulman"

tasks.jreleaserFullRelease {
    subprojects.forEach {
        val copyStagingDeployToRoot by it.tasks.existing
        dependsOn(copyStagingDeployToRoot)
    }
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