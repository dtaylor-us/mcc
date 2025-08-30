package us.dtaylor.mcpserver.api;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.dto.AssetResponse;
import us.dtaylor.mcpserver.dto.CreateAssetRequest;
import us.dtaylor.mcpserver.dto.ManualPreviewDto;
import us.dtaylor.mcpserver.service.AssetCreationService;
import us.dtaylor.mcpserver.service.AssetService;
import us.dtaylor.mcpserver.util.ManualPathNormalizer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets/v1")
@Validated
public class AssetController {

    private final AssetCreationService createService;
    private final AssetService assetService;

    public AssetController(AssetCreationService createService, AssetService assetService) {
        this.createService = createService;
        this.assetService = assetService;
    }

    /**
     * GET /api/assets/v1?query=&page=0&size=20
     * Returns only the array of assets (to match your UI expectation),
     * but includes pagination metadata in headers (X-Total-Count, etc).
     */
    @GetMapping
    public ResponseEntity<List<Asset>> listAssets(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        Page<Asset> result = assetService.search(query, page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        headers.add("X-Page", String.valueOf(result.getNumber()));
        headers.add("X-Size", String.valueOf(result.getSize()));

        // Body is just the array, as your fetch() expects Asset[]
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }

    // GET /api/assets/v1/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Asset> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(assetService.getById(id));
    }

    // GET /api/assets/v1/by-qr/{qrCode}
    @GetMapping("/by-qr/{qrCode}")
    public ResponseEntity<Asset> getByQr(@PathVariable String qrCode) {
        return ResponseEntity.ok(assetService.getByQr(qrCode));
    }

    // GET /api/assets/v1/{id}/manual/preview
    @GetMapping(value = "/{id}/manual/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ManualPreviewDto> getManualPreview(
            @PathVariable UUID id,
            @RequestParam(name = "maxChars", defaultValue = "2000") int maxChars) {
        return ResponseEntity.ok(assetService.getManualPreview(id, maxChars));
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@RequestBody @Validated CreateAssetRequest req) throws Exception {
        Asset a = new Asset();
        a.setQrCode(req.qrCode());
        a.setName(req.name());
        a.setModel(req.model());
        a.setSerialNumber(req.serialNumber());
        a.setLocation(req.location());
        a.setManualPath(ManualPathNormalizer.normalize(req.manualPath()));
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
