package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.access.common.spi.IAccessEvent
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.io.Serializable
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Collections.enumeration
import java.util.Enumeration

/**
 * Represents an access event for logging HTTP requests and responses in a Reactor Netty server environment.
 * This class implements the Logback Access [IAccessEvent] interface to provide detailed information about access logs,
 * including request and response data, headers, cookies, and contextual information.
 *
 * This class includes various lazy-loaded properties to efficiently retrieve detailed information about
 * the request and response only when needed. It supports deferred processing of logging data through
 * the `prepareForDeferredProcessing` method, ensuring all necessary attributes are initialized.
 *
 * @constructor Initializes an [AccessEvent] instance with the given [AccessLogArgProvider] and [AccessContext].
 *
 * The [AccessLogArgProvider] supplies information about the HTTP request and response to populate the event's details.
 * The [AccessContext] facilitates access logging within the specified context.
 *
 * @author Dmitry Sulman
 * @see IAccessEvent
 * @see AccessLog
 */
class AccessEvent(
    @Transient
    private val argProvider: AccessLogArgProvider,
    context: AccessContext,
) : IAccessEvent,
    Serializable {
    private val _timeStamp = System.currentTimeMillis()
    private val _sequenceNumber = context.sequenceNumberGenerator?.nextSequenceNumber() ?: 0
    private val _elapsedTime = argProvider.duration()
    private val _elapsedTimeSeconds = _elapsedTime / 1000
    private val _requestPath by lazy { argProvider.uri()?.toString()?.substringBefore("?") ?: NA }
    private val _queryString by lazy {
        argProvider.uri()?.let { uri ->
            uri
                .indexOf("?")
                .takeIf { it != -1 }
                ?.let { uri.substring(it) }
                .orEmpty()
        } ?: NA
    }
    private val _requestUrl by lazy { "$_method ${argProvider.uri()?.toString() ?: NA} $_protocol" }
    private val _remoteHost by lazy {
        val remoteAddress = argProvider.connectionInformation()?.connectionRemoteAddress()
        if (remoteAddress is InetSocketAddress) {
            remoteAddress.hostString
        } else {
            remoteAddress?.toString()
        } ?: NA
    }
    private val _remoteUser by lazy { argProvider.user() ?: NA }
    private val _protocol by lazy { argProvider.protocol() ?: NA }
    private val _method by lazy { argProvider.method()?.toString() ?: NA }
    private var _threadName: String? = null
    private val _requestParameterMap by lazy {
        _queryString
            .takeIf { it.length > 1 }
            ?.substring(1)
            ?.split("&")
            ?.asSequence()
            ?.mapNotNull {
                val index = it.indexOf("=")
                if (index !in 1..it.length - 2) return@mapNotNull null
                it.substring(0, index) to it.substring(index + 1)
            }?.groupBy({ it.first.decodeCatching() }) { it.second.decodeCatching() }
            ?.mapValues { it.value.toTypedArray() }
            ?: emptyMap()
    }
    private val _remoteAddr by lazy {
        val remoteAddress = argProvider.connectionInformation()?.connectionRemoteAddress()
        if (remoteAddress is InetSocketAddress) {
            remoteAddress.address?.hostAddress
        } else {
            remoteAddress?.toString()
        } ?: NA
    }
    private val _cookieMap by lazy {
        argProvider
            .cookies()
            ?.asSequence()
            ?.mapNotNull { (name, values) ->
                if (name.isNullOrBlank()) return@mapNotNull null
                val value = values.firstOrNull()?.value() ?: return@mapNotNull null
                name.toString() to value
            }?.toMap() ?: emptyMap()
    }
    private val _cookieList by lazy {
        argProvider
            .cookies()
            ?.mapNotNull { (name, values) ->
                if (name.isNullOrBlank()) return@mapNotNull null
                val value = values.firstOrNull()?.value() ?: return@mapNotNull null
                try {
                    Cookie(name.toString(), value)
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()
    }
    private val _contentLength by lazy { _serverAdapter.contentLength }
    private val _statusCode by lazy { _serverAdapter.statusCode }
    private val _localPort by lazy { argProvider.connectionInformation()?.hostPort() ?: -1 }
    private val _responseHeaderMap by lazy { _serverAdapter.buildResponseHeaderMap() }
    private val _requestHeaderMap by lazy {
        argProvider
            .requestHeaderIterator()
            ?.asSequence()
            ?.mapNotNull { (name, value) ->
                if (name.isNullOrEmpty()) return@mapNotNull null
                if (value == null) return@mapNotNull null
                name.toString() to value.toString()
            }?.toMap()
            ?: emptyMap()
    }

    private val _serverAdapter by lazy { ReactorNettyServerAdapter(argProvider) }

    private fun String.decodeCatching() =
        try {
            URLDecoder.decode(this, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            this
        }

    override fun prepareForDeferredProcessing() {
        requestURI
        requestURL
        queryString
        remoteHost
        remoteUser
        protocol
        method
        requestParameterMap
        remoteAddr
        getCookieMap()
        cookies
        contentLength
        statusCode
        localPort
        responseHeaderMap
        requestHeaderMap
        threadName
        serverAdapter.requestTimestamp
    }

    override fun getRequest(): HttpServletRequest? = null

    override fun getResponse(): HttpServletResponse? = null

    override fun getTimeStamp() = _timeStamp

    override fun getSequenceNumber() = _sequenceNumber

    override fun getElapsedTime() = _elapsedTime

    override fun getElapsedSeconds() = _elapsedTimeSeconds

    override fun getRequestURI() = _requestPath

    override fun getRequestURL() = _requestUrl

    override fun getRemoteHost() = _remoteHost

    override fun getRemoteUser() = _remoteUser

    override fun getProtocol() = _protocol

    override fun getMethod() = _method

    override fun getServerName() = _remoteHost

    override fun getSessionID() = NA

    override fun setThreadName(threadName: String) {
        this._threadName = threadName
    }

    override fun getThreadName() = _threadName

    override fun getQueryString() = _queryString

    override fun getRemoteAddr() = _remoteAddr

    override fun getRequestHeader(key: String) = _requestHeaderMap[key] ?: NA

    override fun getRequestHeaderNames(): Enumeration<String> = enumeration(_requestHeaderMap.keys)

    override fun getRequestHeaderMap() = _requestHeaderMap

    override fun getRequestParameterMap() = _requestParameterMap

    override fun getAttribute(key: String) = NA

    override fun getRequestParameter(key: String) = _requestParameterMap[key] ?: NA_ARRAY

    override fun getCookies() = _cookieList

    private fun getCookieMap() = _cookieMap

    override fun getCookie(key: String) = getCookieMap()[key] ?: NA

    override fun getContentLength() = _contentLength

    override fun getStatusCode() = _statusCode

    override fun getRequestContent() = EMPTY_STRING

    override fun getResponseContent() = EMPTY_STRING

    override fun getLocalPort() = _localPort

    override fun getServerAdapter() = _serverAdapter

    override fun getResponseHeader(key: String) = _responseHeaderMap[key] ?: NA

    override fun getResponseHeaderMap() = _responseHeaderMap

    override fun getResponseHeaderNameList() = _responseHeaderMap.keys.toList()

    companion object {
        private const val EMPTY_STRING = ""

        private const val NA = "-"

        private val NA_ARRAY = arrayOf(NA)
    }
}
