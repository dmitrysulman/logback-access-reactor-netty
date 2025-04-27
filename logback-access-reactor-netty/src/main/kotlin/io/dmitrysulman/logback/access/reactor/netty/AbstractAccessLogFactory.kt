package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.joran.spi.JoranException
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLogFactory
import java.net.URL

abstract class AbstractAccessLogFactory(
    joranConfigurator: JoranConfigurator,
    config: URL
) : AccessLogFactory {
    private val accessContext = AccessContext()
    init {
        try {
            // TODO add status manager (+ addInfo/addError where needed in this and other classes)
            // TODO add error handling everywhere
            joranConfigurator.context = accessContext
            joranConfigurator.doConfigure(config)
            // TODO stop context in the end?
            accessContext.start()
            //            addInfo("Done configuring")
        } catch (e: JoranException) {
            //            addError("Failed to configure LogbackValve", e)
        }
    }

    override fun apply(argProvider: AccessLogArgProvider) = AccessLog(accessContext, argProvider)
}