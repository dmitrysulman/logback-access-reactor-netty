package io.dmitrysulman.accesslog

import ch.qos.logback.access.common.joran.JoranConfigurator
import io.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.netty.http.client.HttpClient
import reactor.netty.http.server.HttpServer

// TODO add tests fot different properties and formats (+UrlDecode of query params)
// TODO for status manager?
// TODO for serialization of AccessEvent?
// TODO for different ways of providing configuration
class AccessLogTests {
    @Test
    fun `asd test`() {
        val server = HttpServer.create()
            .port(0)
            .handle { _, response -> response.send() }
            .accessLog(true, ReactorNettyAccessLogFactory("logback-stdout.xml", JoranConfigurator(), true))
            .bindNow()

        val client = HttpClient.create().port(server.port())

        client
            .get()
            .uri("/test")
            .response()
            .subscribe()

//        server.onDispose()
//            .block()

        Thread.sleep(500)

        server.disposeNow()


        assertEquals(1, 1)
    }
}