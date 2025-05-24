package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.github.dmitrysulman.logback.access.reactor.netty.joran.ReactorNettyJoranConfigurator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.util.ResourceUtils
import reactor.netty.http.server.HttpServer
import java.net.URL

/**
 * [Auto-configuration][EnableAutoConfiguration] for a Logback Access Reactor Netty integration.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(HttpServer::class)
@ConditionalOnProperty(prefix = "logback.access.reactor.netty", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LogbackAccessReactorNettyProperties::class)
class ReactorNettyAccessLogFactoryAutoConfiguration {
    @Bean
    fun reactorNettyAccessLogWebServerFactoryCustomize(
        properties: LogbackAccessReactorNettyProperties,
        resourceLoader: ResourceLoader,
    ): WebServerFactoryCustomizer<NettyReactiveWebServerFactory> =
        WebServerFactoryCustomizer { factory ->
            factory.addServerCustomizers(
                { server ->
                    server.accessLog(
                        true,
                        ReactorNettyAccessLogFactory(
                            getConfigUrl(properties, resourceLoader),
                            ReactorNettyJoranConfigurator(),
                            properties.debug ?: false,
                        ),
                    )
                },
            )
        }

    private fun getConfigUrl(
        properties: LogbackAccessReactorNettyProperties,
        resourceLoader: ResourceLoader,
    ): URL {
//        return resourceLoader.getResource(properties.config ?: "logback-access.xml").url
        return ResourceUtils.getURL(properties.config ?: "logback-access.xml")
    }
}
