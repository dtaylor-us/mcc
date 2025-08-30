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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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
        Asset asset = repo.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + assetId));

        String manualPath = normalizeManualPath(asset.getManualPath());
        if (manualPath == null || manualPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manual path not set for asset " + assetId);
        }

        try {
            URI uri = URI.create(manualPath);
            String scheme = (uri.getScheme() == null ? "" : uri.getScheme().toLowerCase());

            // only preview file: URIs; http(s) returns a safe hint
            if ("file".equals(scheme)) {
                var path = Paths.get(uri);
                var sb = new StringBuilder();
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
                return new ManualPreviewDto(asset.getId(), manualPath, sb.toString(), truncated);
            } else if ("http".equals(scheme) || "https".equals(scheme)) {
                return new ManualPreviewDto(
                        asset.getId(),
                        manualPath,
                        "[Preview not available for non-file manuals. Open the full manual link.]",
                        true
                );
            } else {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported manual scheme: " + scheme);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Invalid manual URI: " + manualPath, e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manual not found/readable at: " + manualPath, e);
        }
    }

    private static String normalizeManualPath(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim()
                .replace("\u200B", "").replace("\uFEFF", "")
                .replace('\u2011', '-').replace('\u2013', '-').replace('\u2014', '-');

        String lower = s.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
            return s;
        }
        if (lower.startsWith("file://")) {
            return "file:/" + s.substring("file://".length());
        }
        if (s.startsWith("/")) {
            return "file:" + s;                          // Unix absolute
        }
        if (s.matches("^[A-Za-z]:[\\\\/].*")) {
            return "file:/" + s.replace("\\", "/"); // Windows absolute
        }
        return s;
    }

}
