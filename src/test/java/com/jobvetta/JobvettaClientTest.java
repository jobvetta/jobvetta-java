package com.jobvetta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobvettaClientTest {
    private HttpServer server;
    private URI baseUri;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        baseUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/");
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void searchesJobsWithTypedFilters() {
        AtomicReference<HttpExchange> captured = new AtomicReference<>();
        server.createContext("/v1/jobs", exchange -> {
            captured.set(exchange);
            respond(exchange, 200, """
                    {"total":1,"jobs":[{"job_id":"job-1","title":"Java Developer",
                    "company":"Example","location":"Pune",
                    "url":"https://www.jobvetta.com/jobs/job-1"}]}
                    """);
        });

        JobvettaClient client = new JobvettaClient("jvk_test", baseUri);
        SearchJobsResponse response = client.searchJobs(new SearchOptions()
                .withQuery(" java developer ")
                .withLocation(" Pune ")
                .withDays(7)
                .withLimit(10));

        assertEquals(1, response.total());
        assertEquals("Java Developer", response.jobs().get(0).title());
        assertEquals("q=java%20developer&location=Pune&days=7&limit=10",
                captured.get().getRequestURI().getRawQuery());
        assertEquals("Bearer jvk_test", captured.get().getRequestHeaders().getFirst("Authorization"));
        assertEquals("jobvetta-java/1.0.0", captured.get().getRequestHeaders().getFirst("User-Agent"));
    }

    @Test
    void encodesJobIdsAndSurfacesApiErrors() {
        server.createContext("/v1/jobs/", exchange -> {
            assertEquals("/v1/jobs/abc%2F123", exchange.getRequestURI().getRawPath());
            respond(exchange, 404, "{\"error\":\"Job not found\"}");
        });

        JobvettaClient client = new JobvettaClient("jvk_test", baseUri);
        JobvettaException exception = assertThrows(
                JobvettaException.class, () -> client.getJob("abc/123"));

        assertEquals(404, exception.statusCode());
        assertEquals("Job not found", exception.getMessage());
    }

    @Test
    void validatesConfigurationAndRangesBeforeRequesting() {
        assertThrows(JobvettaException.class, () -> new JobvettaClient("  "));

        JobvettaClient client = new JobvettaClient("jvk_test", baseUri);
        JobvettaException exception = assertThrows(
                JobvettaException.class,
                () -> client.searchJobs(new SearchOptions().withDays(366)));
        assertTrue(exception.getMessage().contains("days must be from 1 to 365"));
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
