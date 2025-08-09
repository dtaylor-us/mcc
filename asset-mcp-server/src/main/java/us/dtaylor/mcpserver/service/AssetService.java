package us.dtaylor.mcpserver.service;

import org.springframework.stereotype.Service;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class AssetService {
    private final AssetRepository repo;
    public AssetService(AssetRepository repo) { this.repo = repo; }

    public Optional<Asset> findByQrOrId(String qrOrId) {
        try { return repo.findById(UUID.fromString(qrOrId)); }
        catch (IllegalArgumentException ignore) { }
        return repo.findByQrCode(qrOrId);
    }
}
