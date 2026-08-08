package de.MCmoderSD.core;

import de.MCmoderSD.cloudflare.core.CloudflareClient;
import de.MCmoderSD.cloudflare.enums.RecordType;
import de.MCmoderSD.cloudflare.objects.DnsRecord;
import de.MCmoderSD.cloudflare.objects.ModifiedRecord;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;

import static java.lang.IO.println;
import static org.xbill.DNS.Type.A;
import static org.xbill.DNS.Type.AAAA;

@SuppressWarnings({"BusyWait", "unused"})
public class Resolver {

    private final Thread thread;

    // Constructor
    public Resolver(CloudflareClient client, DnsRecord record, String domain, long delay) {

        // Validate inputs
        if (client == null) throw new IllegalArgumentException("CloudflareClient cannot be null");
        if (record == null) throw new IllegalArgumentException("DNS record cannot be null");
        if (domain == null || domain.isEmpty()) throw new IllegalArgumentException("Domain cannot be null or empty");
        if (delay <= 0) throw new IllegalArgumentException("Delay must be greater than 0");

        // Initialize variables
        var type = record.getType();
        var threadName = record.getId() + " - " + type + " - " + domain;

        // Fail fast on record types that cannot be resolved
        if (type != RecordType.A && type != RecordType.AAAA) throw new IllegalArgumentException("Unsupported record type: " + type);

        // Resolver Thread
        thread = new Thread(() -> {

            // Initial IP resolution
            var ip = record.getContent();

            // Loop
            while (true) {
                try {

                    // Resolve IP based on record type
                    var resolvedIp = resolveIP(domain, type);

                    // Update DNS record if IP has changed
                    if (resolvedIp != null && !resolvedIp.equals(ip)) {

                        // Prepare modified record
                        var modifiedRecord = new ModifiedRecord(record);
                        modifiedRecord.modifyContent(resolvedIp);

                        // Perform update
                        var updated = client.updateRecord(modifiedRecord);

                        // Only accept the new IP once Cloudflare confirmed it, otherwise retry next iteration
                        if (updated) {
                            ip = resolvedIp;
                            println("[" + threadName + "] Updated " + type + " record to: " + ip);
                        } else {
                            println("[" + threadName + "] Failed to update " + type + " record to: " + resolvedIp + ". Retrying in " + delay + "ms");
                        }
                    }

                } catch (Exception e) {
                    println("[" + threadName + "] Error: " + e.getMessage());
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Log shutdown
            println("[" + threadName + "] Stopped");

        }, threadName);

        // Start thread
        thread.start();
    }

    // Resolve IP address based on record type
    private static String resolveIP(String domain, RecordType type) {
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

    // Setter
    public void stop() {
        thread.interrupt();
    }

    // Getter
    public boolean isAlive() {
        return thread.isAlive();
    }
}