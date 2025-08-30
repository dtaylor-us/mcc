package us.dtaylor.mcpserver.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;

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

}
