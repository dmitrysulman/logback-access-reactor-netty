package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.status.ErrorStatus
import ch.qos.logback.core.status.StatusManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.netty.http.server.logging.AccessLogArgProvider

class AccessLogTests {
    @Test
    fun `log() method should call accessContext#callAppenders`() {
        val mockContext = mockk<AccessContext>(relaxed = true)
        val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)

        val accessLog = AccessLog(mockContext, mockArgProvider)
        accessLog.log()

        verify(exactly = 1) { mockContext.getFilterChainDecision(any()) }
        verify(exactly = 1) { mockContext.callAppenders(any()) }
    }

    @Test
    fun `log() method should not call accessContext#callAppenders if mockContext#getFilterChainDecision returns false`() {
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
