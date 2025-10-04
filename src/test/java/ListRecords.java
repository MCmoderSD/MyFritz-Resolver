import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.core.CloudflareClient;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.objects.DnsRecord;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

public class ListRecords {
    public static void main(String[] args) throws IOException, URISyntaxException {

        // Load configuration
        JsonNode config = JsonUtility.getInstance().load("/config.json");

        String zoneId = config.get("zoneId").asText();
        String apiToken = config.get("apiToken").asText();

        // Initialize Cloudflare client
        CloudflareClient client = new CloudflareClient(zoneId, apiToken);

        // Get current DNS records
        HashSet<DnsRecord> dnsRecords = client.getDnsRecords();
        if (dnsRecords == null || dnsRecords.isEmpty()) throw new IllegalStateException("No DNS records found");
        for (var record : dnsRecords) {
            System.out.println("--------------------------------");
            System.out.println("ID: " + record.getId());
            System.out.println("Name: " + record.getName());
            System.out.println("Type: " + record.getType());
            System.out.println("Content: " + record.getContent());
            System.out.println("Proxiable: " + record.isProxiable());
            System.out.println("Proxied: " + record.isProxied());
            System.out.println("TTL: " + record.getTtl());
            System.out.println("Comment: " + record.getComment());
            System.out.println("Created: " + record.getCreated());
            System.out.println("Modified: " + record.getModified());
            System.out.println("--------------------------------");
        }
    }
}
