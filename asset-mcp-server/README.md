

# Asset MCP Server

The **Asset MCP Server** is a Spring Boot 3.5.4 + Java 21 application that exposes REST APIs to manage assets, generate QR codes for them, and store metadata in PostgreSQL. It can be used standalone or as part of an **Agent + MCP** ecosystem.

---

## Features

* **Asset management**: Create, retrieve, and list assets.
* **QR code generation**: Auto-generates a QR code image when an asset is created.
* **Manual storage paths**: Supports linking to local or cloud-hosted manuals.
* **PostgreSQL persistence**: Stores all asset and QR metadata.
* **RESTful API**: JSON-based API for easy integration.
* **Docker-ready**: Containerized for deployment in Azure or other environments.

---

## Prerequisites

* **Java 21**
* **Maven 3.9+**
* **PostgreSQL 15+**
* (Optional) **Docker** & **Docker Compose**

---

## Project Structure

```
asset-mcp-server/
 ├── src/main/java/com/example/mcpserver/    # Java code
 ├── src/main/resources/
 │    ├── application.yml                    # Main application config
 │    ├── initdb/01-init.sql                  # DB schema + seed data
 ├── pom.xml                                  # Maven build file
 └── README.md
```

---

## Running Locally

### 1. Start PostgreSQL

You can use Docker for local testing:

```bash
docker run --name assetdb \
  -e POSTGRES_USER=assetuser \
  -e POSTGRES_PASSWORD=assetpass \
  -e POSTGRES_DB=assetdb \
  -p 5432:5432 \
  -d postgres:15
```

### 2. Build & Run the Application

```bash
mvn clean package
java -jar target/asset-mcp-server-0.0.1-SNAPSHOT.jar
```

The server will start at:

```
http://localhost:8081
```

---

## API Usage

### Create a New Asset

**Request**

```bash
curl -X POST http://localhost:8081/api/assets/v1 \
  -H 'Content-Type: application/json' \
  -d '{
        "name": "Air Handler - 7th Floor",
        "model": "AH-900",
        "serialNumber": "SN-0001",
        "location": "Building A / Floor 7 / Mech Room",
        "manualPath": "file:///manuals/air-handler.txt"
      }'
```

**Response**

```json
{
  "id": "576033ea-924c-4273-8f86-7aae5f1e9579",
  "qrCode": "QR-25D96D76",
  "name": "Air Handler - 7th Floor",
  "model": "AH-900",
  "serialNumber": "SN-0001",
  "location": "Building A / Floor 7 / Mech Room",
  "manualPath": "file:///manuals/air-handler.txt",
  "qrImageUrl": "http://localhost:8081/qr-images/QR-25D96D76.png"
}
```

---

### Retrieve an Asset by ID

```bash
curl http://localhost:8081/api/assets/v1/576033ea-924c-4273-8f86-7aae5f1e9579
```

---

### Access the QR Code

Open the `qrImageUrl` in your browser or download it:

```bash
curl -o qr.png http://localhost:8081/qr-images/QR-25D96D76.png
```

---

## Configuration

Edit `src/main/resources/application.yml` to match your environment:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/assetdb
    username: assetuser
    password: assetpass
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

---

## Running in Docker

A sample `Dockerfile` is included:

```bash
docker build -t asset-mcp-server .
docker run -p 8081:8081 --env-file .env asset-mcp-server
```

You can also integrate with **Docker Compose** alongside PostgreSQL.

---

## Deployment to Azure

* Push image to **Azure Container Registry**.
* Deploy to **Azure App Service** or **Azure Kubernetes Service**.
* Use **Azure PostgreSQL Flexible Server** for production persistence.
* Store manuals & QR code images in **Azure Blob Storage**.

---

## Security Best Practices

* Use strong PostgreSQL credentials.
* Run over HTTPS in production.
* Restrict file paths for `manualPath`.
* Store QR code images in a secured cloud storage bucket (e.g., Azure Blob, S3).
* Enable authentication/authorization for API endpoints.

---

## License

MIT License

---
