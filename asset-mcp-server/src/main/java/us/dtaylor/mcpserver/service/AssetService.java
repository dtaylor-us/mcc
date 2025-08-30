package us.dtaylor.mcpserver.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.dto.ManualPreviewDto;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.util.ManualPathNormalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public ManualPreviewDto getManualPreview(UUID assetId, int maxChars) {
        Asset asset = getById(assetId);
        String raw = asset.getManualPath();
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(NOT_FOUND, "Manual path not set for asset " + assetId);
        }

        String normalized = ManualPathNormalizer.normalize(raw);

        try {
            URI uri = URI.create(normalized);
            String scheme = uri.getScheme();

            if ("file".equalsIgnoreCase(scheme)) {
                Path path = Paths.get(uri);
                if (!Files.exists(path) || !Files.isReadable(path)) {
                    throw new ResponseStatusException(NOT_FOUND, "Manual not found/readable at: " + path);
                }

                // stream up to maxChars
                StringBuilder sb = new StringBuilder(Math.min(maxChars, 8192));
                try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    int c;
                    while ((c = reader.read()) != -1) {
                        sb.append((char) c);
                        if (sb.length() >= maxChars) {
                            break;
                        }
                    }
                }
                boolean truncated = Files.size(path) > sb.length();
                return new ManualPreviewDto(asset.getId(), normalized, sb.toString(), truncated);
            }

            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                // Still avoid remote fetch for security; UI provides "Open Full Manual".
                return new ManualPreviewDto(asset.getId(), normalized,
                        "[Preview not available for remote manuals. Open the full manual link.]", true);
            }

            throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE, "Unsupported manual scheme: " + scheme);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE, "Invalid manual path/URI: " + raw, e);
        } catch (IOException e) {
            throw new ResponseStatusException(NOT_FOUND, "Failed to read manual: " + raw, e);
        }
    }
}
