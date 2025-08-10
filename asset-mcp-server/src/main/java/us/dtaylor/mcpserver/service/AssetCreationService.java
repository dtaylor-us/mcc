package us.dtaylor.mcpserver.service;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.service.storage.QrStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Service responsible for creating new {@link Asset} instances.  When a new
 * asset is created this service will generate a QR code image, persist
 * the asset entity and return the persisted instance with the QR image
 * URL populated.  The scan URL used when generating the QR code is
 * configurable via application properties.
 */
@Service
public class AssetCreationService {

    private final AssetRepository repo;
    private final QrCodeService qrCodeService;
    private final QrStorage qrStorage;
    private final String scanBaseUrl;

    public AssetCreationService(
            AssetRepository repo,
            QrCodeService qrCodeService,
            QrStorage qrStorage,
            @Value("${app.qr.scanBaseUrl}") String scanBaseUrl) {
        this.repo = repo;
        this.qrCodeService = qrCodeService;
        this.qrStorage = qrStorage;
        this.scanBaseUrl = scanBaseUrl.endsWith("/") ? scanBaseUrl.substring(0, scanBaseUrl.length()-1) : scanBaseUrl;
    }

    /**
     * Creates a new asset, generating a QR code if necessary and storing
     * the resulting image.  The QR code encodes a URL composed of the
     * configured scan base URL and the asset's QR code.  If no QR code
     * was provided on the entity a random one is generated.
     *
     * @param asset the asset to create
     * @return the persisted asset with QR image URL populated
     */
    @Transactional
    public Asset createWithQr(Asset asset) throws Exception {
        // Ensure qrCode exists (allow user to supply, or generate one)
        if (asset.getQrCode() == null || asset.getQrCode().isBlank()) {
            asset.setQrCode(generateShortCode()); // e.g., QR-ABC123
        }
        // default installation timestamp if missing
        if (asset.getInstalledAt() == null) {
            asset.setInstalledAt(Instant.now());
        }
        // persist to obtain an ID
        Asset saved = repo.save(asset);

        // Build scan URL (what the QR encodes)
        String qrData = scanBaseUrl + "/" + saved.getQrCode();
        // generate QR image into a temporary location
        Path tmpDir = Files.createTempDirectory("qr-asset");
        Path tmpFile = tmpDir.resolve(saved.getQrCode() + ".png");
        qrCodeService.generatePng(qrData, tmpFile);
        // store the image and obtain a public URL
        String qrImageUrl = qrStorage.storeAndGetPublicUrl(tmpFile, saved.getQrCode() + ".png");
        // update asset with image URL
        saved.setQrImagePath(qrImageUrl);
        return repo.save(saved);
    }

    private String generateShortCode() {
        // simple, friendly code: QR-<first8 of UUID> (customize to your needs)
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
