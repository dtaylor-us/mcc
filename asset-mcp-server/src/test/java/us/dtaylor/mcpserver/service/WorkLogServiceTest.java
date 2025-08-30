package us.dtaylor.mcpserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.domain.WorkLog;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.repository.WorkLogRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link WorkLogService}. Uses an in-memory database via
 * {@link DataJpaTest}.
 */
@DataJpaTest
@ContextConfiguration(classes = {WorkLogServiceTest.Config.class})
public class WorkLogServiceTest {

    @Autowired
    private WorkLogService workLogService;
    @Autowired
    private AssetRepository assetRepository;

    private Asset asset;

    @BeforeEach
    void setup() {
        asset = new Asset();
        asset.setQrCode("QRWORK");
        asset.setName("Widget");
        asset.setManualPath("file:///tmp/dummy");
        asset.setInstalledAt(Instant.now());
        assetRepository.save(asset);
    }

    @Test
    void testCreateAndListWorkLogs() {
        WorkLogService.CreateWorkLogRequest req1 = new WorkLogService.CreateWorkLogRequest(
                asset.getId(), "Replaced filter", "Sam", 15, "All good");
        WorkLog wl1 = workLogService.create(req1);
        WorkLogService.CreateWorkLogRequest req2 = new WorkLogService.CreateWorkLogRequest(
                asset.getId(), "Lubricated bearings", "Alex", 10, null);
        WorkLog wl2 = workLogService.create(req2);
        // Should return in reverse chronological order
        List<WorkLog> logs = workLogService.listForAsset(asset.getId());
        assertEquals(2, logs.size());
        assertThat(logs.get(0).getId()).isEqualTo(wl2.getId());
        assertThat(logs.get(1).getId()).isEqualTo(wl1.getId());
    }

    @Test
    void testCreateWithMissingAsset() {
        UUID missingId = UUID.randomUUID();
        WorkLogService.CreateWorkLogRequest req = new WorkLogService.CreateWorkLogRequest(
                missingId, "Test", "Bob", 5, "Notes");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> workLogService.create(req));
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
    }

    /**
     * Minimal configuration supplying the service under test. Uses real
     * repositories and services.
     */
    @EntityScan("us.dtaylor.mcpserver.domain")
    @EnableJpaRepositories("us.dtaylor.mcpserver.repository")
    static class Config {

        @Bean
        public AssetService assetService(AssetRepository assetRepository) {
            // Use the real implementation for tests
            return new AssetService(assetRepository);
        }

        @Bean
        WorkLogService workLogService(WorkLogRepository workLogRepository, AssetService assetService) {
            return new WorkLogService(workLogRepository, assetService);
        }
    }
}
