package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.access.common.spi.IAccessEvent
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.io.Serializable
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

private const val EMPTY_STRING = ""
private const val NA = "-"
private val NA_ARRAY = arrayOf(NA)

class AccessEvent(
    @Transient
    private val argProvider: AccessLogArgProvider,
    context: AccessContext
) : IAccessEvent, Serializable {
    private val _timeStamp = System.currentTimeMillis()
    private val _sequenceNumber = context.sequenceNumberGenerator?.nextSequenceNumber() ?: 0
    private val _elapsedTime = argProvider.duration()
    private val _elapsedTimeSeconds = _elapsedTime / 1000
    private val _requestUri by lazy { argProvider.uri()?.toString()?.substringBefore("?") ?: NA }
    private val _queryString by lazy {
        argProvider.uri()?.let { uri ->
            uri.indexOf("?")
                .takeIf { it != -1 }
                ?.let { uri.substring(it) }
                .orEmpty()
        } ?: NA
    }
    private val _remoteHost by lazy { argProvider.connectionInformation()?.hostName() ?: NA }
    private val _remoteUser by lazy { argProvider.user() ?: NA }
    private val _protocol by lazy { argProvider.protocol() ?: NA  }
    private val _method by lazy { argProvider.method()?.toString() ?: NA  }
    private lateinit var _threadName: String
    private val _requestParameterMap by lazy {
        _queryString.takeIf { it.isNotEmpty() && it != NA }
            ?.substring(1)
            ?.split("&")
            ?.mapNotNull {
                val index = it.indexOf("=")
                if (index in 1..it.length - 2) {
                    it.substring(0, index) to it.substring(index + 1)
                } else null
            }
            ?.groupBy({ URLDecoder.decode(it.first, StandardCharsets.UTF_8) }) {
                URLDecoder.decode(it.second, StandardCharsets.UTF_8)
            }
            ?.mapValues { it.value.toTypedArray() }
            ?: emptyMap()
    }
    private val _remoteAddr by lazy { argProvider.connectionInformation()?.remoteAddress()?.toString() ?: NA }
    private val _cookieMap by lazy {
        argProvider.cookies()?.entries?.associate {
            it.key.toString() to (it.value.firstOrNull()?.value() ?: NA)
        } ?: emptyMap()
    }
    private val _contentLength by lazy { _serverAdapter.contentLength }
    private val _statusCode by lazy { _serverAdapter.statusCode }
    private val _localPort by lazy { argProvider.connectionInformation()?.hostPort() ?: -1 }

    @Transient
    private val _serverAdapter = ReactorNettyServerAdapter(argProvider)

    override fun prepareForDeferredProcessing() {
        getRequestURI()
        getQueryString()
        getRemoteHost()
        getRemoteUser()
        getProtocol()
        getMethod()
        getRequestParameterMap()
        getRemoteAddr()
        getCookieMap()
        getContentLength()
        getStatusCode()
        getLocalPort()
    }

    override fun getRequest() = null

    override fun getResponse() = null

    override fun getTimeStamp() = _timeStamp

    override fun getSequenceNumber() = _sequenceNumber

    override fun getElapsedTime() = _elapsedTime

    override fun getElapsedSeconds() = _elapsedTimeSeconds

    override fun getRequestURI() = _requestUri

    override fun getRequestURL() = "$_method $_requestUri$_queryString $_protocol"

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

    override fun getRequestHeader(key: String): String {
        // TODO
        return NA
    }

    override fun getRequestHeaderNames(): Enumeration<String> {
        // TODO
        return Collections.emptyEnumeration()
    }

    override fun getRequestHeaderMap(): Map<String, String> {
        // TODO
        return emptyMap()
    }

    override fun getRequestParameterMap() = _requestParameterMap

    override fun getAttribute(key: String) = NA

    override fun getRequestParameter(key: String) = _requestParameterMap[key] ?: NA_ARRAY

    private fun getCookieMap() = _cookieMap

    override fun getCookie(key: String) = getCookieMap()[key] ?: NA

    override fun getContentLength() = _contentLength

    override fun getStatusCode() = _statusCode

    override fun getRequestContent() = EMPTY_STRING

    override fun getResponseContent() = EMPTY_STRING

    override fun getLocalPort() = _localPort

    override fun getServerAdapter() = _serverAdapter

    override fun getResponseHeader(key: String): String {
        // TODO
        return NA
    }

    override fun getResponseHeaderMap(): Map<String, String> {
        // TODO
        return emptyMap()
    }

    override fun getResponseHeaderNameList(): List<String> {
        // TODO
        return emptyList()
    }
}