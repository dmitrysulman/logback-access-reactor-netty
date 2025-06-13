# Logback Access for Reactor Netty
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.dmitrysulman/logback-access-reactor-netty)](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty)
[![javadoc](https://javadoc.io/badge2/io.github.dmitrysulman/logback-access-reactor-netty/javadoc.svg)](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty)
[![Build](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml)
[![CodeQL](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml)
[![codecov](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty/graph/badge.svg?token=LOEJQ7K8Z7)](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty)

A Java/Kotlin library and Spring Boot Starter that integrates Logback Access with Reactor Netty HTTP server, providing comprehensive access logging capabilities for reactive web applications.

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

## Contents:

- [Using as a Spring Boot Starter](#using-as-a-spring-boot-starter)
   - [Adding dependency to your project](#adding-dependency-to-your-project) 
- [Using as a standalone library](#using-as-a-standalone-library)
   - [Adding dependency to your project](#adding-dependency-to-your-project-1)

## Using as a Spring Boot Starter

### Adding dependency to your project

The Spring Boot Starter is published on [Maven Central](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty-spring-boot-starter). To add the dependency, use the following snippet according to your build system:

#### Maven
```
<dependency>
    <groupId>io.github.dmitrysulman</groupId>
    <artifactId>logback-access-reactor-netty-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

#### Gradle

```
implementation("io.github.dmitrysulman:logback-access-reactor-netty-spring-boot-starter:1.1.0")
```

### Configuration

#### Java

```java
@Configuration
public class NettyAccessLogConfiguration {
    @Bean
    public NettyServerCustomizer accessLogNettyServerCustomizer() {
        return (server) ->
                server.accessLog(true, new ReactorNettyAccessLogFactory("path/to/your/logback-access.xml"));
    }
}
```

#### Kotlin
```kotlin
@Configuration
class NettyAccessLogConfiguration {
    @Bean
    fun accessLogNettyServerCustomizer() = 
        NettyServerCustomizer { server ->
            server.enableLogbackAccess(ReactorNettyAccessLogFactory("path/to/your/logback-access.xml"))
        }
}
```
See [enableLogbackAccess()](https://dmitrysulman.github.io/logback-access-reactor-netty/logback-access-reactor-netty/io.github.dmitrysulman.logback.access.reactor.netty/enable-logback-access.html) extension function documentation.

### Dependencies

- Java 17+
- Kotlin Standard Library 2.1.21
- Spring Boot Starter WebFlux 3.4.6+ (should be explicitly provided)
- Logback-access 2.0.6
- SLF4J 2.0.17

## Using as a standalone library

### Adding dependency to your project

The library is published on [Maven Central](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty). To add the dependency, use the following snippet according to your build system: 

#### Maven

```
<dependency>
    <groupId>io.github.dmitrysulman</groupId>
    <artifactId>logback-access-reactor-netty</artifactId>
    <version>1.1.0</version>
</dependency>
```

##### Gradle

```
implementation("io.github.dmitrysulman:logback-access-reactor-netty:1.1.0")
```

### Basic Setup

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

```kotlin
val factory = ReactorNettyAccessLogFactory()
HttpServer.create()
          .enableLogbackAccess(factory)
          .bindNow()
          .onDispose()
          .block()
```

### Configuration

The library can be configured in several ways:

1. **Default configuration** uses the `logback-access.xml` file from the classpath or the current directory, with a fallback to the [Common Log Format](https://en.wikipedia.org/wiki/Common_Log_Format).
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

- [Java API (Javadoc)](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty/latest/index.html)
- [Kotlin API (KDoc)](https://dmitrysulman.github.io/logback-access-reactor-netty/)

## Author

[Dmitry Sulman](https://www.linkedin.com/in/dmitrysulman/)

## See Also

- [Reactor Netty HTTP Server Documentation](https://projectreactor.io/docs/netty/release/reference/http-server.html)
- [Logback Access Documentation](https://logback.qos.ch/access.html)