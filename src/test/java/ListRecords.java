import de.MCmoderSD.cloudflare.core.CloudflareClient;
import de.MCmoderSD.cloudflare.objects.DnsRecord;
import de.MCmoderSD.json.JsonUtility;
import tools.jackson.databind.JsonNode;

import java.util.HashSet;

void main() {

    // Load configuration
    JsonNode config = JsonUtility.getInstance().loadResource("/config.json");

    String zoneId = config.get("zoneId").asString();
    String apiToken = config.get("apiToken").asString();

    // Initialize Cloudflare client
    CloudflareClient client = new CloudflareClient(zoneId, apiToken);

    // Get current DNS records
    HashSet<DnsRecord> dnsRecords = client.getRecords();
    if (dnsRecords == null || dnsRecords.isEmpty()) throw new IllegalStateException("No DNS records found");
    for (var record : dnsRecords) {
        IO.println("ID: " + record.getId());
        IO.println("Name: " + record.getName());
        IO.println("Type: " + record.getType());
        IO.println("Content: " + record.getContent());
        IO.println("Proxiable: " + record.isProxiable());
        IO.println("Proxied: " + record.isProxied());
        IO.println("TTL: " + record.getTtl());
        IO.println("Comment: " + record.getComment());
        IO.println("Created: " + record.getCreated());
        IO.println("Modified: " + record.getModified());
        IO.println("--------------------------------");
    }
}