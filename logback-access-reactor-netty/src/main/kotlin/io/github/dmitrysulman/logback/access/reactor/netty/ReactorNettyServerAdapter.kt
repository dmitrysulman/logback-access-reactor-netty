package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.ServerAdapter
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.io.Serializable

class ReactorNettyServerAdapter(
    @Transient
    private val argProvider: AccessLogArgProvider,
) : ServerAdapter,
    Serializable {
    private val _requestTimestamp by lazy { argProvider.accessDateTime()?.toInstant()?.toEpochMilli() ?: 0 }
    private val _contentLength by lazy { argProvider.contentLength() }
    private val _statusCode by lazy { argProvider.status()?.toString()?.toIntOrNull() ?: -1 }
    private val _responseHeaderMap by lazy {
        argProvider
            .responseHeaderIterator()
            ?.asSequence()
            ?.mapNotNull { (name, value) ->
                if (name.isNullOrEmpty()) return@mapNotNull null
                if (value == null) return@mapNotNull null
                name.toString().lowercase() to value.toString()
            }?.toMap()
            ?: emptyMap()
    }

    override fun getRequestTimestamp() = _requestTimestamp

    override fun getContentLength() = _contentLength

    override fun getStatusCode() = _statusCode

    override fun buildResponseHeaderMap() = _responseHeaderMap
}
