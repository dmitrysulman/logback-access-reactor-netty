package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.access.common.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.ElementSelector
import ch.qos.logback.core.joran.spi.RuleStore
import ch.qos.logback.core.model.Model
import ch.qos.logback.core.model.processor.DefaultProcessor
import org.springframework.core.env.Environment
import java.util.function.Supplier

class ReactorNettyJoranConfigurator(
    private val environment: Environment,
) : JoranConfigurator() {
    override fun addElementSelectorAndActionAssociations(rs: RuleStore) {
        super.addElementSelectorAndActionAssociations(rs)
        rs.addRule(ElementSelector("*/springProfile"), ::SpringProfileAction)
        rs.addTransparentPathPart("springProfile")
    }

    override fun sanityCheck(topModel: Model) {
        super.sanityCheck(topModel)
    }

    override fun addModelHandlerAssociations(defaultProcessor: DefaultProcessor) {
        defaultProcessor.addHandler(SpringProfileModel::class.java) { _, _ ->
            SpringProfileModelHandler(context, environment)
        }
        super.addModelHandlerAssociations(defaultProcessor)
    }

    override fun buildModelInterpretationContext() {
        super.buildModelInterpretationContext()
        modelInterpretationContext.configuratorSupplier =
            Supplier {
                ReactorNettyJoranConfigurator(environment).also { it.context = this.context }
            }
    }
}
