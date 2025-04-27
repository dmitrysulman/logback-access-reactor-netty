package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLog as ReactorAccessLog

class AccessLog(
    private val accessContext: AccessContext,
    argProvider: AccessLogArgProvider
) : ReactorAccessLog("") {

    private val accessEvent = AccessEvent(argProvider, accessContext)

    override fun log() {
        // TODO add something to status manager?
        accessEvent.threadName = Thread.currentThread().name
        accessContext.callAppenders(accessEvent)
    }
}