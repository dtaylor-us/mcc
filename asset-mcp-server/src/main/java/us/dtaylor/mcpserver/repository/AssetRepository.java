package us.dtaylor.mcpserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import us.dtaylor.mcpserver.domain.Asset;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findByQrCode(String qr);

    /**
     * Case-insensitive search across name, model, serialNumber, and location.
     * If :query is blank/null, all assets are returned (paginated).
     */
    @Query("""
        SELECT a
        FROM Asset a
        WHERE (:query IS NULL OR :query = '' OR
              LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.model) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.location) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Asset> search(@Param("query") String query, Pageable pageable);
}
