# [MyFritz-Resolver](https://hub.docker.com/repository/docker/mcmodersd/myfritz-resolver/)


## Overview
MyFritz-Resolver is a Dynamic DNS (DDNS) service that keeps your MyFritz! domains in sync with your current IP addresses (both IPv4 and IPv6).
It automatically updates DNS records on Cloudflare whenever your IP changes.



## Features
- Supports both IPv4 (A) and IPv6 (AAAA) records.
- Automatic DNS updates using the Cloudflare API.
- Lightweight and easy to deploy using Docker or Docker Compose.
- Configurable check intervals for IP changes.



## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Configuration](#configuration)
    - [Permissions](#permissions)
    - [Record IDs](#record-ids)
- [Setup](#setup)
  - [Docker](#docker)
  - [Docker Compose](#docker-compose)
  - [Direct via Java](#direct-via-java)



## Configuration
Create a `config.json` file with the following structure:
```json
{
  "zoneId": "your_cloudflare_zone_id",
  "apiToken": "your_cloudflare_api_token",
  "delay": 300,
  "records": [
    {
      "nickname": "Friendly name for reference",
      "id": "cloudflare_record_id",
      "domain": "your_myfritz_domain.myfritz.net"
    },
    {
      "nickname": "Another record (IPv6)",
      "id": "another_cloudflare_record_id",
      "domain": "your_myfritz_domain.myfritz.net"
    }
  ]
}
```
- `zoneId`: Cloudflare Zone ID for your domain.
- `apiToken`: Cloudflare API token with DNS edit permissions.
- `delay`: Interval (seconds) to check for IP changes (default 300s).
- `records`: List of DNS records to manage.
    - `nickname`: Reference name.
    - `id`: Cloudflare record ID.
    - `domain`: MyFritz! domain name.

### Permissions
Create a custom Cloudflare API token with the following:
- Zone: DNS: Edit
- Zone: Zone: Read

### Record IDs
To get all DNS record IDs:
```bash
sudo apt update && sudo apt install jq -y

curl -s -X GET "https://api.cloudflare.com/client/v4/zones/YOUR_ZONE_ID/dns_records" \
     -H "Authorization: Bearer YOUR_API_TOKEN" \
     -H "Content-Type: application/json" \
| jq -r '.result[] | select(.type=="A" or .type=="AAAA") | "\(.id)\n\(.type)\n\(.name)\n\(.content)\n"'
```
Replace `YOUR_ZONE_ID` and `YOUR_API_TOKEN` with your actual values.

#### Example output:
```
023e105f4ecef8ad9ca31a8372d0c353    # Record ID
A                                   # Record type (IPv4)
example.com                         # Domain
172.253.118.100                     # IPv4 address

023e105f4ecef8ad9ca31a8372d0c354    # Record ID
AAAA                                # Record type (IPv6)
example.com                         # Domain
2404:6800:4003:c11::64              # IPv6 address
```



## Setup
You can run MyFritz-Resolver via Docker, Docker Compose, or directly on your system.

### Docker
```bash
docker run -d \
  --name MyFritz-Resolver \
  --restart unless-stopped \
  -v ${PWD}/config.json:/app/config.json \
  mcmodersd/myfritz-resolver:latest
```

### Docker Compose
`docker-compose.yaml` example:
```yaml
services:
  myfritz-resolver:
    image: mcmodersd/myfritz-resolver:latest
    container_name: MyFritz-Resolver
    restart: unless-stopped
    volumes:
      - ./config.json:/app/config.json
```
Start the service:
```bash
docker-compose up -d
```

### Direct via Java
1. Install Java 21 or higher. Download from [Oracle](https://www.oracle.com/java/technologies/downloads/#java21).
2. Download the latest release from [GitHub Releases](https://www.GitHub.com/MCmoderSD/MyFritz-Resolver/releases/latest).
3. Run the resolver:
```bash
java -jar MyFritz-Resolver.jar config.json
```
Replace `config.json` with your configuration path.