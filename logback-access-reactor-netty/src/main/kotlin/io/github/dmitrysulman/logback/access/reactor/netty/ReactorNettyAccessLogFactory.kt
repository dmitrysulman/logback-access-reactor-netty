package io.github.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.access.common.spi.AccessContext
import ch.qos.logback.core.status.ErrorStatus
import ch.qos.logback.core.status.InfoStatus
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.core.status.Status
import ch.qos.logback.core.util.StatusListenerConfigHelper
import ch.qos.logback.core.util.StatusPrinter2
import reactor.netty.http.server.logging.AccessLogArgProvider
import reactor.netty.http.server.logging.AccessLogFactory
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

/**
 * Factory class responsible for creating instances of [AccessLog] for integrating Logback Access with a Reactor Netty
 * HTTP server. It supports configuration through external `.xml` configuration files.
 *
 * This class utilizes a [AccessContext] to manage the lifecycle and configuration of Logback Access. The configuration
 * file can be provided using a [URL] or file name.
 *
 * The initialization process involves extracting the configuration from the provided file, setting up
 * the [AccessContext] with the specified configuration, applying the configuration using the [JoranConfigurator],
 * and assigning status listeners for tracking.
 *
 * Configuration debugging is supported and can be enabled through a constructor parameter for additional console-based
 * status outputs.
 *
 * Implements the [AccessLogFactory] interface and provides the [apply] method to create [AccessLog] instances.
 *
 * Java example of enabling Logback Access for Reactor Netty HTTP server:
 * ```
 * HttpServer.create()
 *           .accessLog(true, new ReactorNettyAccessLogFactory())
 *           .bindNow()
 *           .onDispose()
 *           .block();
 * ```
 *
 * @author Dmitry Sulman
 * @see AccessLog
 * @see AccessContext
 * @see AccessLogFactory
 */
class ReactorNettyAccessLogFactory : AccessLogFactory {
    /**
     * An instance of [AccessContext], which serves as the main context object for managing the state and components
     * involved in the access logging process. It provides facilities for managing appenders, filters, and status listeners
     * that are used during the logging lifecycle.
     *
     * This variable is central to configuring and handling the logging behavior specific to access events, including
     * processing log events, invoking filters, and dispatching logged events to appenders.
     *
     * Utilized primarily within the [ReactorNettyAccessLogFactory] for configuration and initialization tasks to ensure
     * proper setup of access logging systems.
     */
    val accessContext = AccessContext()

    /**
     * Constructs a new instance of [ReactorNettyAccessLogFactory] with the given configuration URL.
     *
     * This constructor initializes the instance using the provided [URL] for configuration,
     * a new [JoranConfigurator] instance, and debug mode set to `false`.
     *
     * @param config the [URL] pointing to the configuration file to be used for setup.
     */
    constructor(config: URL) {
        initialize(config, JoranConfigurator(), false)
    }

    /**
     * Constructor for [ReactorNettyAccessLogFactory] that initializes the factory with the specified configuration.
     *
     * @param config the [URL] of the configuration file to be used for setting up the access logging.
     * @param joranConfigurator the [JoranConfigurator] instance used to load and apply the configuration.
     * @param debug a boolean flag indicating whether debug mode is enabled; debug mode includes additional logging.
     */
    constructor(config: URL, joranConfigurator: JoranConfigurator, debug: Boolean) {
        initialize(config, joranConfigurator, debug)
    }

    /**
     * Constructs an instance of the [ReactorNettyAccessLogFactory] with a given configuration file name.
     *
     * This constructor initializes the factory by loading the configuration from the specified file name.
     * If the file is found, it retrieves the configuration and uses it to set up the factory with a new
     * [JoranConfigurator] instance, and debug mode set to `false`.
     *
     * @param fileName The name of the configuration file to load.
     * @throws FileNotFoundException If the specified configuration file is not found.
     */
    constructor(fileName: String) {
        initialize(getConfigFromFileName(fileName), JoranConfigurator(), false)
    }

    /**
     * Constructs a [ReactorNettyAccessLogFactory] instance and initializes it using the given parameters.
     *
     * @param fileName The file name of the configuration file to load.
     * @param joranConfigurator The JoranConfigurator instance for configuring the access context.
     * @param debug A flag indicating whether debug mode is enabled or not.
     */
    constructor(fileName: String, joranConfigurator: JoranConfigurator, debug: Boolean) {
        initialize(getConfigFromFileName(fileName), joranConfigurator, debug)
    }

