package us.dtaylor.mcpserver.dto;

// Outgoing payload (trim to what you need)
public record AssetResponse(
        String id,
        String qrCode,
        String name,
        String model,
        String serialNumber,
        String location,
        String manualPath,
        String qrImageUrl
) {}
