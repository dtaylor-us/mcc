package us.dtaylor.mcpserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.repository.WorkLogRepository;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link WorkLogController}. Uses MockMvc to invoke
 * endpoints and verifies basic CRUD behaviour.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class WorkLogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private WorkLogRepository workLogRepository;

    private Asset asset;

    @BeforeEach
    void setup() {
        workLogRepository.deleteAll();
        assetRepository.deleteAll();
        asset = new Asset();
        asset.setQrCode("QRWL");
        asset.setName("Work Asset");
        asset.setManualPath("file:///tmp/manual");
        asset.setInstalledAt(Instant.now());
        assetRepository.save(asset);
    }

    @Test
    void testCreateAndListWorkLogsEndpoint() throws Exception {
        UUID assetId = asset.getId();
        // Create work log via POST
        String body = objectMapper.writeValueAsString(new us.dtaylor.mcpserver.service.WorkLogService.CreateWorkLogRequest(
                assetId, "Fix pump", "Sam", 20, "Replaced parts"));
        mockMvc.perform(post("/api/worklogs/v1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.action", is("Fix pump")));
        // List and verify
        mockMvc.perform(get("/api/worklogs/v1").param("assetId", assetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].technician", is("Sam")));
    }
}