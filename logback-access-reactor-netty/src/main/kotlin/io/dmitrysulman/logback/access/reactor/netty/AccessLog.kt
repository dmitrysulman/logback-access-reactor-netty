package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.access.common.spi.AccessEvent
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLog as ReactorAccessLog

class AccessLog(
    private val accessContext: AccessContext,
    argProvider: AccessLogArgProvider
) : ReactorAccessLog("") {

    private val accessEvent = AccessEvent(
        accessContext,
        HttpServletRequestAdapter(argProvider),
        HttpServletResponseAdapter(argProvider),
        ReactorNettyServerAdapter(argProvider)
    )

    override fun log() {
        // TODO elapsedTime and seconds
        accessContext.callAppenders(accessEvent)
    }
}