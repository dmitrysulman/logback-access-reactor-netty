package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.status.*
import ch.qos.logback.core.util.StatusListenerConfigHelper
import ch.qos.logback.core.util.StatusPrinter2
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLogFactory
import java.io.FileNotFoundException
import java.net.URL

private const val DEFAULT_CONFIG_FILE_NAME = "logback-access.xml"

class ReactorNettyAccessLogFactory : AccessLogFactory {


    private val accessContext = AccessContext()

    @JvmOverloads
    constructor(config: URL?, joranConfigurator: JoranConfigurator = JoranConfigurator(), debug: Boolean = false) {
        // TODO check config on null
        initialize(config, joranConfigurator, debug)
    }

    @JvmOverloads
    constructor(fileName: String, joranConfigurator: JoranConfigurator = JoranConfigurator(), debug: Boolean = false) {
        initialize(getConfigFromFileName(fileName), joranConfigurator, debug)
    }

    constructor() {
        initialize(getDefaultConfig(), JoranConfigurator(), false)
    }

    private fun initialize(config: URL?, joranConfigurator: JoranConfigurator, debug: Boolean) {
        try {
            if (config != null) {
                addStatus(InfoStatus("Start configuring with configuration file ${config.file}", this::class.java.simpleName))
                accessContext.name = config.file
                joranConfigurator.context = accessContext
                joranConfigurator.doConfigure(config)
            } else {
                addStatus(WarnStatus("No configuration file provided, skipping configuration", this::class.java.simpleName))
            }
            if (debug) {
                StatusListenerConfigHelper.addOnConsoleListenerInstance(accessContext, OnConsoleStatusListener())
            }
            accessContext.start()
            addStatus(InfoStatus("Done configuring", this::class.java.simpleName))
        } catch (e: Exception) {
            addStatus(ErrorStatus("Failed to configure ReactorNettyAccessLogFactory", this::class.java.simpleName, e))
        }
        StatusPrinter2().printInCaseOfErrorsOrWarnings(accessContext)
    }

    private fun getDefaultConfig(): URL? {
        return try {
            getConfigFromFileName(DEFAULT_CONFIG_FILE_NAME)
        } catch (e: FileNotFoundException) {
            addStatus(WarnStatus("No configuration file provided, skipping configuration", this::class.java.simpleName))
            return null
        }
    }

    private fun getConfigFromFileName(fileName: String): URL {
        val resource = this::class.java.classLoader.getResource(fileName)
        // TODO
        return resource ?: throw FileNotFoundException("Configuration file $fileName cannot be found")
    }

    private fun addStatus(status: Status) {
        accessContext.statusManager.add(status)
    }

    override fun apply(argProvider: AccessLogArgProvider) = AccessLog(accessContext, argProvider)
}