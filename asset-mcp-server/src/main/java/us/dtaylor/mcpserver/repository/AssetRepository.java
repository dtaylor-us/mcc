package us.dtaylor.mcpserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import us.dtaylor.mcpserver.domain.Asset;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findByQrCode(String qr);
}
