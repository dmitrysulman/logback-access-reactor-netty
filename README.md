# Logback Access for Reactor Netty
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.dmitrysulman/logback-access-reactor-netty)](https://central.sonatype.com/artifact/io.github.dmitrysulman/logback-access-reactor-netty)
[![javadoc](https://javadoc.io/badge2/io.github.dmitrysulman/logback-access-reactor-netty/javadoc.svg)](https://javadoc.io/doc/io.github.dmitrysulman/logback-access-reactor-netty)
[![Build](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/build.yml)
[![CodeQL](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml/badge.svg)](https://github.com/dmitrysulman/logback-access-reactor-netty/actions/workflows/codeql.yml)
[![codecov](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty/graph/badge.svg?token=LOEJQ7K8Z7)](https://codecov.io/gh/dmitrysulman/logback-access-reactor-netty)

A library that integrates Logback Access with Reactor Netty HTTP server, providing comprehensive access logging capabilities.

## Overview

This library serves as a bridge between the Reactor Netty HTTP logging mechanism and the Logback Access library. It enables detailed HTTP access logging with configurable formats, filters, and appenders through Logback Access configuration.

## Features

- XML-based configuration support
- Comprehensive HTTP request/response logging
- Lazy-loaded access event properties for optimal performance
- Support for headers, cookies, and request parameters logging
- Debug mode for troubleshooting
- Configurable through system properties or external configuration files

## Dependencies

- Java 17+
- Kotlin Standard Library 2.1.20
- Logback-access 2.0.6
- Reactor Netty HTTP Server 1.2.6
- SLF4J 2.0.17

## Usage

### Adding dependency
#### Maven
```
<dependency>
    <groupId>io.github.dmitrysulman</groupId>
    <artifactId>logback-access-reactor-netty</artifactId>
    <version>1.0.1</version>
</dependency>
```
#### Gradle
```
implementation("io.github.dmitrysulman:logback-access-reactor-netty:1.0.1")
```

### Basic Setup
```java
ReactorNettyAccessLogFactory factory = new ReactorNettyAccessLogFactory();
HttpServer.create()
          .accessLog(true, factory)
          .bindNow()
          .onDispose()
          .block();
```

### Configuration

The library can be configured in several ways:

1. **Default configuration** uses `logback-access.xml` file in the classpath.
2. **System property.** Set `-Dlogback.access.reactor.netty.config` property to specify configuration file location.
3. **Programmatic configuration.** Provide configuration file filename or URL of the resource directly:
```java
// Using specific configuration file by the filename
ReactorNettyAccessLogFactory factory = new ReactorNettyAccessLogFactory("/path/to/logback-access.xml");

// Using specific configuration file as a classpath resource
ReactorNettyAccessLogFactory factory = new ReactorNettyAccessLogFactory(
        this.getClass().getClassLoader().getResource("custom-logback-access.xml")
);

// Enable debug mode
ReactorNettyAccessLogFactory factory = new ReactorNettyAccessLogFactory("logback-access.xml", new JoranConfigurator(), true);
```

### Spring Boot configuration
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


## Author

[Dmitry Sulman](https://www.linkedin.com/in/dmitrysulman/)

## See Also

- [Logback Access Documentation](https://logback.qos.ch/access.html)
- [Reactor Netty HTTP Server Documentation](https://projectreactor.io/docs/netty/release/reference/http-server.html)
