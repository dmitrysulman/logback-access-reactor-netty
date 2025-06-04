package io.github.dmitrysulman.logback.access.reactor.netty.joran

import io.github.dmitrysulman.logback.access.reactor.netty.EventCaptureAppender
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure.ReactorNettyAccessLogFactoryAutoConfiguration
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import reactor.netty.http.server.logging.AccessLogArgProvider

class LogbackAccessJoranConfiguratorTests {
    @ParameterizedTest
    @CsvSource(
        "dev,           logback-access-springprofile-dev.xml",
        "'dev,prod',    logback-access-springprofile-dev.xml",
        "dev,           logback-access-springprofile-included.xml",
        "'dev,prod',    logback-access-springprofile-included.xml",
        "dev,           logback-access-springprofile-dev-prod.xml",
        "prod,          logback-access-springprofile-dev-prod.xml",
        "'dev,prod',    logback-access-springprofile-dev-prod.xml",
        "'dev,prod,stg',logback-access-springprofile-dev-prod.xml",
        "'dev,stg',     logback-access-springprofile-dev-prod.xml",
        "'prod,stg',    logback-access-springprofile-dev-prod.xml",
    )
    fun `should log event with springProfile configuration`(
        profile: String,
        filename: String,
    ) {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("spring.profiles.active=$profile")
            .withPropertyValues("logback.access.reactor.netty.config=classpath:$filename")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                val mockArgProvider = mockk<AccessLogArgProvider>(relaxed = true)
                val uri = "/test"
                every { mockArgProvider.uri() } returns uri
                factory.apply(mockArgProvider).log()
                val eventCaptureAppender = factory.accessContext.getAppender("CAPTURE") as EventCaptureAppender
                eventCaptureAppender.list.size shouldBe 1
                eventCaptureAppender.list.first().requestURI shouldBe uri
            }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "logback-access-springprofile-dev.xml",
            "logback-access-springprofile-included.xml",
            "logback-access-springprofile-dev-prod.xml",
        ],
    )
    fun `should not log event with prod springProfile configuration`(filename: String) {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("spring.profiles.active=stg")
            .withPropertyValues("logback.access.reactor.netty.config=classpath:$filename")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.getAppender("CAPTURE").shouldBeNull()
            }
    }

    @Test
    fun `should not log event with empty springProfile configuration`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("spring.profiles.active=dev")
            .withPropertyValues("logback.access.reactor.netty.config=classpath:logback-access-springprofile-empty.xml")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.getAppender("CAPTURE").shouldBeNull()
            }
    }
}
