plugins {
    kotlin("jvm")
    alias(libs.plugins.dokka)
    alias(libs.plugins.jreleaser)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

group = "io.github.dmitrysulman"

dependencies {
    dokka(project("logback-access-reactor-netty"))
    dokka(project("logback-access-reactor-netty-spring-boot-starter"))
}

dokka {
    dokkaPublications.html {
        moduleName.set("Logback Access for Reactor Netty")
    }
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

tasks.dokkaGeneratePublicationHtml {
    finalizedBy(createGoogleVerificationFile, addTitleToDokka)
}

val createGoogleVerificationFile by tasks.registering {
    doLast {
        File("./build/dokka/html/google2faf2af66cb652f4.html").writeText("google-site-verification: google2faf2af66cb652f4.html")
    }
}

val addTitleToDokka by tasks.registering {
    doLast {
        val indexHtml = file("./build/dokka/html/index.html")
        indexHtml.readText()
            .replaceFirst("<title>All modules</title>", "<title>Logback Access for Reactor Netty Spring Boot Starter and Java/Kotlin library</title>")
            .let {
                indexHtml.writeText(it)
            }
    }
}

jreleaser {
    release {
        github {
            changelog {
                enabled = false
            }
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