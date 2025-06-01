package io.github.dmitrysulman.logback.access.reactor.netty.joran

import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure.ReactorNettyAccessLogFactoryAutoConfiguration
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner

class LogbackAccessSpringProfileWithinSecondPhaseElementSanityCheckerTests {
    @Test
    fun `should add warning status on nested springProfile element within appender element`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.config=classpath:logback-access-springprofile-in-appender.xml")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.statusManager.copyOfStatusList
                    .any {
                        it.message ==
                            "<springProfile> elements cannot be nested within an <appender> element"
                    }.shouldBeTrue()
            }
    }
}
