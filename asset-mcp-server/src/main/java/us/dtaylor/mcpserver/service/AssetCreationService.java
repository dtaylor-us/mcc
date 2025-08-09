package us.dtaylor.mcpserver.service;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.service.storage.QrStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

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

    @Transactional
    public Asset createWithQr(Asset asset) throws Exception {
        // Ensure qrCode exists (allow user to supply, or generate one)
        if (asset.getQrCode() == null || asset.getQrCode().isBlank()) {
            asset.setQrCode(generateShortCode()); // e.g., QR-ABC123
        }

        // Persist first to get an ID (if needed)
        Asset saved = repo.save(asset);

        // Build scan URL (what the QR encodes)
        String qrData = scanBaseUrl + "/" + saved.getQrCode();

        // Generate PNG to a temp file (then hand to storage)
        String fileName = saved.getQrCode() + ".png";
        Path tmp = Files.createTempDirectory("qrgen").resolve(fileName);
        qrCodeService.generateQrCode(qrData, tmp);

        // Store & get a public URL (local file mapping or S3/Blob URL)
        String publicUrl = qrStorage.storeAndGetPublicUrl(tmp, fileName);

        // Save URL on the asset
        saved.setQrImagePath(publicUrl);
        return repo.save(saved);
    }

    private String generateShortCode() {
        // simple, friendly code: QR-<first8 of UUID> (customize to your needs)
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
