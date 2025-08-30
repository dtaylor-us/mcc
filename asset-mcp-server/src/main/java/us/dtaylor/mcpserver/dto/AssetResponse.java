package us.dtaylor.mcpserver.dto;

public record AssetResponse(
        String id,
        String qrCode,
        String name,
        String model,
        String serialNumber,
        String brand,
        String manualPath,
        String qrImageUrl,
        String assetType
) {}
