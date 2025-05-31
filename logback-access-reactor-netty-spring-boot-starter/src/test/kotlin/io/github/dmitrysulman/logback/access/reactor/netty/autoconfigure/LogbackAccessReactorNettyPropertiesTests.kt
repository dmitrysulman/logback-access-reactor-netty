package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration

@SpringBootTest(
    classes = [LogbackAccessReactorNettyPropertiesTests::class],
    properties = [
        "logback.access.reactor.netty.enabled=true",
        "logback.access.reactor.netty.config=test-logback-access.xml",
        "logback.access.reactor.netty.debug=true",
    ],
)
@EnableConfigurationProperties(LogbackAccessReactorNettyProperties::class)
@Configuration
class LogbackAccessReactorNettyPropertiesTests(
    @Autowired private val properties: LogbackAccessReactorNettyProperties,
) {
    @Test
    fun `smoke test`() {
        properties.enabled?.shouldBeTrue()
        properties.config shouldBe "test-logback-access.xml"
        properties.debug?.shouldBeTrue()
    }
}
