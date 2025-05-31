package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure

import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.server.HttpServer

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
