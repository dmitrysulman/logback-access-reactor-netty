package io.dmitrysulman.logback.access.reactor.netty

import ch.qos.logback.access.common.joran.JoranConfigurator
import java.net.URL

class DefaultAccessLogFactory(config: URL) : AbstractAccessLogFactory(
    joranConfigurator = JoranConfigurator(),
    config = config
) {
}