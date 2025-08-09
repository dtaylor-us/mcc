package us.dtaylor.mcpserver.service;

import org.springframework.stereotype.Service;
import us.dtaylor.mcpserver.domain.WorkLog;
import us.dtaylor.mcpserver.repository.WorkLogRepository;

@Service
public class WorkLogService {
    private final WorkLogRepository repo;
    public WorkLogService(WorkLogRepository repo) { this.repo = repo; }

    public WorkLog create(WorkLog wl) { return repo.save(wl); }
}
