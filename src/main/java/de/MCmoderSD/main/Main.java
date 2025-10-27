package de.MCmoderSD.main;

import de.MCmoderSD.cloudflare.core.CloudflareClient;
import de.MCmoderSD.cloudflare.objects.DnsRecord;
import de.MCmoderSD.core.Resolver;
import de.MCmoderSD.json.JsonUtility;
import tools.jackson.databind.JsonNode;

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

        // Validate config
        if (config == null || config.isNull() || config.isEmpty()) throw new IllegalArgumentException("Configuration cannot be null or empty");
        if (!config.has("zoneId") || config.get("zoneId").isNull() || !config.get("zoneId").isString()) throw new IllegalArgumentException("Configuration must contain a valid 'zoneId' field");
        if (!config.has("apiToken") || config.get("apiToken").isNull() || !config.get("apiToken").isString()) throw new IllegalArgumentException("Configuration must contain a valid 'apiToken' field");
        if (!config.has("delay") || config.get("delay").isNull() || !config.get("delay").isNumber()) throw new IllegalArgumentException("Configuration must contain a valid 'delay' field");
        if (!config.has("records") || config.get("records").isNull() || !config.get("records").isArray() || config.get("records").isEmpty()) throw new IllegalArgumentException("Configuration must contain a valid 'records' array field");

        // Parse config
        String zoneId = config.get("zoneId").asString();
        String apiToken = config.get("apiToken").asString();
        var delay = config.get("delay").asLong();

        // Validate parsed config
        if (zoneId.isBlank()) throw new IllegalArgumentException("'zoneId' cannot be blank");
        if (apiToken.isBlank()) throw new IllegalArgumentException("'apiToken' cannot be blank");
        if (delay <= 0) throw new IllegalArgumentException("'delay' must be greater than 0");

        // Validate each record
        JsonNode records = config.get("records");
        var size = records.size();

        // Prepare arrays
        String[] id = new String[size];
        String[] domain = new String[size];

        // Parse records
        for (var i = 0; i < size; i++) {

            // Validate record
            var record = records.get(i);
            if (record == null || record.isNull() || record.isEmpty()) throw new IllegalArgumentException("Record at index " + i + " cannot be null or empty");
            if (!record.has("id") || record.get("id").isNull() || !record.get("id").isString()) throw new IllegalArgumentException("Record at index " + i + " must contain a valid 'id' field");
            if (!record.has("domain") || record.get("domain").isNull() || !record.get("domain").isString()) throw new IllegalArgumentException("Record at index " + i + " must contain a valid 'domain' field");

            // Extract fields
            id[i] = record.get("id").asString();
            domain[i] = record.get("domain").asString();

            // Validate fields
            if (id[i].isBlank()) throw new IllegalArgumentException("'id' in record at index " + i + " cannot be blank");
            if (domain[i].isBlank()) throw new IllegalArgumentException("'domain' in record at index " + i + " cannot be blank");
        }

        // Initialize Cloudflare client
        CloudflareClient client = new CloudflareClient(zoneId, apiToken);

        // Get current DNS records
        HashSet<DnsRecord> dnsRecords = client.getRecords();
        if (dnsRecords == null || dnsRecords.isEmpty()) throw new IllegalStateException("No DNS records found");

        // Process each record in the configuration
        for (var i = 0; i < size; i++) {

            // Find DNS record by id
            DnsRecord dnsRecord = client.getRecordMap().get(id[i]);
            if (dnsRecord == null) throw new IllegalArgumentException("No DNS record found for id: " + id[i]);

            // Create Resolver Thread for the domain
            new Resolver(client, dnsRecord, domain[i], delay * 1000L);
            System.out.println("Started resolver for domain: " + domain[i] + " with record ID: " + id[i]);
        }
    }
}