package us.dtaylor.mcpserver.api;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.dto.AssetResponse;
import us.dtaylor.mcpserver.dto.CreateAssetRequest;
import us.dtaylor.mcpserver.service.AssetCreationService;

import java.time.Instant;

@RestController
@RequestMapping("/api/assets/v1")
@Validated
public class AssetController {

    private final AssetCreationService createService;

    public AssetController(AssetCreationService createService) {
        this.createService = createService;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@RequestBody @Validated CreateAssetRequest req) throws Exception {
        Asset a = new Asset();
        a.setQrCode(req.qrCode());
        a.setName(req.name());
        a.setModel(req.model());
        a.setSerialNumber(req.serialNumber());
        a.setLocation(req.location());
        a.setManualPath(req.manualPath());
        a.setInstalledAt(req.installedAt() == null ? Instant.now() : req.installedAt());

        Asset saved = createService.createWithQr(a);

        return ResponseEntity.ok(new AssetResponse(
                saved.getId().toString(),
                saved.getQrCode(),
                saved.getName(),
                saved.getModel(),
                saved.getSerialNumber(),
                saved.getLocation(),
                saved.getManualPath(),
                saved.getQrImagePath()
        ));
    }
}
