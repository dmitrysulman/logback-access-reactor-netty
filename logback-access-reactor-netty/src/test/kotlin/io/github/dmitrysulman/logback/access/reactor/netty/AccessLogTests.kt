package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.status.ErrorStatus
import ch.qos.logback.core.status.StatusManager
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import reactor.netty.http.server.logging.AccessLogArgProvider

class AccessLogTests {
    @ParameterizedTest
    @EnumSource(value = FilterReply::class, names = ["DENY"], mode = EnumSource.Mode.EXCLUDE)
    fun `log() method should call accessContext#callAppenders`(filterReply: FilterReply) {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        val accessEventCaptor = slot<AccessEvent>()
        every { mockContext.getFilterChainDecision(any()) } returns filterReply

        val accessLog = AccessLog(mockContext, mockArgProvider)
        accessLog.log()

        verify(exactly = 1) { mockArgProvider.duration() }
        verify(exactly = 1) { mockContext.sequenceNumberGenerator }
        verify(exactly = 1) { mockContext.getFilterChainDecision(capture(accessEventCaptor)) }
        verify(exactly = 1) { mockContext.callAppenders(accessEventCaptor.captured) }

        accessEventCaptor.captured.threadName shouldBe Thread.currentThread().name
    }

    @Test
    fun `log() method should not call accessContext#callAppenders if getFilterChainDecision returns DENY`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockContext.getFilterChainDecision(any()) } returns FilterReply.DENY

        val accessLog = AccessLog(mockContext, mockArgProvider)
        accessLog.log()

        verify(exactly = 1) { mockContext.getFilterChainDecision(any()) }
        verify(exactly = 0) { mockContext.callAppenders(any()) }
    }

    @Test
    fun `exception from accessContext#callAppenders should be catch`() {
        val mockStatusManager = mockk<StatusManager>(relaxed = true)
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
        every { mockContext.statusManager } returns mockStatusManager
        every { mockContext.callAppenders(any()) } throws Exception()

        val accessLog = AccessLog(mockContext, mockArgProvider)
        accessLog.log()

        verify(exactly = 1) { mockStatusManager.add(any<ErrorStatus>()) }
    }
}
