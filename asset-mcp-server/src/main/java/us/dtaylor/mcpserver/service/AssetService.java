package us.dtaylor.mcpserver.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@Service
public class AssetService {
    private final AssetRepository repo;

    public AssetService(AssetRepository repo) {
        this.repo = repo;
    }

    public Optional<Asset> findByQrOrId(String qrOrId) {
        try {
            return repo.findById(UUID.fromString(qrOrId));
        } catch (IllegalArgumentException ignore) {
        }
        return repo.findByQrCode(qrOrId);
    }

    /**
     * Search (or list all when query is blank) with pagination.
     */
    public Page<Asset> search(String query, int page, int size) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.min(Math.max(size, 1), 200); // cap to avoid abuse
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize, Sort.by("name").ascending());
        return repo.search(query, pageable);
    }


    public Asset getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Asset not found: " + id));
    }

    public Asset getByQr(String qrCode) {
        return repo.findByQrCode(qrCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Asset not found for QR: " + qrCode));
    }

    /**
     * Reads a short preview of the manual pointed to by asset.manualPath.
     * Supports file:// URIs in dev. For http(s) we return a stub with no preview.
     */
    public ManualPreview getManualPreview(UUID assetId, int maxChars) {
        Asset asset = getById(assetId);
        String manualPath = asset.getManualPath();
        if (manualPath == null || manualPath.isBlank()) {
            throw new ResponseStatusException(NOT_FOUND, "Manual path not set for asset " + assetId);
        }

        try {
            URI uri = URI.create(manualPath);
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                var path = Paths.get(uri);
                StringBuilder sb = new StringBuilder();
                boolean truncated = false;
                try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    int c;
                    while ((c = br.read()) != -1) {
                        sb.append((char) c);
                        if (sb.length() >= maxChars) {
                            truncated = true;
                            break;
                        }
                    }
                }
                return new ManualPreview(asset.getId(), manualPath, sb.toString(), truncated);
            } else if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                // For security, we don’t fetch remote here. UI can “open full manual”.
                return new ManualPreview(asset.getId(), manualPath,
                        "[Preview not available for non-file manuals. Open the full manual link.]", true);
            } else {
                throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE, "Unsupported manual scheme: " + uri.getScheme());
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE, "Invalid manual URI: " + manualPath, e);
        } catch (IOException e) {
            throw new ResponseStatusException(NOT_FOUND, "Manual not found/readable at: " + manualPath, e);
        }


    }

    public static class ManualPreview {
        UUID assetId;
        String manualPath;
        String preview;    // truncated text
        boolean truncated;//

        public ManualPreview(UUID assetId, String manualPath, String preview, boolean truncated) {
            this.assetId = assetId;
            this.manualPath = manualPath;
            this.preview = preview;
            this.truncated = truncated;
        }
    }
}