    /**
     * Primary constructor for the [ReactorNettyAccessLogFactory] class.
     *
     * Initializes the access log factory with the configuration file, a new [JoranConfigurator] instance,
     * and debug mode set to `false`. The configuration file is taken from the `logback.access.reactor.netty.config`
     * system property or from the `logback-access.xml` file if the property is not set. If no default configuration
     * file is found, initialization will skip configuration steps.
     */
    constructor() {
        initialize(getDefaultConfig(), JoranConfigurator(), false)
    }

    private fun initialize(
        config: URL,
        joranConfigurator: JoranConfigurator,
        debug: Boolean,
    ) {
        try {
            addStatus(InfoStatus("Start configuring with configuration file [${config.file}]", this::class.java.simpleName))
            accessContext.name = config.file
            joranConfigurator.context = accessContext
            joranConfigurator.doConfigure(config)
            if (debug) {
                StatusListenerConfigHelper.addOnConsoleListenerInstance(accessContext, OnConsoleStatusListener())
            }
            accessContext.start()
            addStatus(InfoStatus("Done configuring", this::class.java.simpleName))
        } catch (e: Exception) {
            addStatus(ErrorStatus("Failed to configure ReactorNettyAccessLogFactory", this::class.java.simpleName, e))
            throw e
        }
        if (!debug) {
            StatusPrinter2().printInCaseOfErrorsOrWarnings(accessContext)
        }
    }

    private fun getDefaultConfig(): URL {
        val fileNameFromSystemProperty =
            System.getProperty(CONFIG_FILE_NAME_PROPERTY)?.also {
                addStatus(
                    InfoStatus(
                        "Found system property [$CONFIG_FILE_NAME_PROPERTY] value: [$it]",
                        this::class.java.simpleName,
                    ),
                )
            }
        return if (fileNameFromSystemProperty != null) {
            try {
                getConfigFromFileName(fileNameFromSystemProperty)
            } catch (e: FileNotFoundException) {
                addStatus(ErrorStatus(e.message, this::class.java.simpleName))
                throw e
            }
        } else {
            addStatus(
                InfoStatus(
                    "No system property [$CONFIG_FILE_NAME_PROPERTY] provided, checking [$DEFAULT_CONFIG_FILE_NAME]",
                    this::class.java.simpleName,
                ),
            )
            try {
                getConfigFromFileName(DEFAULT_CONFIG_FILE_NAME)
            } catch (_: FileNotFoundException) {
                addStatus(
                    InfoStatus("Not found [$DEFAULT_CONFIG_FILE_NAME], fallback to the default configuration", this::class.java.simpleName),
                )
                getResource(DEFAULT_CONFIGURATION)
            }
        }
    }

    private fun getConfigFromFileName(fileName: String): URL {
        val file = File(fileName)
        return if (file.exists()) {
            addStatus(InfoStatus("Found file [$fileName]", this::class.java.simpleName))
            file.toURI().toURL()
        } else {
            addStatus(InfoStatus("Not found file [$fileName], checking resource", this::class.java.simpleName))
            getResource(fileName)
        }
    }

    private fun getResource(fileName: String) =
        this::class.java.classLoader.getResource(fileName)?.also {
            addStatus(InfoStatus("Found resource [${it.file}]", this::class.java.simpleName))
        } ?: throw FileNotFoundException("Configuration file $fileName not found")

    private fun addStatus(status: Status) {
        accessContext.statusManager.add(status)
    }

    /**
     * Applies the provided [AccessLogArgProvider] to create a new [AccessLog] instance.
     *
     * @param argProvider the provider of arguments to building an [AccessLog] instance.
     * @return a new [AccessLog] instance.
     */
    override fun apply(argProvider: AccessLogArgProvider) = AccessLog(accessContext, argProvider)

    companion object {
        /**
         * The name of the system property used to specify the Logback Access configuration file.
         */
        const val CONFIG_FILE_NAME_PROPERTY = "logback.access.reactor.netty.config"

        /**
         * The default configuration file name used for the Logback Access configuration.
         */
        const val DEFAULT_CONFIG_FILE_NAME = "logback-access.xml"

        /**
         * The fallback configuration file path.
         */
        const val DEFAULT_CONFIGURATION = "logback-access-reactor-netty/logback-access-default-config.xml"
    }
}
