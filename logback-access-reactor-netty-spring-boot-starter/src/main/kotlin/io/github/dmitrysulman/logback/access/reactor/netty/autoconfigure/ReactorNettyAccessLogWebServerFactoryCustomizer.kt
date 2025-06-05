package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer

/**
 * [WebServerFactoryCustomizer] of the [NettyReactiveWebServerFactory] for the Logback Access integration.
 */
class ReactorNettyAccessLogWebServerFactoryCustomizer(
    private val enableAccessLog: Boolean,
    private val reactorNettyAccessLogFactory: ReactorNettyAccessLogFactory,
) : WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
    override fun customize(factory: NettyReactiveWebServerFactory) {
        factory.addServerCustomizers(
            { server ->
                server.accessLog(
                    enableAccessLog,
                    reactorNettyAccessLogFactory,
                )
            },
        )
    }
}
