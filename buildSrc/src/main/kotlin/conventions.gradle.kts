import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

group = "io.github.dmitrysulman"

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
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
    jvmToolchain(21)
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val copyStagingDeployToRoot by tasks.registering(Copy::class) {
    group = "publishing"
    dependsOn(tasks.publish)
    from("./build/staging-deploy")
    into("../build/staging-deploy")
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    group = "dokka"
    dependsOn(tasks.dokkaGeneratePublicationJavadoc)
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(dokkaJavadocJar)
            pom {
                afterEvaluate {
                    pom.name = project.description
                    pom.description = project.description
                }
                url = "https://github.com/dmitrysulman/logback-access-reactor-netty"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "dmitrysulman"
                        name = "Dmitry Sulman"
                        email = "dmitry.sulman@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/dmitrysulman/logback-access-reactor-netty.git"
                    developerConnection = "scm:git:ssh://github.com/dmitrysulman/logback-access-reactor-netty.git"
                    url = "https://github.com/dmitrysulman/logback-access-reactor-netty"
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}
