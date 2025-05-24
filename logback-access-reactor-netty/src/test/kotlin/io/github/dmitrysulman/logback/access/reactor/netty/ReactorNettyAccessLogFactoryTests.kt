package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.status.OnConsoleStatusListener
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory.Companion.CONFIG_FILE_NAME_PROPERTY
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory.Companion.DEFAULT_CONFIGURATION
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory.Companion.DEFAULT_CONFIG_FILE_NAME
import io.github.dmitrysulman.logback.access.reactor.netty.integration.EventCaptureAppender
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.net.URL

class ReactorNettyAccessLogFactoryTests {
    @Test
    fun `test default filename configuration`() {
        val reactorNettyAccessLogFactory = ReactorNettyAccessLogFactory()
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("DEFAULT")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
    }

    @Test
    fun `test filename from configuration property`() {
        System.setProperty(CONFIG_FILE_NAME_PROPERTY, "logback-access-property.xml")
        val reactorNettyAccessLogFactory = ReactorNettyAccessLogFactory()
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("PROPERTY")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
        System.clearProperty(CONFIG_FILE_NAME_PROPERTY)
    }

    @Test
    fun `test filename from constructor parameter as file`() {
        val reactorNettyAccessLogFactory = ReactorNettyAccessLogFactory("./src/test/resources/parameter/logback-access-parameter.xml")
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("PARAMETER")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
    }

    @Test
    fun `test filename from constructor parameter as file with debug true`() {
        val reactorNettyAccessLogFactory =
            ReactorNettyAccessLogFactory("./src/test/resources/parameter/logback-access-parameter.xml", JoranConfigurator(), true)
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("PARAMETER")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
        reactorNettyAccessLogFactory.accessContext.statusManager.copyOfStatusListenerList
            .any {
                it::class.java ==
                    OnConsoleStatusListener::class.java
            }.shouldBeTrue()
    }

    @Test
    fun `test filename from constructor parameter as resource`() {
        val reactorNettyAccessLogFactory = ReactorNettyAccessLogFactory("logback-access-resource.xml")
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("RESOURCE")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
    }

    @Test
    fun `test filename from constructor parameter as resource with debug true`() {
        val reactorNettyAccessLogFactory = ReactorNettyAccessLogFactory("logback-access-resource.xml", JoranConfigurator(), true)
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("RESOURCE")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
        reactorNettyAccessLogFactory.accessContext.statusManager.copyOfStatusListenerList
            .any {
                it::class.java ==
                    OnConsoleStatusListener::class.java
            }.shouldBeTrue()
    }

    @Test
    fun `test file url from constructor parameter`() {
        val reactorNettyAccessLogFactory =
            ReactorNettyAccessLogFactory(this::class.java.classLoader.getResource("logback-access-url.xml")!!)
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("URL")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
    }

    @Test
    fun `test file url from constructor parameter with debug true`() {
        val reactorNettyAccessLogFactory =
            ReactorNettyAccessLogFactory(this::class.java.classLoader.getResource("logback-access-url.xml")!!, JoranConfigurator(), true)
        val defaultAppender = reactorNettyAccessLogFactory.accessContext.getAppender("URL")
        defaultAppender.shouldNotBeNull()
        defaultAppender.shouldBeTypeOf<EventCaptureAppender>()
        reactorNettyAccessLogFactory.accessContext.statusManager.copyOfStatusListenerList
            .any {
                it::class.java ==
                    OnConsoleStatusListener::class.java
            }.shouldBeTrue()
    }

    @Test
    fun `test not existing file url from constructor parameter`() {
        shouldThrowExactly<JoranException> { ReactorNettyAccessLogFactory(URL("file:logback-access-not-exist.xml")) }
    }

    @Test
    fun `test not existing filename from configuration property`() {
        System.setProperty(CONFIG_FILE_NAME_PROPERTY, "logback-access-not-exist.xml")
        shouldThrowExactly<FileNotFoundException> { ReactorNettyAccessLogFactory() }
        System.clearProperty(CONFIG_FILE_NAME_PROPERTY)
    }

    @Test
    fun `test not existing filename from constructor parameter`() {
        shouldThrowExactly<FileNotFoundException> { ReactorNettyAccessLogFactory("logback-access-not-exist.xml") }
    }

    @Test
    fun `test not existing default config file fallback to default configuration`() {
        val reactorNettyAccessLogFactory = spyk<ReactorNettyAccessLogFactory>(recordPrivateCalls = true)
        every { reactorNettyAccessLogFactory["getConfigFromFileName"](DEFAULT_CONFIG_FILE_NAME) } throws FileNotFoundException()
        val getDefaultConfigMethod = reactorNettyAccessLogFactory::class.java.getDeclaredMethod("getDefaultConfig")
        getDefaultConfigMethod.trySetAccessible()
        val defaultConfigUrl = getDefaultConfigMethod.invoke(reactorNettyAccessLogFactory) as URL
        defaultConfigUrl.toString() shouldEndWith DEFAULT_CONFIGURATION
        defaultConfigUrl.openStream().reader().use {
            it.readText() shouldBe
                """
                <configuration>
                    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
                        <encoder>
                            <pattern>common</pattern>
                        </encoder>
                    </appender>

                    <appender-ref ref="STDOUT" />
                </configuration>
                """.trimIndent()
        }
    }
}
