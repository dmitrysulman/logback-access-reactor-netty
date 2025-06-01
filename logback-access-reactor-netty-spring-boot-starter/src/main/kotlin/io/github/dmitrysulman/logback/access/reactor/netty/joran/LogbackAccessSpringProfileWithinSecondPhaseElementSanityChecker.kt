package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.core.joran.sanity.SanityChecker
import ch.qos.logback.core.model.AppenderModel
import ch.qos.logback.core.model.Model
import ch.qos.logback.core.spi.ContextAwareBase

/**
 * [SanityChecker] to ensure that `springProfile` elements are not nested
 * within second-phase elements.
 *
 * See [SpringProfileIfNestedWithinSecondPhaseElementSanityChecker](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/logging/logback/SpringProfileIfNestedWithinSecondPhaseElementSanityChecker.java).
 */
class LogbackAccessSpringProfileWithinSecondPhaseElementSanityChecker :
    ContextAwareBase(),
    SanityChecker {
    override fun check(model: Model?) {
        if (model == null) return

        val secondsPhaseModels = mutableListOf<Model>()

        SECOND_PHASE_TYPES.forEach {
            deepFindAllModelsOfType(it, secondsPhaseModels, model)
        }

        deepFindNestedSubModelsOfType(LogbackAccessSpringProfileModel::class.java, secondsPhaseModels)
            ?.takeIf { it.isNotEmpty() }
            ?.also {
                addWarn("<springProfile> elements cannot be nested within an <appender> element")
            }?.forEach {
                val first = it.first
                val second = it.second
                addWarn(
                    "Element <${first.tag}> at line ${first.lineNumber} contains a nested <${second.tag}> element at line ${second.lineNumber}",
                )
            }
    }

    companion object {
        private val SECOND_PHASE_TYPES =
            listOf(
                AppenderModel::class.java,
            )
    }
}
