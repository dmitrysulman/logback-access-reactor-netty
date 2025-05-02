package io.github.dmitrysulman.accesslog

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.core.read.ListAppender
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.netty.http.client.HttpClient
import reactor.netty.http.server.HttpServer

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
    @Test
    fun `simple test`() {
        val accessLogFactory = ReactorNettyAccessLogFactory("logback-stdout.xml", JoranConfigurator(), true)
        val uri = "/test"

        val server = HttpServer.create()
            .port(0)
            .handle { _, response -> response.send() }
            .accessLog(true, accessLogFactory)
            .bindNow()

        val client = HttpClient.create().port(server.port())

        client
            .get()
            .uri(uri)
            .response()
            .subscribe()

        Thread.sleep(500)

        server.disposeNow()

        val listAppender = accessLogFactory.accessContext.getAppender("LIST") as ListAppender
        assertEquals(1, listAppender.list.size)

        val accessEvent = listAppender.list[0]
        assertEquals(uri, accessEvent.requestURI)
    }
}