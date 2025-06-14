package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure.integration

import io.github.dmitrysulman.logback.access.reactor.netty.EventCaptureAppender
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["logback.access.reactor.netty.config=classpath:logback-access-stdout.xml"],
)
class IntegrationTests(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val reactorNettyAccessLogFactory: ReactorNettyAccessLogFactory,
    @LocalServerPort private val localServerPort: Int,
) {
    @Test
    fun `smoke test`() {
        val now = System.currentTimeMillis()
        val value = "test"
        webTestClient
            .get()
            .uri("/get?param={param}", value)
            .header("request_header", "request_header_value")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()
            .responseBodyContent shouldBe value.toByteArray()

        val eventCaptureAppender =
            reactorNettyAccessLogFactory.accessContext.getAppender("CAPTURE") as EventCaptureAppender

        eventCaptureAppender.list.size shouldBe 1
        val accessEvent = eventCaptureAppender.list.first()
        accessEvent.requestURL shouldBe "GET /get?param=$value HTTP/1.1"
        accessEvent.contentLength shouldBe value.length
        accessEvent.localPort shouldBe localServerPort
        accessEvent.requestURI shouldBe "/get"
        accessEvent.queryString shouldBe "?param=$value"
        accessEvent.protocol shouldBe "HTTP/1.1"
        accessEvent.method shouldBe "GET"
        accessEvent.statusCode shouldBe 200
        accessEvent.elapsedTime shouldBeGreaterThanOrEqual 0
        accessEvent.timeStamp shouldBeGreaterThan now
        accessEvent.remoteUser shouldBe "-"
        accessEvent.getRequestHeader("request_header") shouldBe "request_header_value"
        accessEvent.getResponseHeader("response_header") shouldBe "response_header_value"
    }
}
