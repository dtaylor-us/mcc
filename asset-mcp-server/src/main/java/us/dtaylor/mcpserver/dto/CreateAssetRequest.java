package us.dtaylor.mcpserver.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

// Incoming payload
public record CreateAssetRequest(
        String qrCode, // optional; auto-generated if blank
        @NotBlank String name,
        String model,
        String serialNumber,
        String brand,
        String assetType,
        @NotBlank String manualPath, // e.g. file:/... or s3://...
        Instant installedAt
) {}
