package us.dtaylor.mcpserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import us.dtaylor.mcpserver.domain.WorkLog;

import java.util.List;
import java.util.UUID;

public interface WorkLogRepository extends JpaRepository<WorkLog, UUID> {

    // Return all work logs for an asset ordered by newest first (for the UI table)
    List<WorkLog> findByAsset_IdOrderByCreatedAtDesc(UUID assetId);
}
