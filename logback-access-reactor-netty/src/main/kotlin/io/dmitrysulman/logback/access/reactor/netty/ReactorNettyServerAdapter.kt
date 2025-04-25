package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.ServerAdapter
import reactor.netty.http.server.logging.AccessLogArgProvider

class ReactorNettyServerAdapter(
    private val argProvider: AccessLogArgProvider
) : ServerAdapter {
    override fun getRequestTimestamp(): Long {
        return argProvider.accessDateTime()?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
    }

    override fun getContentLength(): Long {
        return argProvider.contentLength()
    }

    override fun getStatusCode(): Int {
        TODO("Not yet implemented")
    }

    override fun buildResponseHeaderMap(): MutableMap<String, String> {
        TODO("Not yet implemented")
    }
}