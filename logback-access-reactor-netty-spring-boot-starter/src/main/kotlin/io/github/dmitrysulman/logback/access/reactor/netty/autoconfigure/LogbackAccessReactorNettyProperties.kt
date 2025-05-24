package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("logback.access.reactor.netty")
class LogbackAccessReactorNettyProperties(
    /**
     * Enable Logback Access Reactor Netty auto-configuration
     */
    val enabled: Boolean?,
    /**
     * Config file name
     */
    val config: String?,
    /**
     * Enable debug mode
     */
    val debug: Boolean?,
)
