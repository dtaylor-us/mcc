package us.dtaylor.mcpserver.api;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import us.dtaylor.mcpserver.domain.WorkLog;
import us.dtaylor.mcpserver.service.WorkLogService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/worklogs/v1")
public class WorkLogController {

    private final WorkLogService workLogService;

    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    // GET /api/worklogs/v1?assetId=<uuid>
    @GetMapping
    public ResponseEntity<List<WorkLog>> listForAsset(@RequestParam UUID assetId) {
        return ResponseEntity.ok(workLogService.listForAsset(assetId));
    }

    // POST /api/worklogs/v1
    @PostMapping
    public ResponseEntity<WorkLog> create(@RequestBody WorkLogService.CreateWorkLogRequest body) {
        return ResponseEntity.ok(workLogService.create(body));
    }
}
