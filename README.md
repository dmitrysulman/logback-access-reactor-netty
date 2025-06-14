# Logback Access for Reactor Netty
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.dmitrysulman/logback-access-reactor-netty-spring-boot-starter)](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty-spring-boot-starter)
[![javadoc](https://javadoc.io/badge2/io.github.dmitrysulman/logback-access-reactor-netty-spring-boot-starter/javadoc.svg)](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty-spring-boot-starter)
[![Build](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml)
[![CodeQL](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml)
[![codecov](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty/graph/badge.svg?token=LOEJQ7K8Z7)](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty)

A Java/Kotlin library and Spring Boot Starter that integrates Logback Access with Reactor Netty HTTP server, providing comprehensive access logging capabilities for reactive web applications.

## Contents:

- [Overview](#overview)
- [Features](#features)
- [Usage](#usage)
- [Using as a Spring Boot Starter](#using-as-a-spring-boot-starter)
  - [Adding Spring Boot Starter to your project](#adding-spring-boot-starter-to-your-project)
  - [Configuration](#configuration)
    - [Application properties](#application-properties)
    - [Profile-specific configuration](#profile-specific-configuration)
  - [Dependencies](#dependencies)
- [Using as a standalone library](#using-as-a-standalone-library)
  - [Adding dependency to your project](#adding-dependency-to-your-project)
  - [Basic setup](#basic-setup)
  - [Customize Logback Access configuration](#customize-logback-access-configuration)
  - [Dependencies](#dependencies-1)
- [API documentation](#api-documentation)
- [See also](#see-also)

## Overview

**Reactor Netty HTTP Server** is a non-blocking, asynchronous server built on the Netty networking framework and used as the default runtime for handling HTTP requests in Spring WebFlux and Spring Cloud Gateway. It enables reactive, event-driven processing of web requests, making it well-suited for scalable and high-throughput applications. In Spring Boot, it's automatically configured when building reactive applications with the `spring-boot-starter-webflux` dependency.

**Logback Access** is a module of the Logback logging library that provides HTTP access logging capabilities for servlet containers like Tomcat or Jetty. It allows logging of incoming HTTP requests and responses using customizable patterns and supports easy configuration through an XML file.

**Logback Access for Reactor Netty** library serves as a bridge between the Reactor Netty HTTP logging mechanism and the Logback Access library. It enables detailed HTTP access logging with configurable formats, filters, and appenders through Logback Access XML configuration.

## Features

- Spring Boot Starter with auto-configuration
- XML-based configuration support
- Comprehensive HTTP request/response logging
- Lazy-loaded access event properties for optimal performance
- Support for headers, cookies, and request parameters logging
- Configurable through system properties or external configuration files
- Debug mode for troubleshooting

## Usage

The Logback Access integration with Reactor Netty can be used in two ways:

1. As a Spring Boot Starter for reactive Spring Boot applications based on `spring-boot-starter-webflux`.
2. As a standalone library for applications using Reactor Netty HTTP Server directly.

## Using as a Spring Boot Starter

### Adding Spring Boot Starter to your project

The Spring Boot Starter is published on [Maven Central](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty-spring-boot-starter). To add the dependency, use the following snippet according to your build system:

#### Gradle

```kotlin
implementation("io.github.dmitrysulman:logback-access-reactor-netty-spring-boot-starter:1.1.0")
```

#### Maven

```xml
<dependency>
    <groupId>io.github.dmitrysulman</groupId>
    <artifactId>logback-access-reactor-netty-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Configuration

Default Spring Boot auto-configuration uses the `logback-access.xml` file from the current directory or the classpath, with a fallback to the [Common Log Format](https://en.wikipedia.org/wiki/Common_Log_Format).

#### Application properties

Several application properties can be specified inside `application.properties` file or `application.yaml` file, or as command line arguments:

| Name                                   | Description                                             | Default Value        |
|:---------------------------------------|:--------------------------------------------------------|:---------------------|
| `logback.access.reactor.netty.enabled` | Enable Logback Access Reactor Netty auto-configuration. | `true`               |
| `logback.access.reactor.netty.config`  | Config file name.                                       | `logback-access.xml` |
| `logback.access.reactor.netty.debug`   | Enable debug mode.                                      | `false`              |

#### Profile-specific configuration

The `<springProfile>` tag allows you to conditionally include or exclude parts of the configuration based on the active Spring profiles. You can use it anywhere within the `<configuration>` element. Specify the applicable profile using the `name` attribute, which can be either a single profile name (e.g., `staging`) or a profile expression. For more details, see the [Spring Boot Logback Extensions Profile-specific Configuration reference guide](https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.logback-extensions.profile-specific), which describes the same usage. There are several examples:

```xml
<springProfile name="staging">
	<!-- configuration to be enabled when the "staging" profile is active -->
</springProfile>

<springProfile name="dev | staging">
	<!-- configuration to be enabled when the "dev" or "staging" profiles are active -->
</springProfile>

<springProfile name="!production">
	<!-- configuration to be enabled when the "production" profile is not active -->
</springProfile>
```

### Dependencies

- Java 17+
- Kotlin Standard Library 2.1.21
- Spring Boot Starter WebFlux 3.4.6+ (should be explicitly provided)
- Logback-access 2.0.6
- SLF4J 2.0.17

## Using as a standalone library

### Adding dependency to your project

The library is published on [Maven Central](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty). To add the dependency, use the following snippet according to your build system: 

##### Gradle

```kotlin
implementation("io.github.dmitrysulman:logback-access-reactor-netty:1.1.0")
```

#### Maven

```xml
<dependency>
    <groupId>io.github.dmitrysulman</groupId>
    <artifactId>logback-access-reactor-netty</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Basic Setup

To enable Logback Access integration with Reactor Netty, create a new instance of `ReactorNettyAccessLogFactory` and pass it to the `HttpServer.accessLog()` method. 

#### Java

```java
ReactorNettyAccessLogFactory factory = new ReactorNettyAccessLogFactory();
HttpServer.create()
          .accessLog(true, factory)
          .bindNow()
          .onDispose()
          .block();
```

#### Kotlin

For Kotlin, a convenient [enableLogbackAccess()](https://dmitrysulman.github.io/logback-access-reactor-netty/logback-access-reactor-netty/io.github.dmitrysulman.logback.access.reactor.netty/enable-logback-access.html) extension function is provided to pass the factory instance.

```kotlin
val factory = ReactorNettyAccessLogFactory()
HttpServer.create()
          .enableLogbackAccess(factory)
          .bindNow()
          .onDispose()
          .block()
```

### Customize Logback Access configuration

The library can be configured in several ways:

1. **Default configuration** uses the `logback-access.xml` file from the current directory or the classpath, with a fallback to the [Common Log Format](https://en.wikipedia.org/wiki/Common_Log_Format).
2. **System property.** Set `-Dlogback.access.reactor.netty.config` property to specify configuration file location.
3. **Programmatic configuration.** Provide configuration file filename or URL of the classpath resource directly:

```java
// Using specific configuration file by the filename
var factory = new ReactorNettyAccessLogFactory("/path/to/logback-access.xml");

// Using specific configuration file as a classpath resource
var factory = new ReactorNettyAccessLogFactory(
        this.getClass().getClassLoader().getResource("custom-logback-access.xml")
);
```

### Dependencies

- Java 17+
- Kotlin Standard Library 2.1.21
- Reactor Netty HTTP Server 1.2.6+ (should be explicitly provided)
- Logback-access 2.0.6
- SLF4J 2.0.17

## API documentation

- [Java API (Javadoc) - Spring Boot Starter](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty-spring-boot-starter/latest/index.html)
- [Java API (Javadoc) - Standalone library](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty/latest/index.html)
- [Kotlin API (KDoc)](https://dmitrysulman.github.io/logback-access-reactor-netty/)

## See Also

- [Reactor Netty HTTP Server Documentation](https://projectreactor.io/docs/netty/release/reference/http-server.html)
- [Logback Access Documentation](https://logback.qos.ch/access.html)

## Author

[Dmitry Sulman](https://www.linkedin.com/in/dmitrysulman/)