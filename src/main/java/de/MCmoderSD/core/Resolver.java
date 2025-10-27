package de.MCmoderSD.core;

import de.MCmoderSD.cloudflare.core.CloudflareClient;
import de.MCmoderSD.cloudflare.enums.RecordType;
import de.MCmoderSD.cloudflare.objects.DnsRecord;
import de.MCmoderSD.cloudflare.objects.ModifiedRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;

import static org.xbill.DNS.Type.*;

@SuppressWarnings("BusyWait")
public class Resolver {

    // Constructor
    public Resolver(CloudflareClient client, DnsRecord record, String domain, long delay) {

        // Validate inputs
        if (client == null) throw new IllegalArgumentException("CloudflareClient cannot be null");
        if (record == null) throw new IllegalArgumentException("DNS record cannot be null");
        if (domain == null || domain.isEmpty()) throw new IllegalArgumentException("Domain cannot be null or empty");
        if (delay <= 0) throw new IllegalArgumentException("Delay must be greater than 0");

        // Initialize variables
        RecordType type = record.getType();
        String threadName = record.getId() + " - " + type + " - " + domain;

        // Resolver Thread
        new Thread(() -> {

            // Initial IP resolution
            String ip = record.getContent();

            // Loop
            while (true) {
                try {

                    // Resolve IP based on record type
                    String resolvedIp = resolveIP(domain, type);

                    // Update DNS record if IP has changed
                    if (resolvedIp != null && !resolvedIp.equals(ip)) {

                        // Update record in Cloudflare
                        ip = resolvedIp;
                        ModifiedRecord modifiedRecord = new ModifiedRecord(record);
                        modifiedRecord.modifyContent(ip);

                        // Perform update
                        boolean updated = client.updateRecord(modifiedRecord);

                        // Log result
                        if (updated) System.out.println("[" + threadName + "] Updated " + type + " record to: " + ip);
                        else System.out.println("[" + threadName + "] No update needed for " + type + " record. Current IP: " + ip);
                    }

                    // Wait for next resolution
                    Thread.sleep(delay);

                } catch (InterruptedException e) {
                    throw new RuntimeException("Resolver thread interrupted", e);
                }
            }
        }, threadName).start();
    }

    // Resolve IP address based on record type
    private String resolveIP(String domain, RecordType type) {
        try {

            // Validate inputs
            if (domain == null || domain.isEmpty()) throw new IllegalArgumentException("Domain cannot be null or empty");
            if (type == null) throw new IllegalArgumentException("RecordType cannot be null");

            // Map RecordType to DNS Type
            var dnsType = switch (type) {
                case A -> A;
                case AAAA -> AAAA;
                default -> throw new IllegalArgumentException("Unsupported record type: " + type);
            };

            // Perform DNS lookup
            var records = new Lookup(domain, dnsType).run();
            if (records == null) return null;

            // Return the first matching record
            for (var record : records) if (record.getType() == dnsType) return record.rdataToString();
            return null;

        } catch (TextParseException e) {
            throw new RuntimeException("Failed to resolve domain: " + domain, e);
        }
    }
}