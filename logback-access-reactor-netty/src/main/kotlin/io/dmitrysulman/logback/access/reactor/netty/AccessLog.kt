package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.status.ErrorStatus
import org.slf4j.LoggerFactory
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLog as ReactorAccessLog

class AccessLog(
    private val accessContext: AccessContext,
    private val argProvider: AccessLogArgProvider,
) : ReactorAccessLog("") {

    override fun log() {
        try {
            val accessEvent = AccessEvent(argProvider, accessContext)
            accessEvent.threadName = Thread.currentThread().name
            if (accessContext.getFilterChainDecision(accessEvent) != FilterReply.DENY) {
                accessContext.callAppenders(accessEvent)
            }
        } catch (e: Exception) {
            accessContext.statusManager.add(ErrorStatus("Failed to log access event", this, e))
            logger.error("Failed to log access event: {}", e.message)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(AccessLog::class.java)
    }
}