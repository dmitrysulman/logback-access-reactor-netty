package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.access.common.spi.IAccessEvent
import io.github.dmitrysulman.logback.access.reactor.netty.util.EventCaptureAppender
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.cookie.DefaultCookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse
import reactor.netty.http.server.HttpServer
import java.net.InetSocketAddress
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val REMOTE_HOST_HEADER = "Remote-Host"
private const val REMOTE_ADDRESS_HEADER = "Remote-Address"
private const val TEST_RESPONSE_HEADER_NAME = "Test-Response-Header"
private const val TEST_RESPONSE_HEADER_VALUE = "testResponseHeaderValue"
private const val TEST_REQUEST_HEADER_NAME = "Test-Request-Header"
private const val TEST_REQUEST_HEADER_VALUE = "testRequestHeaderValue"
private const val TEST_COOKIE_NAME = "cookieName"
private const val TEST_COOKIE_VALUE = "cookieValue"

// TODO add tests fot different properties and formats
// TODO cookies
// TODO request/response headers
// TODO query params, UrlDecode (names and values)
// TODO sequence number
// TODO auth user
// TODO serialization of AccessEvent
// TODO different ways of providing configuration
// TODO filters
class AccessLogTests {

    private lateinit var server: DisposableServer
    private lateinit var eventCaptureAppender: EventCaptureAppender
    private var now: Long = 0

    @BeforeEach
    fun setUp() {
        now = System.currentTimeMillis()
        eventCaptureAppender = EventCaptureAppender()
    }

    @AfterEach
    fun tearDown() {
        server.disposeNow()
    }

    @Test
    fun `GET request without query params`() {
        val accessLogFactory = ReactorNettyAccessLogFactory("logback-access-stdout.xml", JoranConfigurator(), true)
            .apply {
                accessContext.addAppender(eventCaptureAppender)
            }

        server = createServer(accessLogFactory, "mock response")
        val response = performGetRequest("/test")
        assertNotNull(response)

        Thread.sleep(150)
        assertEquals(1, eventCaptureAppender.list.size)
        val accessEvent = eventCaptureAppender.list[0]
        assertAccessEvent(accessEvent, response)
    }

    private fun assertAccessEvent(
        accessEvent: IAccessEvent,
        response: HttpClientResponse,
    ) {
        assertEquals("${response.method().name()} ${response.uri()} ${response.version().text()}", accessEvent.requestURL)
        assertEquals(response.uri(), accessEvent.requestURI)
        assertEquals(server.port(), accessEvent.localPort)
        assertEquals(response.responseHeaders().get(REMOTE_HOST_HEADER), accessEvent.remoteHost)
        assertEquals(response.responseHeaders().get(REMOTE_ADDRESS_HEADER), accessEvent.remoteAddr)
        assertEquals("", accessEvent.queryString)
        assertEquals(response.version().text(), accessEvent.protocol)
        assertEquals(response.method().name(), accessEvent.method)
        assertEquals(response.status().code(), accessEvent.statusCode)
        assertTrue(accessEvent.elapsedTime > 0)
        assertTrue(accessEvent.timeStamp > now)
        assertEquals(0, accessEvent.sequenceNumber)
        assertEquals( "-", accessEvent.remoteUser)
        assertEquals(TEST_REQUEST_HEADER_VALUE, accessEvent.getRequestHeader(TEST_REQUEST_HEADER_NAME))
        assertEquals(TEST_RESPONSE_HEADER_VALUE, accessEvent.getResponseHeader(TEST_RESPONSE_HEADER_NAME))
        assertEquals(TEST_COOKIE_VALUE, accessEvent.getCookie(TEST_COOKIE_NAME))
        assertEquals(response.responseHeaders().get(CONTENT_LENGTH)?.toLongOrNull() ?: 0, accessEvent.contentLength)
    }

    private fun createServer(
        accessLogFactory: ReactorNettyAccessLogFactory,
        responseContent: String
    ): DisposableServer {
        return HttpServer.create()
            .handle { request, response ->
                val remoteHost = (request.remoteAddress() as InetSocketAddress).hostString
                val remoteAddress = (request.remoteAddress() as InetSocketAddress).address.hostAddress
                response
                    .addHeader(REMOTE_HOST_HEADER, remoteHost)
                    .addHeader(REMOTE_ADDRESS_HEADER, remoteAddress)
                    .addHeader(TEST_RESPONSE_HEADER_NAME, TEST_RESPONSE_HEADER_VALUE)
                    .sendByteArray(Mono.just(responseContent.toByteArray()))
            }
            .accessLog(true, accessLogFactory)
            .bindNow()
    }

    private fun performGetRequest(uri: String): HttpClientResponse? {
        return HttpClient
            .create()
            .port(server.port())
            .headers { it.add(TEST_REQUEST_HEADER_NAME, TEST_REQUEST_HEADER_VALUE) }
            .cookie(DefaultCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE))
            .get()
            .uri(uri)
            .response()
            .block(30.seconds.toJavaDuration())
    }
}