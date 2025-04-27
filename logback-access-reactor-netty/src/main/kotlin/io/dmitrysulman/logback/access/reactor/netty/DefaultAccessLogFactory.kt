package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import java.net.URL

// TODO take config from classpath or form the jvm arg?
class DefaultAccessLogFactory(config: URL) : AbstractAccessLogFactory(JoranConfigurator(), config)