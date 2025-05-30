package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.core.joran.action.BaseModelAction
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext
import ch.qos.logback.core.model.Model
import org.xml.sax.Attributes

/**
 * Logback Access [BaseModelAction] for `<springProfile>` tags. Allows a section of a
 * Logback Access configuration to only be enabled when a specific profile is active.
 *
 * See [SpringProfileAction](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/logging/logback/SpringProfileAction.java).
 *
 * @see [LogbackAccessSpringProfileModel]
 * @see [LogbackAccessSpringProfileModelHandler]
 */
class LogbackAccessSpringProfileAction : BaseModelAction() {
    override fun buildCurrentModel(
        interpretationContext: SaxEventInterpretationContext,
        name: String,
        attributes: Attributes,
    ): Model =
        LogbackAccessSpringProfileModel().apply {
            this.name = attributes.getValue(NAME_ATTRIBUTE)
        }
}
