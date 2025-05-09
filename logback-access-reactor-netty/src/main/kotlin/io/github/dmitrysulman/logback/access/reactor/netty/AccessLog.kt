package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.access.common.spi.IAccessEvent
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.status.ErrorStatus
import org.slf4j.LoggerFactory
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLog as ReactorAccessLog

/**
 * Represents a logger for Logback Access [IAccessEvent] events within a given [AccessContext] and [AccessLogArgProvider].
 *
 * This class is a bridge between the Reactor Netty HTTP logging mechanism and the Logback Access library. It is responsible
 * for handling and logging access events based on the configured access context and argument provider. It extends Reactor
 * Netty [AccessLog][ReactorAccessLog], providing an implementation of the [log] method to capture and process
 * access-related information.
 *
 * @constructor Initializes an [AccessLog] instance with the given [AccessContext] and [AccessLogArgProvider].
 * The [AccessContext] facilitates access logging within the specified context.
 * The [AccessLogArgProvider] supplies information about the HTTP request and response to populate the event's details.
 *
 * @author Dmitry Sulman
 * @see ReactorAccessLog
 * @see AccessEvent
 * @see ReactorNettyAccessLogFactory
 */
class AccessLog(
    private val accessContext: AccessContext,
    private val argProvider: AccessLogArgProvider,
) : ReactorAccessLog("") {
    /**
     * Logs an access event by creating an instance of [AccessEvent] and processing it through the [AccessContext].
     *
     * If the access event passes the filter chain decision (not returning [FilterReply.DENY]), it is passed to the access context's
     * appenders for further processing. Otherwise, the event is discarded.
     *
     * If an error occurs during the logging process, it is recorded in the status manager of the [AccessContext] and logged
     * using the internal logger.
     */
    public override fun log() {
        try {
            val accessEvent = AccessEvent(argProvider, accessContext)
            accessEvent.setThreadName(Thread.currentThread().name)
            if (accessContext.getFilterChainDecision(accessEvent) != FilterReply.DENY) {
                accessContext.callAppenders(accessEvent)
            }
        } catch (e: Exception) {
            accessContext.statusManager.add(ErrorStatus("Failed to log access event", this, e))
            logger.error("Failed to log access event: {}", e.message)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AccessLog::class.java)
    }
}
