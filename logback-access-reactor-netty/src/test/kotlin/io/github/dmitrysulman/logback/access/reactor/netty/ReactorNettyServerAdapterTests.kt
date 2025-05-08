package io.github.dmitrysulman.logback.access.reactor.netty

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
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockArgProvider.accessDateTime() } returns ZonedDateTime.ofInstant(Instant.ofEpochMilli(1746734856000), ZoneId.of("UTC"))
        every { mockArgProvider.contentLength() } returns 100
        every { mockArgProvider.status() } returns "200"
        every { mockArgProvider.responseHeaderIterator() } returns
            listOf(
                object : Map.Entry<CharSequence, CharSequence> {
                    override val key = "key"
                    override val value = "value"
                },
            ).iterator()

        val reactorNettyServerAdapter = ReactorNettyServerAdapter(mockArgProvider)

        reactorNettyServerAdapter.requestTimestamp shouldBe 1746734856000
        reactorNettyServerAdapter.contentLength shouldBe 100
        reactorNettyServerAdapter.statusCode shouldBe 200
        reactorNettyServerAdapter.buildResponseHeaderMap() shouldBe mapOf("key" to "value")
    }
}
