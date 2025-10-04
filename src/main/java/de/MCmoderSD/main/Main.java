package de.MCmoderSD.main;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.core.CloudflareClient;
import de.MCmoderSD.core.Resolver;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.objects.DnsRecord;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

public class Main {

    public static void main(String[] args) {

        // Validate arguments
        if (args.length != 1) throw new IllegalArgumentException("Usage: java -jar MyFritz-Resolver.jar <config-path>");
        if (args[0] == null || args[0].isEmpty()) throw new IllegalArgumentException("Config path cannot be null or empty");

        // Load configuration
        JsonNode config;
        try {
            config = JsonUtility.getInstance().load(args[0], true);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to load configuration: " + e.getMessage(), e);
        }

        // Validate configuration
        if (config == null || config.isEmpty()) throw new IllegalArgumentException("Configuration cannot be null or empty");
        if (!config.has("zoneId")) throw new IllegalArgumentException("Configuration must have a zoneId");
        if (!config.has("apiToken")) throw new IllegalArgumentException("Configuration must have an apiToken");
        if (!config.has("delay")) throw new IllegalArgumentException("Configuration must have a delay");
        if (!config.has("records")) throw new IllegalArgumentException("Configuration must have records");
        if (config.get("zoneId") == null) throw new IllegalArgumentException("zoneId cannot be null");
        if (config.get("apiToken") == null) throw new IllegalArgumentException("apiToken cannot be null");
        if (config.get("delay") == null) throw new IllegalArgumentException("delay cannot be null");
        if (config.get("records").isEmpty() || !config.get("records").isArray()) throw new IllegalArgumentException("records must be a non-empty array");

        // Extract configuration values
        String zoneId = config.get("zoneId").asText();
        String apiToken = config.get("apiToken").asText();
        var delay = config.get("delay").asLong();

        // Validate configuration values
        if (zoneId.isBlank()) throw new IllegalArgumentException("zoneId cannot be blank");
        if (apiToken.isBlank()) throw new IllegalArgumentException("apiToken cannot be blank");
        if (delay <= 0) throw new IllegalArgumentException("delay must be greater than 0");

        // Initialize Cloudflare client
        CloudflareClient client = new CloudflareClient(zoneId, apiToken);

        // Get current DNS records
        HashSet<DnsRecord> dnsRecords = client.getDnsRecords();
        if (dnsRecords == null || dnsRecords.isEmpty()) throw new IllegalStateException("No DNS records found");

        // Process each record in the configuration
        for (var record : config.get("records")) {

            // Validate record
            if (record == null) throw new IllegalArgumentException("Record cannot be null");
            if (record.isEmpty()) throw new IllegalArgumentException("Record cannot be empty");
            if (!record.has("id")) throw new IllegalArgumentException("Record must have an id");
            if (!record.has("domain")) throw new IllegalArgumentException("Record must have a domain");
            if (record.get("id") == null) throw new IllegalArgumentException("Record must have an id");
            if (record.get("domain") == null) throw new IllegalArgumentException("Record must have a domain");

            // Extract id and domain
            String id = record.get("id").asText();
            String domain = record.get("domain").asText();

            // Validate id and domain
            if (id.isBlank()) throw new IllegalArgumentException("Record id cannot be blank");
            if (domain.isBlank()) throw new IllegalArgumentException("Record domain cannot be blank");

            // Find DNS record by id
            DnsRecord dnsRecord = dnsRecords.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
            if (dnsRecord == null) throw new IllegalArgumentException("No DNS record found for id: " + id);

            // Create Resolver for the domain
            new Resolver(client, dnsRecord, domain, delay * 1000L);
            System.out.println("Started resolver for domain: " + domain + " with record ID: " + id);
        }
    }
}