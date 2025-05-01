package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.status.*
import ch.qos.logback.core.util.StatusListenerConfigHelper
import ch.qos.logback.core.util.StatusPrinter2
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLogFactory
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

private const val CONFIG_FILE_NAME_PROPERTY = "logback.access.reactor.netty.config"
private const val DEFAULT_CONFIG_FILE_NAME = "logback-access.xml"

class ReactorNettyAccessLogFactory : AccessLogFactory {


    private val accessContext = AccessContext()

    constructor(config: URL) {
        initialize(config, JoranConfigurator(), false)
    }

    constructor(config: URL, joranConfigurator: JoranConfigurator, debug: Boolean) {
        initialize(config, joranConfigurator, debug)
    }

    constructor(fileName: String) {
        initialize(getConfigFromFileName(fileName), JoranConfigurator(), false)
    }

    constructor(fileName: String, joranConfigurator: JoranConfigurator, debug: Boolean) {
        initialize(getConfigFromFileName(fileName), joranConfigurator, debug)
    }

    constructor() {
        initialize(getDefaultConfig(), JoranConfigurator(), false)
    }

    private fun initialize(config: URL?, joranConfigurator: JoranConfigurator, debug: Boolean) {
        try {
            if (config != null) {
                addStatus(InfoStatus("Start configuring with configuration file [${config.file}]", this::class.java.simpleName))
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
        if (!debug) {
            StatusPrinter2().printInCaseOfErrorsOrWarnings(accessContext)
        }
    }

    private fun getDefaultConfig(): URL? {
        return try {
            val fileNameFromSystemProperty = System.getProperty(CONFIG_FILE_NAME_PROPERTY)?.also {
                addStatus(InfoStatus("Found system property [$CONFIG_FILE_NAME_PROPERTY] value: [$it]",
                    this::class.java.simpleName))
            }
            val fileName = fileNameFromSystemProperty ?: run {
                addStatus(InfoStatus("No system property [$CONFIG_FILE_NAME_PROPERTY] provided, checking [$DEFAULT_CONFIG_FILE_NAME]",
                    this::class.java.simpleName))
                DEFAULT_CONFIG_FILE_NAME
            }
            getConfigFromFileName(fileName)
        } catch (e: FileNotFoundException) {
            addStatus(WarnStatus(e.message, this::class.java.simpleName))
            return null
        }
    }

    private fun getConfigFromFileName(fileName: String): URL {
        val file = File(fileName)
        return if (file.exists()) {
            addStatus(InfoStatus("Found file [$fileName]", this::class.java.simpleName))
            file.toURI().toURL()
        } else {
            addStatus(InfoStatus("Not found file [$fileName], checking resource", this::class.java.simpleName))
            this::class.java.classLoader.getResource(fileName)?.also {
                addStatus(InfoStatus("Found resource [${it.file}]", this::class.java.simpleName))
            } ?: throw FileNotFoundException("Configuration file $fileName not found")
        }
    }

    private fun addStatus(status: Status) {
        accessContext.statusManager.add(status)
    }

    override fun apply(argProvider: AccessLogArgProvider) = AccessLog(accessContext, argProvider)
}