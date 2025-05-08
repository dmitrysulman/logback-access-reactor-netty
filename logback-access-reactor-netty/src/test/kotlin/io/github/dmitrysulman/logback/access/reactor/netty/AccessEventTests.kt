package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.netty.http.server.ConnectionInformation
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.net.SocketAddress
import java.util.Collections

// TODO serialization of AccessEvent
// TODO cookies
// TODO request/response headers
class AccessEventTests {
    @Test
    fun `test basic properties`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockConnectionInformation = mockk<ConnectionInformation>()
        every { mockConnectionInformation.connectionRemoteAddress() } returns
            object : SocketAddress() {
                override fun toString() = "192.168.1.1"
            }
        every { mockConnectionInformation.hostPort() } returns 1000
        val mockArgProvider = mockk<AccessLogArgProvider>()
        every { mockArgProvider.method() } returns "GET"
        every { mockArgProvider.uri() } returns "/test?param=value"
        every { mockArgProvider.protocol() } returns "HTTP/1.1"
        every { mockArgProvider.status() } returns "200"
        every { mockArgProvider.contentLength() } returns 100
        every { mockArgProvider.duration() } returns 1001
        every { mockArgProvider.user() } returns "username"
        every { mockArgProvider.requestHeaderIterator() } returns Collections.emptyIterator()
        every { mockArgProvider.responseHeaderIterator() } returns Collections.emptyIterator()
        every { mockArgProvider.cookies() } returns emptyMap()
        every { mockArgProvider.connectionInformation() } returns mockConnectionInformation

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.method shouldBe "GET"
        accessEvent.requestURI shouldBe "/test"
        accessEvent.queryString shouldBe "?param=value"
        accessEvent.getRequestParameter("param") shouldBe arrayOf("value")
        accessEvent.getRequestParameter("no_param") shouldBe NA_ARRAY
        accessEvent.protocol shouldBe "HTTP/1.1"
        accessEvent.requestURL shouldBe "GET /test?param=value HTTP/1.1"
        accessEvent.statusCode shouldBe 200
        accessEvent.contentLength shouldBe 100L
        accessEvent.elapsedTime shouldBe 1001L
        accessEvent.elapsedSeconds shouldBe 1L
        accessEvent.remoteAddr shouldBe "192.168.1.1"
        accessEvent.remoteHost shouldBe "192.168.1.1"
        accessEvent.serverName shouldBe "192.168.1.1"
        accessEvent.localPort shouldBe 1000
        accessEvent.remoteUser shouldBe "username"
        accessEvent.sequenceNumber shouldBe 0
        accessEvent.getRequestHeader("Header") shouldBe NA
        accessEvent.getResponseHeader("Header") shouldBe NA
        accessEvent.getCookie("cookie") shouldBe NA
    }

    @Test
    fun `test laziness of the properties`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockConnectionInformation = mockk<ConnectionInformation>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.connectionInformation() } returns mockConnectionInformation

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        verify(exactly = 1) { mockContext.sequenceNumberGenerator }
        verify(exactly = 1) { mockArgProvider.duration() }
        verify(exactly = 0) { mockArgProvider.method() }
        verify(exactly = 0) { mockArgProvider.uri() }
        verify(exactly = 0) { mockArgProvider.protocol() }
        verify(exactly = 0) { mockArgProvider.status() }
        verify(exactly = 0) { mockArgProvider.contentLength() }
        verify(exactly = 0) { mockArgProvider.user() }
        verify(exactly = 0) { mockArgProvider.requestHeaderIterator() }
        verify(exactly = 0) { mockArgProvider.responseHeaderIterator() }
        verify(exactly = 0) { mockArgProvider.cookies() }
        verify(exactly = 0) { mockArgProvider.connectionInformation() }
        verify(exactly = 0) { mockConnectionInformation.connectionRemoteAddress() }
        verify(exactly = 0) { mockConnectionInformation.hostPort() }

        repeat(2) {
            accessEvent.method
            accessEvent.requestURI
            accessEvent.queryString
            accessEvent.requestParameterMap
            accessEvent.getRequestParameter("param")
            accessEvent.protocol
            accessEvent.requestURL
            accessEvent.statusCode
            accessEvent.contentLength
            accessEvent.elapsedTime
            accessEvent.elapsedSeconds
            accessEvent.remoteAddr
            accessEvent.remoteHost
            accessEvent.serverName
            accessEvent.localPort
            accessEvent.remoteUser
            accessEvent.getRequestHeader("Header")
            accessEvent.getResponseHeader("Header")
            accessEvent.requestHeaderMap
            accessEvent.requestHeaderNames
            accessEvent.responseHeaderMap
            accessEvent.responseHeaderNameList
            accessEvent.getCookie("cookie")
        }

        verifyAllMethods(mockArgProvider, mockConnectionInformation)
    }

    @Test
    fun `test prepare for deferred processing`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockConnectionInformation = mockk<ConnectionInformation>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.connectionInformation() } returns mockConnectionInformation

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        repeat(2) {
            accessEvent.prepareForDeferredProcessing()
        }

        verifyAllMethods(mockArgProvider, mockConnectionInformation)
    }

    private fun verifyAllMethods(
        mockArgProvider: AccessLogArgProvider,
        mockConnectionInformation: ConnectionInformation,
    ) {
        verify(exactly = 1) { mockArgProvider.method() }
        verify(exactly = 2) { mockArgProvider.uri() }
        verify(exactly = 1) { mockArgProvider.protocol() }
        verify(exactly = 1) { mockArgProvider.status() }
        verify(exactly = 1) { mockArgProvider.contentLength() }
        verify(exactly = 1) { mockArgProvider.user() }
        verify(exactly = 1) { mockArgProvider.requestHeaderIterator() }
        verify(exactly = 1) { mockArgProvider.responseHeaderIterator() }
        verify(exactly = 1) { mockArgProvider.cookies() }
        verify(exactly = 3) { mockArgProvider.connectionInformation() }
        verify(exactly = 2) { mockConnectionInformation.connectionRemoteAddress() }
        verify(exactly = 1) { mockConnectionInformation.hostPort() }
    }

    @Test
    fun `test request URL with no query parameters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.method() } returns "GET"
        every { mockArgProvider.uri() } returns "/test"
        every { mockArgProvider.protocol() } returns "HTTP/1.1"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.requestURL shouldBe "GET /test HTTP/1.1"
    }

    @Test
    fun `test query string parsing - no parameters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString.isEmpty() shouldBe true
        accessEvent.requestParameterMap.isEmpty() shouldBe true
        accessEvent.getRequestParameter("param") shouldBe NA_ARRAY
    }

    @Test
    fun `test query string parsing - single parameter`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1=value1"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1=value1"
        accessEvent.requestParameterMap.size shouldBe 1
        accessEvent.requestParameterMap shouldContainKey "param1"
        accessEvent.getRequestParameter("param1") shouldBe arrayOf("value1")
    }

    @Test
    fun `test query string parsing - multiple parameters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1=value1&param2=value2"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1=value1&param2=value2"
        accessEvent.requestParameterMap.size shouldBe 2
        accessEvent.requestParameterMap shouldContainKey "param1"
        accessEvent.requestParameterMap shouldContainKey "param2"
        accessEvent.getRequestParameter("param1") shouldBe arrayOf("value1")
        accessEvent.getRequestParameter("param2") shouldBe arrayOf("value2")
    }

    @Test
    fun `test query string parsing - multiple values for same parameter`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1=value1&param1=value2"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1=value1&param1=value2"
        accessEvent.requestParameterMap.size shouldBe 1
        accessEvent.requestParameterMap shouldContainKey "param1"
        accessEvent.getRequestParameter("param1") shouldBe arrayOf("value1", "value2")
    }

    @Test
    fun `test query string parsing - empty parameter value`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1="

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1="
        accessEvent.requestParameterMap.isEmpty() shouldBe true
        accessEvent.getRequestParameter("param1") shouldBe NA_ARRAY
    }

    @Test
    fun `test query string parsing - URL encoded parameters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param%201=value%201&param2=value+2"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param%201=value%201&param2=value+2"
        accessEvent.requestParameterMap.size shouldBe 2
        accessEvent.requestParameterMap shouldContainKey "param 1"
        accessEvent.requestParameterMap shouldContainKey "param2"
        accessEvent.getRequestParameter("param 1") shouldBe arrayOf("value 1")
        accessEvent.getRequestParameter("param2") shouldBe arrayOf("value 2")
    }

    @Test
    fun `test query string parsing - special characters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1=value%40%23%24&param2=%26%3D%3F"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1=value%40%23%24&param2=%26%3D%3F"
        accessEvent.requestParameterMap.size shouldBe 2
        accessEvent.requestParameterMap shouldContainKey "param1"
        accessEvent.requestParameterMap shouldContainKey "param2"
        accessEvent.getRequestParameter("param1") shouldBe arrayOf("value@#$")
        accessEvent.getRequestParameter("param2") shouldBe arrayOf("&=?")
    }

    @Test
    fun `test query string parsing - malformed query string`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1&param2=value2&=value3&param4="

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1&param2=value2&=value3&param4="
        accessEvent.requestParameterMap.size shouldBe 1
        accessEvent.requestParameterMap shouldContainKey "param2"
        accessEvent.getRequestParameter("param1") shouldBe NA_ARRAY
        accessEvent.getRequestParameter("param2") shouldBe arrayOf("value2")
        accessEvent.getRequestParameter("param4") shouldBe NA_ARRAY
    }

    companion object {
        private const val NA = "-"
        private val NA_ARRAY = arrayOf(NA)
    }
}
