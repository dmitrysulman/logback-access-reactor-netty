package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.core.Context
import ch.qos.logback.core.model.Model
import ch.qos.logback.core.model.processor.ModelHandlerBase
import ch.qos.logback.core.model.processor.ModelInterpretationContext
import ch.qos.logback.core.util.OptionHelper
import org.springframework.core.env.Environment

class LogbackAccessSpringProfileModelHandler(
    context: Context,
    private val environment: Environment,
) : ModelHandlerBase(context) {
    override fun handle(
        mic: ModelInterpretationContext,
        model: Model,
    ) {
        if (model is LogbackAccessSpringProfileModel) {
            val profiles =
                model.name
                    .split(",")
                    .map { OptionHelper.substVars(it.trim(), mic, context) }
            if (profiles.isEmpty() || !environment.matchesProfiles(*profiles.toTypedArray())) {
                model.deepMarkAsSkipped()
            }
        }
    }
}
