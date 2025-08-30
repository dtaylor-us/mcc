package us.dtaylor.mcpserver.dto;

import java.util.UUID;

public record ManualPreviewDto (
    UUID assetId,
    String manualPath,
    String preview,
    boolean isTruncated
) {}
