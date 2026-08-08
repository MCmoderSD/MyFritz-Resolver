# [MyFritz-Resolver](https://hub.docker.com/repository/docker/mcmodersd/myfritz-resolver/)

## Overview
MyFritz-Resolver is a Dynamic DNS (DynDNS) service that keeps your MyFritz! domains in sync with your current IP addresses (both IPv4 and IPv6).
It automatically updates DNS records on Cloudflare whenever your IP changes.

## Features
- Supports both IPv4 (A) and IPv6 (AAAA) records.
- Automatic DNS updates using the Cloudflare API.
- Lightweight and easy to deploy using Docker or Docker Compose.
- Multi-architecture images (`linux/amd64`, `linux/arm64`) published to both Docker Hub and GHCR.
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
  - [Image Tags & Registries](#image-tags--registries)

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
    - `nickname`: Reference name for your own use (not read by the application, purely for keeping your `config.json` readable).
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
The image runs as an unprivileged, non-root user by default.

### Docker Compose
`docker-compose.yaml` example:
```yaml
services:
  myfritz-resolver:
    image: mcmodersd/myfritz-resolver:latest
    container_name: MyFritz-Resolver
    restart: unless-stopped
    pull_policy: always
    volumes:
      - ./config.json:/app/config.json

networks:
  default:
    name: MyFritz-Resolver-Network
```
Start the service:
```bash
docker-compose up
```
To pin a specific version instead of always tracking `latest`, set `image` to e.g. `mcmodersd/myfritz-resolver:1.0.0` — see [Image Tags & Registries](#image-tags--registries) for available tags.

### Direct via Java
1. Install Java 25 or higher. Download from [Oracle](https://www.oracle.com/java/technologies/downloads/#java25).
2. Download the latest release jar from [GitHub Releases](https://github.com/MCmoderSD/MyFritz-Resolver/releases/latest). The asset is named `MyFritz-Resolver-<version>.jar` (e.g. `MyFritz-Resolver-1.0.0.jar`).
3. Run the resolver:
```bash
java -jar MyFritz-Resolver-<version>.jar config.json
```
Replace `<version>` with the version you downloaded, and `config.json` with your configuration path.

Each release includes MD5, SHA1 and SHA256 checksums in its release notes, plus a [build provenance attestation](https://github.com/MCmoderSD/MyFritz-Resolver/attestations) you can verify with `gh attestation verify`.

### Image Tags & Registries
Images are built for `linux/amd64` and `linux/arm64` and published to both registries below on every release:

| Registry   | Image                                | Tags                  |
|------------|--------------------------------------|-----------------------|
| Docker Hub | `mcmodersd/myfritz-resolver`         | `latest`, `<version>` |
| GHCR       | `ghcr.io/mcmodersd/myfritz-resolver` | `latest`, `<version>` |

Use `latest` to always track the newest release, or pin a `<version>` tag (e.g. `1.0.0`) for reproducible deployments.