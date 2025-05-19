package io.github.dmitrysulman.logback.access.reactor.netty

import io.kotest.matchers.string.shouldEndWith
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.netty.http.server.HttpServer

class ExtensionsTests {
    @Test
    fun `test HttpServer#enableLogbackAccess() extension`() {
        val httpServerMock = mockk<HttpServer>(relaxed = true)
        val factoryCapture = slot<ReactorNettyAccessLogFactory>()
        httpServerMock.enableLogbackAccess()
        verify(exactly = 1) { httpServerMock.accessLog(true, capture(factoryCapture)) }
        factoryCapture.captured.accessContext.name shouldEndWith "logback-access.xml"
    }

    @Test
    fun `test HttpServer#enableLogbackAccess(factory) extension`() {
        val factory = ReactorNettyAccessLogFactory()
        val httpServerMock = mockk<HttpServer>(relaxed = true)
        httpServerMock.enableLogbackAccess(factory)
        verify(exactly = 1) { httpServerMock.accessLog(true, factory) }
    }

    @Test
    fun `test HttpServer#enableLogbackAccess(filename) extension`() {
        val httpServerMock = mockk<HttpServer>(relaxed = true)
        val factoryCapture = slot<ReactorNettyAccessLogFactory>()
        httpServerMock.enableLogbackAccess("logback-access-resource.xml")
        verify(exactly = 1) { httpServerMock.accessLog(true, capture(factoryCapture)) }
        factoryCapture.captured.accessContext.name shouldEndWith "logback-access-resource.xml"
    }

    @Test
    fun `test HttpServer#enableLogbackAccess(url) extension`() {
        val httpServerMock = mockk<HttpServer>(relaxed = true)
        val factoryCapture = slot<ReactorNettyAccessLogFactory>()
        httpServerMock.enableLogbackAccess(this::class.java.classLoader.getResource("logback-access-url.xml")!!)
        verify(exactly = 1) { httpServerMock.accessLog(true, capture(factoryCapture)) }
        factoryCapture.captured.accessContext.name shouldEndWith "logback-access-url.xml"
    }
}
