package io.github.dmitrysulman.logback.access.reactor.netty.joran

import ch.qos.logback.core.model.NamedModel

/**
 * Logback Access [NamedModel] to support `<springProfile>` tags.
 *
 * See [SpringProfileModel](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/logging/logback/SpringProfileModel.java).
 *
 * @author Dmitry Sulman
 * @see [LogbackAccessSpringProfileAction]
 * @see [LogbackAccessSpringProfileModelHandler]
 */
class LogbackAccessSpringProfileModel : NamedModel()
