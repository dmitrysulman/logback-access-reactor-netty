package io.github.dmitrysulman.logback.access.reactor.netty

import io.kotest.matchers.longs.shouldBeZero
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ReactorNettyServerAdapterTests {
    @Test
    fun `smoke test`() {
        val mockArgProvider = mockk<AccessLogArgProvider>()
        every { mockArgProvider.accessDateTime() } returns ZonedDateTime.ofInstant(Instant.ofEpochMilli(1746734856000), ZoneId.of("UTC"))
        every { mockArgProvider.contentLength() } returns 100
        every { mockArgProvider.status() } returns "200"
        every { mockArgProvider.responseHeaderIterator() } returns headerListIterator()

        val reactorNettyServerAdapter = ReactorNettyServerAdapter(mockArgProvider)

        reactorNettyServerAdapter.requestTimestamp shouldBe 1746734856000
        reactorNettyServerAdapter.contentLength shouldBe 100
        reactorNettyServerAdapter.statusCode shouldBe 200
        reactorNettyServerAdapter.buildResponseHeaderMap() shouldBe
            mapOf(
                "name1" to "value1",
                "name2" to "value2",
                "empty_value" to "",
            )
    }

    @Test
    fun `test on null values`() {
        val mockArgProvider = mockk<AccessLogArgProvider>()
        every { mockArgProvider.accessDateTime() } returns null
        every { mockArgProvider.status() } returns null
        every { mockArgProvider.responseHeaderIterator() } returns null

        val reactorNettyServerAdapter = ReactorNettyServerAdapter(mockArgProvider)

        reactorNettyServerAdapter.requestTimestamp.shouldBeZero()
        reactorNettyServerAdapter.statusCode shouldBe -1
        reactorNettyServerAdapter.buildResponseHeaderMap().shouldBeEmpty()
    }
}
