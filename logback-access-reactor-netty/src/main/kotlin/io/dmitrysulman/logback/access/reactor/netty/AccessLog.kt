package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.LoggerFactory
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLog as ReactorAccessLog

class AccessLog(
    private val accessContext: AccessContext,
    private val argProvider: AccessLogArgProvider,
) : ReactorAccessLog("") {

    private val logger = LoggerFactory.getLogger(AccessLog::class.java)

    override fun log() {
        try {
            val accessEvent = AccessEvent(argProvider, accessContext)
            accessEvent.threadName = Thread.currentThread().name
            if (accessContext.getFilterChainDecision(accessEvent) != FilterReply.DENY) {
                accessContext.callAppenders(accessEvent)
            }
            logger.info("Test123")
        } catch (e: Exception) {
            logger.error("Failed to log access event: {}", e.message)
        }
    }
}