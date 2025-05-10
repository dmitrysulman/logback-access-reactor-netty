package io.github.dmitrysulman.logback.access.reactor.netty.integration

import ch.qos.logback.access.common.spi.IAccessEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class TestFilter : Filter<IAccessEvent>() {
    override fun decide(event: IAccessEvent) =
        event
            .getRequestParameter("filter")
            ?.firstOrNull()
            ?.let { filterParam ->
                FilterReply.entries.find { it.name.equals(filterParam, true) }
            } ?: FilterReply.NEUTRAL
}
