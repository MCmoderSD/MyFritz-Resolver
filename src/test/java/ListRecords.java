import de.MCmoderSD.cloudflare.core.CloudflareClient;
import de.MCmoderSD.json.JsonUtility;

import static java.lang.IO.println;

void main() {

    // Load configuration
    var config = JsonUtility.getInstance().loadResource("/config.json");

    var zoneId = config.get("zoneId").asString();
    var apiToken = config.get("apiToken").asString();

    // Initialize Cloudflare client
    var client = new CloudflareClient(zoneId, apiToken);

    // Get current DNS records
    var dnsRecords = client.getRecords();
    if (dnsRecords == null || dnsRecords.isEmpty()) throw new IllegalStateException("No DNS records found");
    for (var record : dnsRecords) {
        println("ID: " + record.getId());
        println("Name: " + record.getName());
        println("Type: " + record.getType());
        println("Content: " + record.getContent());
        println("Proxiable: " + record.isProxiable());
        println("Proxied: " + record.isProxied());
        println("TTL: " + record.getTtl());
        println("Comment: " + record.getComment());
        println("Created: " + record.getCreated());
        println("Modified: " + record.getModified());
        println("--------------------------------");
    }
}