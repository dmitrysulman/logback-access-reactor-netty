package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.access.common.joran.JoranConfigurator
import org.springframework.core.env.Environment

class ReactorNettyJoranConfigurator(
    environment: Environment,
) : JoranConfigurator()
