package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure.integration

import io.github.dmitrysulman.logback.access.reactor.netty.EventCaptureAppender
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["logback.access.reactor.netty.config=classpath:logback-access-stdout.xml"],
)
class IntegrationTests(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val reactorNettyAccessLogFactory: ReactorNettyAccessLogFactory,
) {
    @Test
    fun `smoke test`() {
        val value = "test"
        webTestClient
            .get()
            .uri("/get?param={param}", value)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()
            .responseBodyContent shouldBe value.toByteArray()

        val eventCaptureAppender =
            reactorNettyAccessLogFactory.accessContext.getAppender("CAPTURE") as EventCaptureAppender

        eventCaptureAppender.list.size shouldBe 1
    }
}
