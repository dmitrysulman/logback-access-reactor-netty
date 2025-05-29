package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.core.joran.action.BaseModelAction
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext
import ch.qos.logback.core.model.Model
import org.xml.sax.Attributes

class SpringProfileAction : BaseModelAction() {
    override fun buildCurrentModel(
        interpretationContext: SaxEventInterpretationContext,
        name: String,
        attributes: Attributes,
    ): Model =
        SpringProfileModel().apply {
            this.name = attributes.getValue(NAME_ATTRIBUTE)
        }
}
