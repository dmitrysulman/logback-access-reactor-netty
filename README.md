# Logback Access for Reactor Netty
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.dmitrysulman/logback-access-reactor-netty)](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty)
[![javadoc](https://javadoc.io/badge2/io.github.dmitrysulman/logback-access-reactor-netty/javadoc.svg)](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty)
[![Build](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml)
[![CodeQL](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml)
[![codecov](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty/graph/badge.svg?token=LOEJQ7K8Z7)](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty)

A Java/Kotlin library that integrates Logback Access with Reactor Netty HTTP server, providing comprehensive access logging capabilities.

## Overview

**Reactor Netty HTTP Server** is a non-blocking, asynchronous server built on the Netty networking framework and used as the default runtime for handling HTTP requests in Spring WebFlux. It enables reactive, event-driven processing of web requests, making it well-suited for scalable and high-throughput applications. In Spring Boot, it's automatically configured when building reactive applications with the `spring-boot-starter-webflux` dependency.

**Logback Access** is a module of the Logback logging framework that provides HTTP access logging capabilities, similar to those in servlet containers like Tomcat or Jetty. It allows logging of incoming HTTP requests and responses using customizable patterns and supports easy configuration through an XML file.

**Logback Access for Reactor Netty** library serves as a bridge between the Reactor Netty HTTP logging mechanism and the Logback Access library. It enables detailed HTTP access logging with configurable formats, filters, and appenders through Logback Access configuration.

## Features

- XML-based configuration support
- Comprehensive HTTP request/response logging
- Lazy-loaded access event properties for optimal performance
- Support for headers, cookies, and request parameters logging
- Configurable through system properties or external configuration files
- Debug mode for troubleshooting

## Dependencies

- Java 17+
- Kotlin Standard Library 2.1.21
- Reactor Netty HTTP Server 1.2.6
- Logback-access 2.0.6
- SLF4J 2.0.17

## Usage

### Adding dependency

#### Maven

```
<dependency>
    <groupId>io.github.dmitrysulman</groupId>
    <artifactId>logback-access-reactor-netty</artifactId>
    <version>1.0.5</version>
</dependency>
```

#### Gradle

```
implementation("io.github.dmitrysulman:logback-access-reactor-netty:1.0.5")
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

1. **Default configuration** uses `logback-access.xml` file on the classpath.
2. **System property.** Set `-Dlogback.access.reactor.netty.config` property to specify configuration file location.
3. **Programmatic configuration.** Provide configuration file filename or URL of the resource directly:
```java
// Using specific configuration file by the filename
var factory = new ReactorNettyAccessLogFactory("/path/to/logback-access.xml");

// Using specific configuration file as a classpath resource
var factory = new ReactorNettyAccessLogFactory(
        this.getClass().getClassLoader().getResource("custom-logback-access.xml")
);
```

### Spring Boot configuration

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

## Documentation

- [Java API (Javadoc)](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty/latest/index.html)
- [Kotlin API (KDoc)](https://dmitrysulman.github.io/logback-access-reactor-netty/)

## Author

[Dmitry Sulman](https://www.linkedin.com/in/dmitrysulman/)

## See Also

- [Reactor Netty HTTP Server Documentation](https://projectreactor.io/docs/netty/release/reference/http-server.html)
- [Logback Access Documentation](https://logback.qos.ch/access.html)
