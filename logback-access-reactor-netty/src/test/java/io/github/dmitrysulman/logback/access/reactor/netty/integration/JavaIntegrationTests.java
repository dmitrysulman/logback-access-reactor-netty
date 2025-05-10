package io.github.dmitrysulman.logback.access.reactor.netty.integration;

import ch.qos.logback.access.common.joran.JoranConfigurator;
import io.github.dmitrysulman.logback.access.reactor.netty.ReactorNettyAccessLogFactory;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaIntegrationTests {
    @Test
    public void smokeTest() throws InterruptedException {
        var accessLogFactory =
                new ReactorNettyAccessLogFactory("logback-access-stdout.xml", new JoranConfigurator(), true);
        var eventCaptureAppender = (EventCaptureAppender) accessLogFactory.getAccessContext().getAppender("CAPTURE");
        var responseBody = "test";
        var server = HttpServer
                .create()
                .accessLog(true, accessLogFactory)
                .handle((request, response) -> response.sendByteArray(Mono.just(responseBody.getBytes())))
                .bindNow();
        var uri = "/test";
        var response = HttpClient
                .create()
                .port(server.port())
                .get()
                .uri(uri)
                .response()
                .block(Duration.ofSeconds(10));

        assertNotNull(response);
        assertEquals(200, response.status().code());

        Thread.sleep(100);
        assertEquals(1, eventCaptureAppender.getList().size());
        assertEquals(uri, eventCaptureAppender.getList().get(0).getRequestURI());
        assertEquals(responseBody.length(), eventCaptureAppender.getList().get(0).getContentLength());

        server.disposeNow();
    }
}
