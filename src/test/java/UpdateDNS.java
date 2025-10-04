import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.core.CloudflareClient;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.objects.DnsRecord;
import enums.RecordType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

public class UpdateDNS {
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

        // Edit Record
        DnsRecord recordToEdit = dnsRecords.stream()
                .filter(r -> r.getType() == RecordType.A && r.getName().equals("dev.mcmodersd.de"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Record not found"));

        if (recordToEdit == null) throw new IllegalStateException("No DNS record found");

        boolean updated = client.updateDnsRecord(recordToEdit, "172.253.118.100");
        System.out.println("Updated: " + updated);
    }
}
