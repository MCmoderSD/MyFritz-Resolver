package de.MCmoderSD.objects;

import com.fasterxml.jackson.databind.JsonNode;
import enums.RecordType;

import java.sql.Timestamp;

public class DnsRecord {

    // Attributes
    private final String id;
    private final String name;
    private final RecordType type;
    private final String content;
    private final boolean proxiable;
    private final boolean proxied;
    private final int ttl;
    private final Timestamp created;
    private final Timestamp modified;

    // Constructor
    public DnsRecord(JsonNode dnsRecord) {

        // Check dnsRecord
        if (dnsRecord == null) throw new IllegalArgumentException("Record cannot be null");
        if (dnsRecord.isEmpty()) throw new IllegalArgumentException("Record cannot be empty");
        if (!dnsRecord.has("id")) throw new IllegalArgumentException("Record must have an id");
        if (!dnsRecord.has("name")) throw new IllegalArgumentException("Record must have a name");
        if (!dnsRecord.has("type")) throw new IllegalArgumentException("Record must have a type");
        if (!dnsRecord.has("content")) throw new IllegalArgumentException("Record must have a content");
        if (!dnsRecord.has("proxiable")) throw new IllegalArgumentException("Record must have a proxiable");
        if (!dnsRecord.has("proxied")) throw new IllegalArgumentException("Record must have a proxied");
        if (!dnsRecord.has("ttl")) throw new IllegalArgumentException("Record must have a ttl");
        if (!dnsRecord.has("created_on")) throw new IllegalArgumentException("Record must have a created_on");
        if (!dnsRecord.has("modified_on")) throw new IllegalArgumentException("Record must have a modified_on");

        // Set attributes
        id = dnsRecord.get("id").asText();
        name = dnsRecord.get("name").asText();
        type = RecordType.fromString(dnsRecord.get("type").asText());
        content = dnsRecord.get("content").asText();
        proxiable = dnsRecord.get("proxiable").asBoolean();
        proxied = dnsRecord.get("proxied").asBoolean();
        ttl = dnsRecord.get("ttl").asInt();
        created = Timestamp.valueOf(dnsRecord.get("created_on").asText().replace("T", " ").replace("Z", ""));
        modified = Timestamp.valueOf(dnsRecord.get("modified_on").asText().replace("T", " ").replace("Z", ""));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RecordType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public boolean isProxiable() {
        return proxiable;
    }

    public boolean isProxied() {
        return proxied;
    }

    public int getTtl() {
        return ttl;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getModified() {
        return modified;
    }
}