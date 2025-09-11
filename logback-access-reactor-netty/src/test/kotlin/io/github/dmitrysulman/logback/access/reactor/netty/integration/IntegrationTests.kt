package io.github.dmitrysulman.logback.access.reactor.netty.integration

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.access.common.spi.IAccessEvent
import ch.qos.logback.core.spi.FilterReply
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.kotest.assertions.nondeterministic.continually
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSorted
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeZero
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.cookie.DefaultCookie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse
import reactor.netty.http.server.HttpServer
import reactor.netty.resources.ConnectionProvider
import kotlin.time.Duration.Companion.seconds

class IntegrationTests {
    private lateinit var server: DisposableServer
    private lateinit var client: HttpClient
    private var now = 0L

    @BeforeEach
    fun setUp() {
        now = System.currentTimeMillis()
    }

    @AfterEach
    fun tearDown() {
        server.disposeNow()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/",
            "/test",
            "/test?name=value",
            "/test?name1=value1&name2=value2",
            "/test?name=v%20alue%21%40%23%24%25%5E%26%2A%28%29%3D%2B",
        ],
    )
    fun `test basic requests`(uri: String): Unit =
        runBlocking {
            val accessLogFactory =
                ReactorNettyAccessLogFactory("logback-access-stdout.xml", JoranConfigurator(), true)

            val eventCaptureAppender = accessLogFactory.accessContext.getAppender("CAPTURE") as EventCaptureAppender
            server = createServer(accessLogFactory, "mock response")
            client = createClient()
            val response = performGetRequest(uri).awaitSingleOrNull()
            response.shouldNotBeNull()
            response.status().code() shouldBe 200

            eventually(1.seconds) {
                eventCaptureAppender.list.size shouldBe 1
                val accessEvent = eventCaptureAppender.list[0]
                accessEvent.sequenceNumber.shouldBeZero()
                assertAccessEvent(accessEvent, response)
            }
        }

    @Test
    fun `test filter deny`(): Unit =
        runBlocking {
            val accessLogFactory =
                ReactorNettyAccessLogFactory("logback-access-filter.xml", JoranConfigurator(), true)

            val eventCaptureAppender = accessLogFactory.accessContext.getAppender("CAPTURE") as EventCaptureAppender
            server = createServer(accessLogFactory, "mock response")
            client = createClient()

            val responseDeny = performGetRequest("/test?filter=deny").awaitSingleOrNull()
            responseDeny.shouldNotBeNull()
            responseDeny.status().code() shouldBe 200

            continually(1.seconds) {
                eventCaptureAppender.list.shouldBeEmpty()
            }
        }

    @ParameterizedTest
    @EnumSource(value = FilterReply::class, names = ["DENY"], mode = EnumSource.Mode.EXCLUDE)
    fun `test filter allow`(filterReply: FilterReply): Unit =
        runBlocking {
            val accessLogFactory =
                ReactorNettyAccessLogFactory("logback-access-filter.xml", JoranConfigurator(), true)

            val eventCaptureAppender = accessLogFactory.accessContext.getAppender("CAPTURE") as EventCaptureAppender
            server = createServer(accessLogFactory, "mock response")
            client = createClient()

            val responseAccept = performGetRequest("/test?filter=${filterReply.name}").awaitSingleOrNull()
            responseAccept.shouldNotBeNull()
            responseAccept.status().code() shouldBe 200

            eventually(1.seconds) {
                eventCaptureAppender.list.size shouldBe 1
                val accessEvent = eventCaptureAppender.list[0]
                assertAccessEvent(accessEvent, responseAccept)
            }
        }

    @Test
    fun `test sequence number generator`(): Unit =
        runBlocking {
            val accessLogFactory =
                ReactorNettyAccessLogFactory("logback-access-sequence-number-generator.xml", JoranConfigurator(), true)

            val eventCaptureAppender = accessLogFactory.accessContext.getAppender("CAPTURE") as EventCaptureAppender
            server = createServer(accessLogFactory, "")
            client = createClient()

            repeat(50) {
                val response = performGetRequest("/test").awaitSingleOrNull()
                response.shouldNotBeNull()
                response.status().code() shouldBe 200
            }

            eventually(1.seconds) {
                eventCaptureAppender.list.size shouldBe 50
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
                ReactorNettyAccessLogFactory("logback-access-capture.xml", JoranConfigurator(), true)

            val eventCaptureAppender = accessLogFactory.accessContext.getAppender("CAPTURE") as EventCaptureAppender
            server = createServer(accessLogFactory, "test")
            client = createClient()

            val jobs =
                (1..5000).map {
                    CoroutineScope(Dispatchers.Default).launch {
                        val response = performGetRequest("/test").awaitSingleOrNull()
                        response.shouldNotBeNull()
                        response.status().code() shouldBe 200
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
        accessEvent.getRequestHeader(TEST_REQUEST_HEADER_NAME_IGNORE_CASE.lowercase()) shouldBe TEST_REQUEST_HEADER_VALUE_IGNORE_CASE
        accessEvent.getResponseHeader(TEST_RESPONSE_HEADER_NAME) shouldBe TEST_RESPONSE_HEADER_VALUE
        accessEvent.getResponseHeader(TEST_RESPONSE_HEADER_NAME_IGNORE_CASE.lowercase()) shouldBe TEST_RESPONSE_HEADER_VALUE_IGNORE_CASE
        accessEvent.getCookie(TEST_COOKIE_NAME) shouldBe TEST_COOKIE_VALUE
        accessEvent.contentLength shouldBe (response.responseHeaders().get(CONTENT_LENGTH)?.toLongOrNull() ?: 0)
    }

    private fun createServer(
        accessLogFactory: ReactorNettyAccessLogFactory,
        responseContent: String,
    ) = HttpServer
        .create()
        .accessLog(true, accessLogFactory)
        .handle { request, response ->
            val remoteHost = request.remoteAddress()!!.hostString
            val remoteAddress = request.remoteAddress()!!.address.hostAddress
            response
                .addHeader(REMOTE_HOST_HEADER, remoteHost)
                .addHeader(REMOTE_ADDRESS_HEADER, remoteAddress)
                .addHeader(TEST_RESPONSE_HEADER_NAME, TEST_RESPONSE_HEADER_VALUE)
                .addHeader(TEST_RESPONSE_HEADER_NAME_IGNORE_CASE, TEST_RESPONSE_HEADER_VALUE_IGNORE_CASE)
                .sendByteArray(Mono.just(responseContent.toByteArray()))
        }.bindNow()

    private fun createClient(): HttpClient {
        val connectionProvider =
            ConnectionProvider
                .builder("provider")
                .maxConnections(50)
                .pendingAcquireMaxCount(5000)
                .build()
        return HttpClient
            .create(connectionProvider)
    }

    private fun performGetRequest(uri: String): Mono<HttpClientResponse> =
        client
            .port(server.port())
            .headers {
                it.add(TEST_REQUEST_HEADER_NAME, TEST_REQUEST_HEADER_VALUE)
                it.add(TEST_REQUEST_HEADER_NAME_IGNORE_CASE, TEST_REQUEST_HEADER_VALUE_IGNORE_CASE)
            }.cookie(DefaultCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE))
            .get()
            .uri(uri)
            .response()

    companion object {
        private const val REMOTE_HOST_HEADER = "Remote-Host"
        private const val REMOTE_ADDRESS_HEADER = "Remote-Address"
        private const val TEST_RESPONSE_HEADER_NAME = "Test-Response-Header"
        private const val TEST_RESPONSE_HEADER_VALUE = "testResponseHeaderValue"
        private const val TEST_RESPONSE_HEADER_NAME_IGNORE_CASE = "Test-Response-Header-Ignore-Case"
        private const val TEST_RESPONSE_HEADER_VALUE_IGNORE_CASE = "testResponseHeaderValueIgnoreCase"
        private const val TEST_REQUEST_HEADER_NAME = "Test-Request-Header"
        private const val TEST_REQUEST_HEADER_VALUE = "testRequestHeaderValue"
        private const val TEST_REQUEST_HEADER_NAME_IGNORE_CASE = "Test-Request-Header-Ignore-Case"
        private const val TEST_REQUEST_HEADER_VALUE_IGNORE_CASE = "testRequestHeaderValueIgnoreCase"
        private const val TEST_COOKIE_NAME = "cookieName"
        private const val TEST_COOKIE_VALUE = "cookieValue"
    }
}
