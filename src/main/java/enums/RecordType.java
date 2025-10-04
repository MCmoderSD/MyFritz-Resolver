package enums;

@SuppressWarnings("SpellCheckingInspection")
public enum RecordType {

    // Values
    A,
    AAAA,
    CAA,
    CERT,
    CNAME,
    DNSKEY,
    DS,
    HTTPS,
    LOC,
    MX,
    NAPTR,
    NS,
    PTR,
    SMIMEA,
    SRV,
    SSHFP,
    SVCB,
    TLSA,
    TXT,
    URI;

    // Methods
    public static RecordType fromString(String type) {
        if (type == null) throw new IllegalArgumentException("Type cannot be null");
        if (type.isBlank()) throw new IllegalArgumentException("Type cannot be blank");
        for (var recordType : RecordType.values()) if (recordType.name().equalsIgnoreCase(type)) return recordType;
        throw new IllegalArgumentException("Unknown record type: " + type);
    }
}