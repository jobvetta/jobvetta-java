package com.jobvetta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Authenticated synchronous client for the Jobvetta REST API. */
public final class JobvettaClient {
    public static final URI DEFAULT_BASE_URI = URI.create("https://api.jobvetta.com/v1/");
    public static final String VERSION = "1.0.0";

    private static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    private final String apiKey;
    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /** Creates a client for the production Jobvetta API. */
    public JobvettaClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URI);
    }

    /** Creates a client with a custom base URI for controlled environments. */
    public JobvettaClient(String apiKey, URI baseUri) {
        this(apiKey, baseUri, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build(),
                new ObjectMapper());
    }

    JobvettaClient(String apiKey, URI baseUri, HttpClient httpClient, ObjectMapper objectMapper) {
        this.apiKey = normalizeRequired(apiKey, "API key");
        this.baseUri = normalizeBaseUri(baseUri);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    /** Searches current India jobs checked against official employer sources. */
    public SearchJobsResponse searchJobs(SearchOptions options) {
        options = options == null ? new SearchOptions() : options;
        validateRange("days", options.days(), 1, 365);
        validateRange("limit", options.limit(), 1, 10);

        List<String> query = new ArrayList<>();
        addQuery(query, "q", options.query());
        addQuery(query, "location", options.location());
        addQuery(query, "days", options.days());
        addQuery(query, "limit", options.limit());

        String suffix = query.isEmpty() ? "" : "?" + String.join("&", query);
        return get(URI.create(baseUri.resolve("jobs") + suffix), SearchJobsResponse.class);
    }

    /** Retrieves full details for a job ID returned by {@link #searchJobs(SearchOptions)}. */
    public JobDetail getJob(String jobId) {
        String normalizedId = normalizeRequired(jobId, "job ID");
        return get(baseUri.resolve("jobs/" + encode(normalizedId)), JobDetail.class);
    }

    private <T> T get(URI uri, Class<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", "jobvetta-java/" + VERSION)
                .GET()
                .build();

        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new JobvettaException("Jobvetta request was interrupted", exception);
        } catch (IOException exception) {
            throw new JobvettaException("Jobvetta request failed: " + exception.getMessage(), exception);
        }

        byte[] bytes = response.body();
        if (bytes.length > MAX_RESPONSE_BYTES) {
            throw new JobvettaException("Jobvetta response exceeded 8 MiB");
        }
        if (bytes.length == 0) {
            throw new JobvettaException("Jobvetta returned an empty response");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            JsonNode body = parseBody(bytes);
            String message = body.path("error").asText();
            if (message.isBlank()) {
                message = "Jobvetta request failed with status " + response.statusCode();
            }
            throw new JobvettaException(message, response.statusCode(), body);
        }

        try {
            return objectMapper.readValue(bytes, responseType);
        } catch (IOException exception) {
            throw new JobvettaException("Jobvetta could not decode the response", exception);
        }
    }

    private JsonNode parseBody(byte[] bytes) {
        try {
            return objectMapper.readTree(bytes);
        } catch (JsonProcessingException exception) {
            return TextNode.valueOf(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            return TextNode.valueOf(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    private static URI normalizeBaseUri(URI value) {
        Objects.requireNonNull(value, "baseUri");
        if (value.getHost() == null || !("http".equals(value.getScheme()) || "https".equals(value.getScheme()))) {
            throw new JobvettaException("Jobvetta base URI must use HTTP or HTTPS");
        }
        String normalized = value.toString();
        return URI.create(normalized.endsWith("/") ? normalized : normalized + "/");
    }

    private static String normalizeRequired(String value, String name) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new JobvettaException("Jobvetta " + name + " is required");
        }
        return normalized;
    }

    private static void validateRange(String name, Integer value, int minimum, int maximum) {
        if (value != null && (value < minimum || value > maximum)) {
            throw new JobvettaException(
                    "Jobvetta " + name + " must be from " + minimum + " to " + maximum);
        }
    }

    private static void addQuery(List<String> query, String name, String value) {
        if (value != null && !value.trim().isEmpty()) {
            query.add(name + "=" + encode(value.trim()));
        }
    }

    private static void addQuery(List<String> query, String name, Integer value) {
        if (value != null) {
            query.add(name + "=" + value);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
