package de.MCmoderSD.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.MCmoderSD.objects.DnsRecord;
import enums.RecordType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;

public class CloudflareClient {

    // Endpoint URLs
    private static final String BASE_URL = "https://api.cloudflare.com/client/v4/zones/";

    // Credentials
    private final String zoneId;

    // Attributes
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final HttpRequest.Builder requestBuilder;

    // Constructor
    public CloudflareClient(String zoneId, String apiToken) {

        // Set Zone ID
        this.zoneId = zoneId;

        // Initialize Attributes
        mapper = new ObjectMapper();
        client = HttpClient.newHttpClient();

        // Initialize Request Builder
        requestBuilder = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json");
    }

    // List DNS records
    public HashSet<DnsRecord> getDnsRecords() {
        try {

            // Create request
            HttpRequest request = requestBuilder
                    .uri(new URI(BASE_URL + zoneId + "/dns_records"))
                    .GET()
                    .build();

            // Send request
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check response
            if (httpResponse.statusCode() != 200) throw new RuntimeException("Failed : HTTP error code : " + httpResponse.statusCode());
            var body = httpResponse.body();
            if (body == null) throw new RuntimeException("Response body is null");
            if (body.isBlank()) throw new RuntimeException("Response body is empty");

            // Parse response
            JsonNode response = mapper.readTree(body);
            if (response == null) throw new RuntimeException("Response body is null");
            if (response.isEmpty()) throw new RuntimeException("Response body is empty");
            if (!response.has("result") || response.get("result").isEmpty() || !response.get("result").isArray()) throw new RuntimeException("Response body does not contain result");
            if (!response.has("success") || !response.get("success").asBoolean()) throw new RuntimeException("Response body indicates failure");

            // Convert to set of Records
            HashSet<DnsRecord> dnsRecords = new HashSet<>();
            var result = response.get("result");
            for (var record : result) dnsRecords.add(new DnsRecord(record));
            return dnsRecords;

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get DNS records", e);
        }
    }

    // Update A or AAAA record
    public boolean updateDnsRecord(DnsRecord record, String content) {
        try {

            // Validate record
            if (record == null) throw new IllegalArgumentException("Record cannot be null");

            // Extract record details
            RecordType type = record.getType();

            // Validate input
            if (!(type == RecordType.A || type == RecordType.AAAA)) throw new IllegalArgumentException("Record type must be A or AAAA");
            if (content == null || content.isBlank()) throw new IllegalArgumentException("Content cannot be null or blank");

            // Create a JSON object for the request body
            JsonNode bodyNode = mapper.createObjectNode()
                    .put("type", type.name())
                    .put("name", record.getName())
                    .put("content", content);

            // Convert JSON object to string
            String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyNode);

            // Create request
            HttpRequest request = requestBuilder
                    .uri(new URI(BASE_URL + zoneId + "/dns_records/" + record.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // Send request
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check response
            if (httpResponse.statusCode() != 200) throw new RuntimeException("Failed : HTTP error code : " + httpResponse.statusCode());
            var responseBody = httpResponse.body();
            if (responseBody == null) throw new RuntimeException("Response body is null");
            if (responseBody.isBlank()) throw new RuntimeException("Response body is empty");

            // Parse response
            JsonNode response = mapper.readTree(responseBody);
            if (response == null) throw new RuntimeException("Response body is null");
            if (response.isEmpty()) throw new RuntimeException("Response body is empty");
            if (!response.has("result") || response.get("result").isEmpty()) throw new RuntimeException("Response body does not contain result");
            if (!response.has("success") || !response.get("success").asBoolean()) throw new RuntimeException("Response body indicates failure");
            DnsRecord updatedRecord = new DnsRecord(response.get("result"));

            // Check if content was updated
            return  updatedRecord.getContent().equals(content) && !updatedRecord.getContent().equals(record.getContent()) && updatedRecord.getId().equals(record.getId());

        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException("Failed to update DNS record", e);
        }
    }
}