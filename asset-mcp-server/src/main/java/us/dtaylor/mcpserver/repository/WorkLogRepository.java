package us.dtaylor.mcpserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import us.dtaylor.mcpserver.domain.WorkLog;

import java.util.UUID;

public interface WorkLogRepository extends JpaRepository<WorkLog, UUID> { }
