package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import ch.qos.logback.core.status.OnConsoleStatusListener
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.util.ResourceUtils
import reactor.netty.http.server.HttpServer
import java.io.ByteArrayInputStream
import java.net.URL
import java.net.URLConnection

class ReactorNettyAccessLogFactoryAutoConfigurationTests {
    @Test
    fun `should supply beans`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .run { context ->
                assertThat(context).hasSingleBean(ReactorNettyAccessLogFactory::class.java)
                assertThat(context).hasSingleBean(ReactorNettyAccessLogWebServerFactoryCustomizer::class.java)
            }
    }

    @Test
    fun `should not supply beans when HttpServer is not on the classpath`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withClassLoader(FilteredClassLoader(HttpServer::class.java))
            .run { context ->
                assertThat(context).doesNotHaveBean(ReactorNettyAccessLogFactory::class.java)
                assertThat(context).doesNotHaveBean(ReactorNettyAccessLogWebServerFactoryCustomizer::class.java)
            }
    }

    @Test
    fun `should not supply beans when is not reactive web application`() {
        WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .run { context ->
                assertThat(context).doesNotHaveBean(ReactorNettyAccessLogFactory::class.java)
                assertThat(context).doesNotHaveBean(ReactorNettyAccessLogWebServerFactoryCustomizer::class.java)
            }
    }

    @Test
    fun `should not supply beans when disabled by the property`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.enabled=false")
            .run { context ->
                assertThat(context).doesNotHaveBean(ReactorNettyAccessLogFactory::class.java)
                assertThat(context).doesNotHaveBean(ReactorNettyAccessLogWebServerFactoryCustomizer::class.java)
            }
    }

    @Test
    fun `should supply beans when explicitly enabled by the property`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.enabled=true")
            .run { context ->
                assertThat(context).hasSingleBean(ReactorNettyAccessLogFactory::class.java)
                assertThat(context).hasSingleBean(ReactorNettyAccessLogWebServerFactoryCustomizer::class.java)
            }
    }

    @Test
    fun `should enable debug mode by the property`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.debug=true")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.statusManager.copyOfStatusListenerList
                    .any { it::class == OnConsoleStatusListener::class }
                    .shouldBeTrue()
            }
    }

    @Test
    fun `should not enable debug mode when debug false`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.debug=false")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.statusManager.copyOfStatusListenerList
                    .none { it::class == OnConsoleStatusListener::class }
                    .shouldBeTrue()
            }
    }

    @Test
    fun `should not enable debug mode when no debug property provided`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.statusManager.copyOfStatusListenerList
                    .none { it::class == OnConsoleStatusListener::class }
                    .shouldBeTrue()
            }
    }

    @Test
    fun `should not supply beans when already has user defined beans in the context`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withUserConfiguration(CustomReactorNettyAccessLogFactoryConfiguration::class.java)
            .run { context ->
                assertThat(context.getBeansOfType<ReactorNettyAccessLogFactory>()).hasSize(1)
                assertThat(context.getBeansOfType<ReactorNettyAccessLogWebServerFactoryCustomizer>()).hasSize(1)
            }
    }

    @Test
    fun `should apply customizer`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    ReactiveWebServerFactoryAutoConfiguration::class.java,
                    ReactorNettyAccessLogFactoryAutoConfiguration::class.java,
                ),
            ).withUserConfiguration(MockNettyAccessLogFactoryConfiguration::class.java)
            .run { context ->
                val customizer = context.getBean<ReactorNettyAccessLogWebServerFactoryCustomizer>()
                verify(exactly = 1) { customizer.customize(any()) }
            }
    }

    @Test
    fun `should load configuration from provided configuration file resource`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.config=file:./src/test/resources/file/logback-access-file.xml")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.getAppender("FILE").shouldNotBeNull()
            }
    }

    @Test
    fun `should load configuration from provided configuration classpath resource`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.config=classpath:logback-access-stdout.xml")
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.getAppender("CAPTURE").shouldNotBeNull()
            }
    }

    @Test
    fun `should fail on not existing file resource`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.config=file:logback-access-not-exist.xml")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context).failure.hasMessageContaining("Could not open URL [file:logback-access-not-exist.xml]")
            }
    }

    @Test
    fun `should fail on not existing classpath resource`() {
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withPropertyValues("logback.access.reactor.netty.config=classpath:logback-access-not-exist.xml")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(
                    context,
                ).failure.hasMessageContaining(
                    "class path resource [logback-access-not-exist.xml] cannot be resolved to URL because it does not exist",
                )
            }
    }

    @Test
    fun `should load configuration from default filename configuration file resource`() {
        val resourceLoaderMock = mockk<ResourceLoader>()
        val resourceMock = mockk<Resource>()
        val urlMock = mockk<URL>(relaxed = true)
        val urlConnectionMock = mockk<URLConnection>(relaxed = true)
        every { resourceMock.exists() } returns true
        every { resourceMock.url } returns urlMock
        every { urlMock.file } returns "default"
        every { urlMock.openConnection() } returns urlConnectionMock
        every { urlConnectionMock.inputStream } returns ByteArrayInputStream("<configuration></configuration>".toByteArray())
        every { resourceLoaderMock.getResource("file:logback-access.xml") } returns resourceMock
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withBean("resourceLoader", ResourceLoader::class.java, { resourceLoaderMock })
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.name shouldBe "default"
            }
    }

    @Test
    fun `should load configuration from default filename configuration classpath resource`() {
        val resourceLoaderMock = mockk<ResourceLoader>()
        val filenameResourceMock = mockk<Resource>()
        val classpathResourceMock = mockk<Resource>()
        val urlMock = mockk<URL>(relaxed = true)
        val urlConnectionMock = mockk<URLConnection>(relaxed = true)
        every { filenameResourceMock.exists() } returns false
        every { classpathResourceMock.exists() } returns true
        every { classpathResourceMock.url } returns urlMock
        every { urlMock.file } returns "default"
        every { urlMock.openConnection() } returns urlConnectionMock
        every { urlConnectionMock.inputStream } returns ByteArrayInputStream("<configuration></configuration>".toByteArray())
        every { resourceLoaderMock.getResource("file:logback-access.xml") } returns filenameResourceMock
        every { resourceLoaderMock.getResource("classpath:logback-access.xml") } returns classpathResourceMock
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withBean("resourceLoader", ResourceLoader::class.java, { resourceLoaderMock })
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.name shouldBe "default"
            }
    }

    @Test
    fun `should load configuration from default configuration file`() {
        val resourceLoaderMock = mockk<ResourceLoader>()
        val filenameResourceMock = mockk<Resource>()
        val classpathResourceMock = mockk<Resource>()
        val defaultResourceMock = mockk<Resource>()
        every { filenameResourceMock.exists() } returns false
        every { classpathResourceMock.exists() } returns false
        every { defaultResourceMock.url } returns
            ResourceUtils.getURL("classpath:logback-access-reactor-netty/logback-access-default-config.xml")
        every { resourceLoaderMock.getResource("file:logback-access.xml") } returns filenameResourceMock
        every { resourceLoaderMock.getResource("classpath:logback-access.xml") } returns classpathResourceMock
        every { resourceLoaderMock.getResource("classpath:logback-access-reactor-netty/logback-access-default-config.xml") } returns
            defaultResourceMock
        ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ReactorNettyAccessLogFactoryAutoConfiguration::class.java))
            .withBean("resourceLoader", ResourceLoader::class.java, { resourceLoaderMock })
            .run { context ->
                val factory = context.getBean<ReactorNettyAccessLogFactory>()
                factory.accessContext.name shouldEndWith "logback-access-reactor-netty/logback-access-default-config.xml"
            }
    }

    @Configuration(proxyBeanMethods = false)
    class CustomReactorNettyAccessLogFactoryConfiguration {
        @Bean
        fun customReactorNettyAccessLogFactory() = ReactorNettyAccessLogFactory()

        @Bean
        fun customReactorNettyAccessLogWebServerFactoryCustomizer(customReactorNettyAccessLogFactory: ReactorNettyAccessLogFactory) =
            ReactorNettyAccessLogWebServerFactoryCustomizer(true, customReactorNettyAccessLogFactory)
    }

    @Configuration(proxyBeanMethods = false)
    class MockNettyAccessLogFactoryConfiguration {
        private val mockReactorNettyAccessLogWebServerFactoryCustomizer =
            mockk<ReactorNettyAccessLogWebServerFactoryCustomizer>(relaxed = true)

        @Bean
        fun mockReactorNettyAccessLogWebServerFactoryCustomizer() = mockReactorNettyAccessLogWebServerFactoryCustomizer
    }
}
