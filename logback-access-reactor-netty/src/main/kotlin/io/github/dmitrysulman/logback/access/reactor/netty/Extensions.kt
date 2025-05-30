package io.github.dmitrysulman.logback.access.reactor.netty

import reactor.netty.http.server.HttpServer
import java.net.URL

/**
 * Extension for [HttpServer] providing [HttpServer.accessLog] method alternative.
 *
 * @param reactorNettyAccessLogFactory The [ReactorNettyAccessLogFactory] instance for access log configuration.
 * @return a new [HttpServer].
 */
fun HttpServer.enableLogbackAccess(reactorNettyAccessLogFactory: ReactorNettyAccessLogFactory): HttpServer =
    accessLog(true, reactorNettyAccessLogFactory)

/**
 * Extension for [HttpServer] providing [HttpServer.accessLog] method alternative with a default
 * [ReactorNettyAccessLogFactory] configuration.
 *
 * @return a new [HttpServer].
 */
fun HttpServer.enableLogbackAccess(): HttpServer = enableLogbackAccess(ReactorNettyAccessLogFactory())

/**
 * Extension for [HttpServer] providing [HttpServer.accessLog] method alternative.
 *
 * @param fileName The file name of the configuration file to load.
 * @return a new [HttpServer].
 */
fun HttpServer.enableLogbackAccess(fileName: String): HttpServer = enableLogbackAccess(ReactorNettyAccessLogFactory(fileName))

/**
 * Extension for [HttpServer] providing [HttpServer.accessLog] method alternative.
 *
 * @param config the [URL] pointing to the configuration file to be used for setup.
 * @return a new [HttpServer].
 */
fun HttpServer.enableLogbackAccess(config: URL): HttpServer = enableLogbackAccess(ReactorNettyAccessLogFactory(config))
