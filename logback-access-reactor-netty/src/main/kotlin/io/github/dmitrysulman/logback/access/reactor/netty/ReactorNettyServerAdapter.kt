package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.ServerAdapter
import reactor.netty.http.server.logging.AccessLogArgProvider

class ReactorNettyServerAdapter(
    private val argProvider: AccessLogArgProvider,
) : ServerAdapter {
    override fun getRequestTimestamp() = argProvider.accessDateTime()?.toInstant()?.toEpochMilli() ?: 0

    override fun getContentLength() = argProvider.contentLength()

    override fun getStatusCode() = argProvider.status()?.toString()?.toIntOrNull() ?: -1

    override fun buildResponseHeaderMap() =
        argProvider
            .responseHeaderIterator()
            ?.asSequence()
            ?.mapNotNull { (name, value) ->
                if (name.isNullOrEmpty()) return@mapNotNull null
                if (value == null) return@mapNotNull null
                name.toString() to value.toString()
            }?.toMap()
            ?: emptyMap()
}
