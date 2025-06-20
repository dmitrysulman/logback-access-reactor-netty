package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * [@ConfigurationProperties][ConfigurationProperties] for the Logback Access integration with Reactor Netty.
 *
 * @author Dmitry Sulman
 * @see ReactorNettyAccessLogFactoryAutoConfiguration
 */
@ConfigurationProperties("logback.access.reactor.netty")
class ReactorNettyAccessLogProperties {
    /**
     * Enable Logback Access Reactor Netty auto-configuration.
     */
    var enabled: Boolean? = null

    /**
     * Config file name.
     */
    var config: String? = null

    /**
     * Enable debug mode.
     */
    var debug: Boolean? = null
}
