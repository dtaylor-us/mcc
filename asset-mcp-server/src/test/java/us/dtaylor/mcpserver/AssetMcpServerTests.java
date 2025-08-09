package us.dtaylor.mcpserver;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.WorkLogRepository;
import us.dtaylor.mcpserver.tools.AssetTools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the asset MCP server.  These tests exercise the
 * controller layer via MockMvc and call tool methods directly to
 * verify expected behaviour and edge cases.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = AssetMcpServerTests.ManualFileInitializer.class)
public class AssetMcpServerTests {

    /**
     * Provides a temporary manual file and overrides application properties
     * so that QR images and manuals are stored in an isolated location for
     * each test run.
     */
    static class ManualFileInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            // Create a temporary directory under the system temp directory
            Path tmp = Path.of(System.getProperty("java.io.tmpdir"), "asset-mcp-test" + System.nanoTime());
            // Override QR storage directory and manual path base
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "app.qr.storage.local.dir=" + tmp.toString(),
                    "app.qr.storage.local.publicBaseUrl=http://localhost:8081/qr-images",
                    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.datasource.platform=h2",
                    "spring.sql.init.data-locations=classpath:data-h2.sql" // <-- force H2 data file for tests
            );
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetTools assetTools;

    @Autowired
    private WorkLogRepository workLogRepository;

    /**
     * Helper method to create a temporary manual file with the given
     * contents and return its URI string.  The file is placed in the
     * JUnit-managed {@code @TempDir} directory so that it is cleaned up
     * automatically.
     */
    private String createManualFile(Path dir, String content) throws IOException {
        Path file = dir.resolve("manual.txt");
        java.nio.file.Files.createDirectories(dir);
        java.nio.file.Files.writeString(file, content);
        return "file:" + file.toAbsolutePath();
    }

    /**
     * Tests that creating an asset returns a generated QR code and image
     * URL.  A simple manual is used and the response is validated for
     * required fields.
     */
    @Test
    void createAsset_ReturnsQrAndImageUrl(@TempDir Path temp) throws Exception {
        String manualUri = createManualFile(temp, "Simple manual content");
        Map<String, Object> request = Map.of(
                "name", "Pump",
                "model", "P1",
                "serialNumber", "SN001",
                "location", "Room 1",
                "manualPath", manualUri
        );
        mvc.perform(post("/api/assets/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.qrCode").exists())
                .andExpect(jsonPath("$.qrImageUrl").exists());
    }

    /**
     * Tests that a missing required field results in a 400 Bad Request.
     */
    @Test
    void createAsset_MissingName_ReturnsBadRequest(@TempDir Path temp) throws Exception {
        String manualUri = createManualFile(temp, "Manual");
        // missing name
        Map<String, Object> request = Map.of(
                "model", "M1",
                "serialNumber", "SN002",
                "location", "Room 2",
                "manualPath", manualUri
        );
        mvc.perform(post("/api/assets/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests that the asset.search tool returns NOT_FOUND when no asset
     * matches the provided QR or ID.
     */
    @Test
    void assetSearch_NotFound_ReturnsNotFound() {
        AssetTools.AssetResponse resp = assetTools.search("nonexistent");
        assertThat(resp.status()).isEqualTo("NOT_FOUND");
        assertThat(resp.id()).isNull();
    }

    /**
     * Tests that manual.get throws when the asset does not exist.
     */
    @Test
    void manualGet_ThrowsWhenAssetMissing() {
        assertThrows(IllegalArgumentException.class, () -> assetTools.manual("00000000-0000-0000-0000-000000000000"));
    }

    /**
     * Tests that manual.get truncates the manual content to 2000 characters.
     */
    @Test
    void manualGet_TruncatesPreview(@TempDir Path temp) throws Exception {
        // create a long manual > 3000 characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3100; i++) {
            sb.append('A');
        }
        String manualUri = createManualFile(temp, sb.toString());
        // create asset via tool injection (bypassing controller) to avoid concurrency issues
        Asset a = new Asset();
        a.setQrCode("QR-LONGTEST");
        a.setName("Long Manual Asset");
        a.setManualPath(manualUri);
        a.setInstalledAt(java.time.Instant.now());
        // persist asset so tool can find it; using AssetService would require autowire; we can call search after create
        // For simplicity we call AssetTools.search after saving via AssetCreateService; this will happen via controller
        // Build request to create asset
        Map<String, Object> request = Map.of(
                "qrCode", "QR-LONGTEST",
                "name", "Long Manual Asset",
                "manualPath", manualUri
        );
        mvc.perform(post("/api/assets/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // Now call manual.get via tool
        AssetTools.AssetResponse resp = assetTools.search("QR-LONGTEST");
        assertThat(resp.status()).isEqualTo("OK");
        AssetTools.ManualResponse manualResp = assetTools.manual(resp.id());
        assertThat(manualResp.preview().length()).isEqualTo(2000);
        assertThat(manualResp.previewChars()).isEqualTo(2000);
    }

    /**
     * Tests that worklog.create persists a new WorkLog entity.
     */
    @Test
    void worklogCreate_PersistsEntry(@TempDir Path temp) throws Exception {
        // create asset first
        String manualUri = createManualFile(temp, "Manual");
        Map<String, Object> request = Map.of(
                "qrCode", "QR-WLTEST",
                "name", "Pump",
                "manualPath", manualUri
        );
        mvc.perform(post("/api/assets/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // search to get ID
        AssetTools.AssetResponse resp = assetTools.search("QR-WLTEST");
        // call worklog.create
        AssetTools.CreateWorklogRequest wlReq = new AssetTools.CreateWorklogRequest(
                resp.id(), "Inspection", "Alice", 15, "Checked filters");
        int before = (int) workLogRepository.count();
        assetTools.createWorklog(wlReq);
        int after = (int) workLogRepository.count();
        assertThat(after).isEqualTo(before + 1);
    }
}
