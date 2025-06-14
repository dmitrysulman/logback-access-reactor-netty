package io.github.dmitrysulman.logback.access.reactor.netty.autoconfigure.integration

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EchoController {
    @GetMapping("/get")
    fun getRequest(
        @RequestParam param: String,
    ): ResponseEntity<String> = ResponseEntity.status(200).header("response_header", "response_header_value").body(param)
}
