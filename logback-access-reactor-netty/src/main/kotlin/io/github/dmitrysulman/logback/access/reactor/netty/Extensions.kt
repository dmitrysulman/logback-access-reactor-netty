package io.github.dmitrysulman.logback.access.reactor.netty

import reactor.netty.http.server.HttpServer

/**
 * Extension for [HttpServer] providing [HttpServer.accessLog] method alternative.
 *
 * @param reactorNettyAccessLogFactory The [ReactorNettyAccessLogFactory] instance for access log configuration.
 */
fun HttpServer.enableLogbackAccess(reactorNettyAccessLogFactory: ReactorNettyAccessLogFactory): HttpServer =
    accessLog(true, reactorNettyAccessLogFactory)
