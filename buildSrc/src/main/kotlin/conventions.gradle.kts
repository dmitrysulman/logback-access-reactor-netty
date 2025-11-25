import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    id("org.jlleitschuh.gradle.ktlint")
    jacoco
    `maven-publish`
}

// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()

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
    jvmToolchain(libs.versions.java.get().toInt())
}

java {
    withSourcesJar()
}

val provided: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = false
}

configurations {
    compileClasspath {
        extendsFrom(provided)
    }
    runtimeClasspath {
        extendsFrom(provided)
    }
    testCompileClasspath {
        extendsFrom(provided)
    }
    testRuntimeClasspath {
        extendsFrom(provided)
    }
}

tasks.build {
    dependsOn(tasks.check)
    dependsOn(tasks.jacocoTestReport)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
    }
}

tasks.withType<Test> {
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

dokka {
    dokkaSourceSets.configureEach {
        includes.from("./src/main/resources/dokka/Module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/dmitrysulman/logback-access-reactor-netty/tree/main/${project.name}/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }
        externalDocumentationLinks {
            register("reactor-netty-docs") {
                url("https://projectreactor.io/docs/netty/release/api/")
                packageListUrl("https://projectreactor.io/docs/netty/release/api/element-list")
            }
            register("logback-access-docs") {
                url("https://javadoc.io/doc/ch.qos.logback.access/logback-access-common/latest/")
                packageListUrl("https://javadoc.io/doc/ch.qos.logback.access/logback-access-common/latest/element-list")
            }
            register("logback-core-docs") {
                url("https://javadoc.io/doc/ch.qos.logback/logback-core/latest/")
                packageListUrl("https://javadoc.io/doc/ch.qos.logback/logback-core/latest/element-list")
            }
            register("spring-framework-docs") {
                url("https://docs.spring.io/spring-framework/docs/current/javadoc-api/")
                packageListUrl("https://docs.spring.io/spring-framework/docs/current/javadoc-api/element-list")
            }
            register("spring-boot-docs") {
                url("https://docs.spring.io/spring-boot/api/java/")
                packageListUrl("https://docs.spring.io/spring-boot/api/java/element-list")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
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

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

configure<KtlintExtension> {
    version = libs.versions.ktlint
    additionalEditorconfig.set(
        mapOf(
            "ktlint_standard_backing-property-naming" to "disabled"
        )
    )
}
