package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.github.dmitrysulman.logback.access.reactor.netty.joran.ReactorNettyJoranConfigurator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.util.ResourceUtils
import reactor.netty.http.server.HttpServer

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
    @ConditionalOnMissingBean
    fun reactorNettyAccessLogFactory(
        properties: LogbackAccessReactorNettyProperties,
        resourceLoader: ResourceLoader,
        environment: Environment,
    ) = ReactorNettyAccessLogFactory(
        getConfigUrl(properties, resourceLoader),
        ReactorNettyJoranConfigurator(environment),
        properties.debug ?: false,
    )

    @Bean
    fun reactorNettyAccessLogWebServerFactoryCustomize(
        reactorNettyAccessLogFactory: ReactorNettyAccessLogFactory,
    ): WebServerFactoryCustomizer<NettyReactiveWebServerFactory> =
        WebServerFactoryCustomizer { factory ->
            factory.addServerCustomizers(
                { server ->
                    server.accessLog(
                        true,
                        reactorNettyAccessLogFactory,
                    )
                },
            )
        }

    private fun getConfigUrl(
        properties: LogbackAccessReactorNettyProperties,
        resourceLoader: ResourceLoader,
    ) = properties.config?.let { ResourceUtils.getURL(it) }
        ?: getDefaultConfigurationResource(resourceLoader).url

    private fun getDefaultConfigurationResource(resourceLoader: ResourceLoader) =
        resourceLoader
            .getResource("${ResourceUtils.FILE_URL_PREFIX}${ReactorNettyAccessLogFactory.DEFAULT_CONFIG_FILE_NAME}")
            .takeIf { it.exists() }
            ?: resourceLoader
                .getResource("${ResourceUtils.CLASSPATH_URL_PREFIX}${ReactorNettyAccessLogFactory.DEFAULT_CONFIG_FILE_NAME}")
                .takeIf { it.exists() }
            ?: resourceLoader.getResource("${ResourceUtils.CLASSPATH_URL_PREFIX}${ReactorNettyAccessLogFactory.DEFAULT_CONFIGURATION}")
}
