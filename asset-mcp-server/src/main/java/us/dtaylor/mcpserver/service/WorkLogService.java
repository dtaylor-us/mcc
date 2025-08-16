package us.dtaylor.mcpserver.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.domain.WorkLog;
import us.dtaylor.mcpserver.repository.WorkLogRepository;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class WorkLogService {
    private final WorkLogRepository repo;
    private final AssetService assetService;
    public WorkLogService(WorkLogRepository repo, AssetService assetService) { this.repo = repo;
        this.assetService = assetService;
    }

    public WorkLog create(WorkLog wl) { return repo.save(wl); }

    public List<WorkLog> listForAsset(UUID assetId) {
        return repo.findByAsset_IdOrderByCreatedAtDesc(assetId);
    }

    @Transactional
    public WorkLog create(CreateWorkLogRequest req) {
        Asset asset = assetService.getById(req.assetId());
        WorkLog wl = new WorkLog();
        wl.setAsset(asset);
        wl.setAction(req.action());
        wl.setTechnician(req.technician());
        wl.setDurationMinutes(req.durationMinutes());
        wl.setNotes(req.notes());

        return repo.save(wl);
    }

    // DTO for POST body (matches your UI omission of id/createdAt)
    public record CreateWorkLogRequest(
            UUID assetId,
            String action,
            String technician,
            Integer durationMinutes,
            String notes
    ) {}
}
