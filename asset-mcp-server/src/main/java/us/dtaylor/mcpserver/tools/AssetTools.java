package us.dtaylor.mcpserver.tools;

import com.fasterxml.jackson.annotation.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.domain.WorkLog;
import us.dtaylor.mcpserver.service.AssetService;
import us.dtaylor.mcpserver.service.WorkLogService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class AssetTools {
    private final AssetService assets;
    private final WorkLogService worklogs;
    private final AssetService assetService;

    public AssetTools(AssetService assets, WorkLogService worklogs, AssetService assetService) {
        this.assets = assets;
        this.worklogs = worklogs;
        this.assetService = assetService;
    }

    // ====== Tool 1: search asset ======
    @Tool(name = "asset.search", description = "Lookup an asset by QR code or asset UUID. Returns basic asset info.")
    public AssetResponse search(@JsonProperty("qr_or_id") String qrOrId) {
        Optional<Asset> opt = assets.findByQrOrId(qrOrId);
        return opt.map(AssetResponse::from).orElseGet(() -> new AssetResponse("NOT_FOUND", null, null, null, null, null));
    }

    // ====== Tool 2: create work log ======
    @Tool(name = "worklog.create", description = "Create a maintenance worklog for the asset. Use short action, optional notes, duration minutes, and technician.")
    public Map<String, Object> createWorklog(CreateWorklogRequest req) {
        var wl = new WorkLog();
        Asset asset = assetService.getById(UUID.fromString(req.assetId()));
        wl.setAsset(asset); // Validate asset exists
        wl.setTechnician(req.technician());
        wl.setAction(req.action());
        wl.setDurationMinutes(req.durationMinutes());
        wl.setNotes(req.notes());
        var saved = worklogs.create(wl);
        return Map.of("worklogId", saved.getId(), "status", "CREATED");
    }

    // ====== Tool 3: retrieve work logs for asset ======
    @Tool(name = "worklog.list", description = "List all worklogs for a given assetId (UUID).")
    public WorkLog[] listWorklogs(@JsonProperty("asset_id") String assetId) {
        var asset = assets.findByQrOrId(assetId).orElseThrow(() -> new IllegalArgumentException("asset not found"));
        var logs = worklogs.listForAsset(asset.getId());
        return logs.toArray(new WorkLog[0]);
    }

    // ==== DTOs ====
    @JsonClassDescription("Create worklog input")
    public record CreateWorklogRequest(
            @JsonProperty(value = "asset_id", required = true) String assetId,
            @JsonProperty(value = "action", required = true) String action,
            @JsonProperty(value = "technician", required = true) String technician,
            @JsonProperty(value = "duration_minutes", required = false) Integer durationMinutes,
            @JsonProperty(value = "notes", required = false) String notes) {
    }

    public record AssetResponse(String status, String id, String qrCode, String name, String model, String location) {
        static AssetResponse from(Asset a) {
            return new AssetResponse("OK", a.getId().toString(), a.getQrCode(), a.getName(), a.getModel(), a.getBrand());
        }
    }
}
