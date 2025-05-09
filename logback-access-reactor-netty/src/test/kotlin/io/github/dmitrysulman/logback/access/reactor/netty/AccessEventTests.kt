package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.netty.handler.codec.http.cookie.DefaultCookie
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import reactor.netty.http.server.ConnectionInformation
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.SocketAddress
import java.util.Collections
import io.netty.handler.codec.http.cookie.Cookie as NettyCookie

class AccessEventTests {
    @Test
    fun `test basic properties`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>()
        mockArgProvider(mockArgProvider)

        val accessEvent = AccessEvent(mockArgProvider, mockContext)
        accessEvent.setThreadName(THREAD)

        assertAllMethods(accessEvent)
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
            accessEvent.getRequestParameter(PARAM)
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
            accessEvent.getRequestHeader(HEADER)
            accessEvent.getResponseHeader(HEADER)
            accessEvent.requestHeaderMap
            accessEvent.requestHeaderNames
            accessEvent.responseHeaderMap
            accessEvent.responseHeaderNameList
            accessEvent.cookies
            accessEvent.getCookie(COOKIE)
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
        verify(exactly = 3) { mockArgProvider.uri() }
        verify(exactly = 1) { mockArgProvider.protocol() }
        verify(exactly = 1) { mockArgProvider.status() }
        verify(exactly = 1) { mockArgProvider.contentLength() }
        verify(exactly = 1) { mockArgProvider.user() }
        verify(exactly = 1) { mockArgProvider.requestHeaderIterator() }
        verify(exactly = 1) { mockArgProvider.responseHeaderIterator() }
        verify(exactly = 2) { mockArgProvider.cookies() }
        verify(exactly = 3) { mockArgProvider.connectionInformation() }
        verify(exactly = 2) { mockConnectionInformation.connectionRemoteAddress() }
        verify(exactly = 1) { mockConnectionInformation.hostPort() }
    }

    @Test
    fun `test request URL with no query parameters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.method() } returns GET
        every { mockArgProvider.uri() } returns REQUEST_URI
        every { mockArgProvider.protocol() } returns HTTP11

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.requestURL shouldBe "GET /test HTTP/1.1"
    }

    @Test
    fun `test empty request URI`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns ""

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.requestURI.isEmpty() shouldBe true
        accessEvent.queryString.isEmpty() shouldBe true
        accessEvent.requestParameterMap.isEmpty() shouldBe true
        accessEvent.getRequestParameter(PARAM) shouldBe NA_ARRAY
    }

    @Test
    fun `test query string parsing - question mark without parameters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?"
        accessEvent.requestParameterMap.isEmpty() shouldBe true
        accessEvent.getRequestParameter(PARAM) shouldBe NA_ARRAY
    }

    @Test
    fun `test query string parsing - no parameters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns REQUEST_URI

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString.isEmpty() shouldBe true
        accessEvent.requestParameterMap.isEmpty() shouldBe true
        accessEvent.getRequestParameter(PARAM) shouldBe NA_ARRAY
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
    fun `test query string parsing - URL encoded parameters with special characters`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns
            "/test?param%201=value+1&param2=v%20a%2Bl%26u%3De%24%23%28%29%40%21123%C2%B1%2F%5C%3C%3E%2C%22%3B%27%25%3F"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe
            "?param%201=value+1&param2=v%20a%2Bl%26u%3De%24%23%28%29%40%21123%C2%B1%2F%5C%3C%3E%2C%22%3B%27%25%3F"
        accessEvent.requestParameterMap.size shouldBe 2
        accessEvent.requestParameterMap shouldContainKey "param 1"
        accessEvent.requestParameterMap shouldContainKey "param2"
        accessEvent.getRequestParameter("param 1") shouldBe arrayOf("value 1")
        accessEvent.getRequestParameter("param2") shouldBe arrayOf("v a+l&u=e\$#()@!123±/\\<>,\";'%?")
    }

    @Test
    fun `test query string parsing - malformed URL encoding`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1=%26%3D%3F&param2=%Q6%3D%3F"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1=%26%3D%3F&param2=%Q6%3D%3F"
        accessEvent.requestParameterMap.size shouldBe 2
        accessEvent.requestParameterMap shouldContainKey "param1"
        accessEvent.requestParameterMap shouldContainKey "param2"
        accessEvent.getRequestParameter("param1") shouldBe arrayOf("&=?")
        accessEvent.getRequestParameter("param2") shouldBe arrayOf("%Q6%3D%3F")
    }

    @Test
    fun `test query string parsing - malformed query string`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.uri() } returns "/test?param1&param2=value2&=value3&param4=&&=&param5&=&==&=param6=?"

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.queryString shouldBe "?param1&param2=value2&=value3&param4=&&=&param5&=&==&=param6=?"
        accessEvent.requestParameterMap.size shouldBe 1
        accessEvent.requestParameterMap shouldContainKey "param2"
        accessEvent.getRequestParameter("param1") shouldBe NA_ARRAY
        accessEvent.getRequestParameter("param2") shouldBe arrayOf("value2")
        accessEvent.getRequestParameter("param4") shouldBe NA_ARRAY
        accessEvent.getRequestParameter("param5") shouldBe NA_ARRAY
        accessEvent.getRequestParameter("param6") shouldBe NA_ARRAY
    }

    @Test
    fun `test on null values`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockConnectionInformation = mockk<ConnectionInformation>()
        every { mockConnectionInformation.connectionRemoteAddress() } returns null
        val mockArgProvider = mockk<AccessLogArgProvider>()
        every { mockArgProvider.duration() } returns 0
        every { mockArgProvider.method() } returns null
        every { mockArgProvider.uri() } returns null
        every { mockArgProvider.protocol() } returns null
        every { mockArgProvider.status() } returns null
        every { mockArgProvider.user() } returns null
        every { mockArgProvider.requestHeaderIterator() } returns null
        every { mockArgProvider.responseHeaderIterator() } returns null
        every { mockArgProvider.cookies() } returns null
        every { mockArgProvider.connectionInformation() } returns mockConnectionInformation

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.method shouldBe NA
        accessEvent.requestURI shouldBe NA
        accessEvent.queryString shouldBe NA
        accessEvent.getRequestParameter(PARAM) shouldBe NA_ARRAY
        accessEvent.protocol shouldBe NA
        accessEvent.requestURL shouldBe "$NA $NA $NA"
        accessEvent.statusCode shouldBe -1
        accessEvent.remoteAddr shouldBe NA
        accessEvent.remoteHost shouldBe NA
        accessEvent.serverName shouldBe NA
        accessEvent.remoteUser shouldBe NA
        accessEvent.getRequestHeader(HEADER) shouldBe NA
        accessEvent.getResponseHeader(HEADER) shouldBe NA
        accessEvent.requestHeaderMap.isEmpty() shouldBe true
        accessEvent.requestHeaderNames.hasMoreElements() shouldBe false
        accessEvent.responseHeaderMap.isEmpty() shouldBe true
        accessEvent.responseHeaderNameList.isEmpty() shouldBe true
        accessEvent.getCookie(COOKIE) shouldBe NA
    }

    @Test
    fun `test on null connectionInformation`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>()
        every { mockArgProvider.duration() } returns 0
        every { mockArgProvider.connectionInformation() } returns null

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.remoteAddr shouldBe NA
        accessEvent.remoteHost shouldBe NA
        accessEvent.localPort shouldBe -1
    }

    @Test
    fun `test cookies`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.cookies() } returns
            mapOf(
                "cookie1" to setOf(DefaultCookie("cookie1", "value1")),
                "cookie2" to
                    LinkedHashSet<NettyCookie>().apply {
                        add(DefaultCookie("cookie2", "value21"))
                        add(DefaultCookie("cookie2", "value22"))
                    },
            )

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.cookies.size shouldBe 2
        accessEvent.cookies shouldContain Cookie("cookie1", "value1")
        accessEvent.cookies shouldContain Cookie("cookie2", "value21")
        accessEvent.getCookie("cookie1") shouldBe "value1"
        accessEvent.getCookie("cookie2") shouldBe "value21"
        accessEvent.getCookie("not_exist") shouldBe NA
    }

    @Test
    fun `should not throw on null or blank cookie name`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.cookies() } returns
            mapOf(
                null to setOf(DefaultCookie(COOKIE, VALUE)),
                "" to setOf(DefaultCookie(COOKIE, VALUE)),
                "   " to setOf(DefaultCookie(COOKIE, VALUE)),
                "cookie1" to setOf(DefaultCookie("cookie1", "value1")),
            )

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.cookies.size shouldBe 1
        accessEvent.cookies shouldContain Cookie("cookie1", "value1")
        accessEvent.getCookie("cookie1") shouldBe "value1"
        accessEvent.getCookie("") shouldBe NA
        accessEvent.getCookie("   ") shouldBe NA
    }

    @Test
    fun `test request headers`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.requestHeaderIterator() } returns headerListIterator()

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.requestHeaderMap shouldBe
            mapOf(
                "name1" to "value1",
                "name2" to "value2",
                "empty_value" to "",
            )
        accessEvent.requestHeaderNames
            .asIterator()
            .asSequence()
            .toList() shouldBe
            listOf("name1", "name2", "empty_value")
    }

    @Test
    fun `test response headers`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.responseHeaderIterator() } returns headerListIterator()

        val accessEvent = AccessEvent(mockArgProvider, mockContext)

        accessEvent.responseHeaderMap shouldBe
            mapOf(
                "name1" to "value1",
                "name2" to "value2",
                "empty_value" to "",
            )
        accessEvent.responseHeaderNameList shouldBe
            listOf("name1", "name2", "empty_value")
    }

    @Test
    fun `test serialization`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>()
        mockArgProvider(mockArgProvider)

        val accessEvent = AccessEvent(mockArgProvider, mockContext)
        accessEvent.setThreadName(THREAD)
        accessEvent.prepareForDeferredProcessing()

        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(accessEvent)
        objectOutputStream.flush()
        objectOutputStream.close()
        val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val deserializedAccessEvent = objectInputStream.readObject() as AccessEvent

        assertAllMethods(deserializedAccessEvent)
    }

    private fun assertAllMethods(accessEvent: AccessEvent) {
        accessEvent.method shouldBe GET
        accessEvent.requestURI shouldBe REQUEST_URI
        accessEvent.queryString shouldBe QUERY_STRING
        accessEvent.requestParameterMap.size shouldBe 1
        accessEvent.getRequestParameter(PARAM) shouldBe arrayOf(VALUE)
        accessEvent.getRequestParameter("no_param") shouldBe NA_ARRAY
        accessEvent.protocol shouldBe HTTP11
        accessEvent.requestURL shouldBe "$GET $REQUEST_URI$QUERY_STRING $HTTP11"
        accessEvent.statusCode shouldBe OK.toInt()
        accessEvent.contentLength shouldBe CONTENT_LENGTH
        accessEvent.elapsedTime shouldBe DURATION
        accessEvent.elapsedSeconds shouldBe DURATION_SECONDS
        accessEvent.remoteAddr shouldBe IP_ADDRESS
        accessEvent.remoteHost shouldBe IP_ADDRESS
        accessEvent.serverName shouldBe IP_ADDRESS
        accessEvent.localPort shouldBe PORT
        accessEvent.remoteUser shouldBe USERNAME
        accessEvent.sequenceNumber shouldBe 0
        accessEvent.getRequestHeader(HEADER) shouldBe NA
        accessEvent.getResponseHeader(HEADER) shouldBe NA
        accessEvent.requestHeaderMap.isEmpty() shouldBe true
        accessEvent.requestHeaderNames.hasMoreElements() shouldBe false
        accessEvent.responseHeaderMap.isEmpty() shouldBe true
        accessEvent.responseHeaderNameList.isEmpty() shouldBe true
        accessEvent.cookies.isEmpty() shouldBe true
        accessEvent.getCookie(COOKIE) shouldBe NA
        accessEvent.threadName shouldBe THREAD
        accessEvent.request shouldBe null
        accessEvent.response shouldBe null
        accessEvent.sessionID shouldBe NA
        accessEvent.getAttribute(ATTRIBUTE) shouldBe NA
        accessEvent.requestContent.isEmpty() shouldBe true
        accessEvent.responseContent.isEmpty() shouldBe true
    }

    private fun mockArgProvider(mockArgProvider: AccessLogArgProvider) {
        val mockConnectionInformation = mockk<ConnectionInformation>()
        every { mockConnectionInformation.connectionRemoteAddress() } returns
            object : SocketAddress() {
                override fun toString() = IP_ADDRESS
            }
        every { mockConnectionInformation.hostPort() } returns PORT
        every { mockArgProvider.method() } returns GET
        every { mockArgProvider.uri() } returns "$REQUEST_URI$QUERY_STRING"
        every { mockArgProvider.protocol() } returns HTTP11
        every { mockArgProvider.status() } returns OK
        every { mockArgProvider.contentLength() } returns CONTENT_LENGTH
        every { mockArgProvider.duration() } returns DURATION
        every { mockArgProvider.user() } returns USERNAME
        every { mockArgProvider.requestHeaderIterator() } returns Collections.emptyIterator()
        every { mockArgProvider.responseHeaderIterator() } returns Collections.emptyIterator()
        every { mockArgProvider.cookies() } returns emptyMap()
        every { mockArgProvider.connectionInformation() } returns mockConnectionInformation
    }

    companion object {
        private const val NA = "-"
        private val NA_ARRAY = arrayOf(NA)
        private const val GET = "GET"
        private const val HTTP11 = "HTTP/1.1"
        private const val OK = "200"
        private const val IP_ADDRESS = "192.168.1.1"
        private const val USERNAME = "username"
        private const val PORT = 1000
        private const val CONTENT_LENGTH = 100L
        private const val DURATION = 1001L
        private const val HEADER = "Header"
        private const val COOKIE = "cookie"
        private const val THREAD = "thread"
        private const val ATTRIBUTE = "attribute"
        private const val PARAM = "param"
        private const val VALUE = "value"
        private const val REQUEST_URI = "/test"
        private const val QUERY_STRING = "?param=value"
        private const val DURATION_SECONDS = 1L
    }
}
