package io.dmitrysulman.accesslog

import io.dmitrysulman.logback.access.reactor.netty.DefaultAccessLogFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.netty.http.client.HttpClient
import reactor.netty.http.server.HttpServer
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

// TODO add tests fot different properties and formats
// TODO for status manager?
// TODO for serialization of AccessEvent?
// TODO for different ways of providing configuration
class AccessLogTests {
    @Test
    fun `asd test`() {
        val server = HttpServer.create()
            .port(0)
            .handle { _, response -> response.send() }
            .accessLog(true, DefaultAccessLogFactory(javaClass.classLoader.getResource("logback-stdout.xml")))
            .bindNow()

        val client = HttpClient.create().port(server.port())

        client
            .get()
            .uri("/")
            .response()
            .block(5.seconds.toJavaDuration())

//        server.onDispose()
//            .block()

        server.disposeNow(5.seconds.toJavaDuration())

        Thread.sleep(5000)

        assertEquals(1, 1)
    }
}