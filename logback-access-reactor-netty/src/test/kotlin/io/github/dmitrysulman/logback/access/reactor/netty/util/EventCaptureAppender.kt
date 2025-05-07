package io.github.dmitrysulman.logback.access.reactor.netty.util

import ch.qos.logback.access.common.spi.IAccessEvent
import ch.qos.logback.core.AppenderBase

class EventCaptureAppender : AppenderBase<IAccessEvent>() {
    val list = mutableListOf<IAccessEvent>()

    init {
        start()
    }

    override fun append(event: IAccessEvent) {
        list.add(event)
    }
}
