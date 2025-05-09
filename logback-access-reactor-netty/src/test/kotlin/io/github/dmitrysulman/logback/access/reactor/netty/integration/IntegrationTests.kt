package io.github.dmitrysulman.logback.access.reactor.netty.integration

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.access.common.spi.IAccessEvent
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.collections.shouldBeSorted
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.cookie.DefaultCookie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse
import reactor.netty.http.server.HttpServer
import java.net.InetSocketAddress
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

// TODO filters
class IntegrationTests {
    private lateinit var server: DisposableServer
    private lateinit var eventCaptureAppender: EventCaptureAppender
    private var now = 0L

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
    fun `test basic request`(): Unit =
        runBlocking {
            val accessLogFactory =
                ReactorNettyAccessLogFactory("logback-access-stdout.xml", JoranConfigurator(), true)
                    .apply {
                        accessContext.addAppender(eventCaptureAppender)
                    }

            server = createServer(accessLogFactory, "mock response")
            val response = performGetRequest("/test?name=value")
            response.shouldNotBeNull()

            eventually(1.seconds) {
                eventCaptureAppender.list.size shouldBe 1
                val accessEvent = eventCaptureAppender.list[0]
                accessEvent.sequenceNumber shouldBe 0
                assertAccessEvent(accessEvent, response)
            }
        }

    @Test
    fun `test sequence number generator`(): Unit =
        runBlocking {
            val accessLogFactory =
                ReactorNettyAccessLogFactory("logback-access-sequence-number-generator.xml", JoranConfigurator(), true)
                    .apply {
                        accessContext.addAppender(eventCaptureAppender)
                    }

            server = createServer(accessLogFactory, "")

            repeat(500) {
                performGetRequest("/test")
            }

            eventually(1.seconds) {
                eventCaptureAppender.list.size shouldBe 500
                eventCaptureAppender.list
                    .sortedBy { it.timeStamp }
                    .map { it.sequenceNumber }
                    .shouldBeUnique()
                    .shouldBeSorted()
            }
        }

    @Test
    fun `performance test`(): Unit =
        runBlocking {
            val accessLogFactory =
                ReactorNettyAccessLogFactory("logback-access-sequence-number-generator.xml", JoranConfigurator(), true)
                    .apply {
                        accessContext.addAppender(eventCaptureAppender)
                    }

            server = createServer(accessLogFactory, "test")

            val jobs =
                (1..4000).map {
                    CoroutineScope(Dispatchers.Default).launch {
                        performGetRequest("/test")
                    }
                }
            jobs.joinAll()
            eventually(5.seconds) { eventCaptureAppender.list.size shouldBe jobs.size }
        }

    private fun assertAccessEvent(
        accessEvent: IAccessEvent,
        response: HttpClientResponse,
    ) {
        accessEvent.requestURL shouldBe "${response.method().name()} ${response.uri()} ${response.version().text()}"
        accessEvent.requestURI shouldBe response.fullPath()
        accessEvent.localPort shouldBe server.port()
        accessEvent.remoteHost shouldBe response.responseHeaders().get(REMOTE_HOST_HEADER)
        accessEvent.remoteAddr shouldBe response.responseHeaders().get(REMOTE_ADDRESS_HEADER)
        accessEvent.queryString shouldBe
            if (response.uri().indexOf("?") != -1) response.uri().substring(response.uri().indexOf("?")) else ""
        accessEvent.protocol shouldBe response.version().text()
        accessEvent.method shouldBe response.method().name()
        accessEvent.statusCode shouldBe response.status().code()
        accessEvent.elapsedTime shouldBeGreaterThanOrEqual 0
        accessEvent.timeStamp shouldBeGreaterThan now
        accessEvent.remoteUser shouldBe "-"
        accessEvent.getRequestHeader(TEST_REQUEST_HEADER_NAME) shouldBe TEST_REQUEST_HEADER_VALUE
        accessEvent.getResponseHeader(TEST_RESPONSE_HEADER_NAME) shouldBe TEST_RESPONSE_HEADER_VALUE
        accessEvent.getCookie(TEST_COOKIE_NAME) shouldBe TEST_COOKIE_VALUE
        accessEvent.contentLength shouldBe (response.responseHeaders().get(CONTENT_LENGTH)?.toLongOrNull() ?: 0)
    }

    private fun createServer(
        accessLogFactory: ReactorNettyAccessLogFactory,
        responseContent: String,
    ): DisposableServer =
        HttpServer
            .create()
            .handle { request, response ->
                val remoteHost = (request.remoteAddress() as InetSocketAddress).hostString
                val remoteAddress = (request.remoteAddress() as InetSocketAddress).address.hostAddress
                response
                    .addHeader(REMOTE_HOST_HEADER, remoteHost)
                    .addHeader(REMOTE_ADDRESS_HEADER, remoteAddress)
                    .addHeader(TEST_RESPONSE_HEADER_NAME, TEST_RESPONSE_HEADER_VALUE)
                    .sendByteArray(Mono.just(responseContent.toByteArray()))
            }.accessLog(true, accessLogFactory)
            .bindNow()

    private fun performGetRequest(uri: String): HttpClientResponse? =
        HttpClient
            .create()
            .port(server.port())
            .headers { it.add(TEST_REQUEST_HEADER_NAME, TEST_REQUEST_HEADER_VALUE) }
            .cookie(DefaultCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE))
            .get()
            .uri(uri)
            .response()
            .block(30.seconds.toJavaDuration())

    companion object {
        private const val REMOTE_HOST_HEADER = "Remote-Host"
        private const val REMOTE_ADDRESS_HEADER = "Remote-Address"
        private const val TEST_RESPONSE_HEADER_NAME = "Test-Response-Header"
        private const val TEST_RESPONSE_HEADER_VALUE = "testResponseHeaderValue"
        private const val TEST_REQUEST_HEADER_NAME = "Test-Request-Header"
        private const val TEST_REQUEST_HEADER_VALUE = "testRequestHeaderValue"
        private const val TEST_COOKIE_NAME = "cookieName"
        private const val TEST_COOKIE_VALUE = "cookieValue"
    }
}
