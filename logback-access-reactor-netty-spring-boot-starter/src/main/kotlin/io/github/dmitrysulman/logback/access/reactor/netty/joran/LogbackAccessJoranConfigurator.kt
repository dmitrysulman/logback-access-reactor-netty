package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.ElementSelector
import ch.qos.logback.core.joran.spi.RuleStore
import ch.qos.logback.core.model.Model
import ch.qos.logback.core.model.processor.DefaultProcessor
import org.springframework.core.env.Environment
import java.util.function.Supplier

/**
 * Extended version of the Logback Access [JoranConfigurator] that adds support of `<springProfile>` tags.
 *
 * See [SpringBootJoranConfigurator](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/logging/logback/SpringBootJoranConfigurator.java).
 */
class LogbackAccessJoranConfigurator(
    private val environment: Environment,
) : JoranConfigurator() {
    override fun addElementSelectorAndActionAssociations(rs: RuleStore) {
        super.addElementSelectorAndActionAssociations(rs)
        rs.addRule(ElementSelector("*/springProfile"), ::LogbackAccessSpringProfileAction)
        rs.addTransparentPathPart("springProfile")
    }

    override fun sanityCheck(topModel: Model) {
        super.sanityCheck(topModel)
        performCheck(LogbackAccessSpringProfileWithinSecondPhaseElementSanityChecker(), topModel)
    }

    override fun addModelHandlerAssociations(defaultProcessor: DefaultProcessor) {
        defaultProcessor.addHandler(LogbackAccessSpringProfileModel::class.java) { _, _ ->
            LogbackAccessSpringProfileModelHandler(context, environment)
        }
        super.addModelHandlerAssociations(defaultProcessor)
    }

    override fun buildModelInterpretationContext() {
        super.buildModelInterpretationContext()
        modelInterpretationContext.configuratorSupplier =
            Supplier {
                LogbackAccessJoranConfigurator(environment).also { it.context = this.context }
            }
    }
}
