package us.dtaylor.mcpserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.dto.CreateAssetRequest;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.service.QrCodeService;
import us.dtaylor.mcpserver.service.storage.QrStorage;

import java.nio.file.Path;
import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AssetController}. Utilises MockMvc to perform
 * HTTP requests against the in-memory server. QrCodeService and QrStorage
 * are mocked to avoid generating real images during tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AssetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QrCodeService qrCodeService;

    @MockBean
    private QrStorage qrStorage;

    private Asset asset;

    @BeforeEach
    void setup() throws Exception {
        assetRepository.deleteAll();
        // When QrCodeService.generateQrCode is called, do nothing
        Mockito.when(qrCodeService.generatePng(anyString(), any(Path.class))).thenReturn(Path.of("/tmp/dummy.png"));
        // When QrStorage.storeAndGetPublicUrl is called, return a predictable URL
        Mockito.when(qrStorage.storeAndGetPublicUrl(any(Path.class), anyString())).thenReturn("http://localhost/qr-images/test.png");

        // create one asset to test GET endpoints
        asset = new Asset();
        asset.setQrCode("QRTEST");
        asset.setName("Test Asset");
        asset.setModel("T-100");
        asset.setSerialNumber("SN-TEST");
        asset.setLocation("Lab");
        asset.setManualPath("file:///tmp/manualTest.txt");
        asset.setQrImagePath("https://cdn.example.com/qr/QR-12345.png");
        asset.setInstalledAt(Instant.now());
        assetRepository.save(asset);
    }

    @Test
    void testListAssetsEndpoint() throws Exception {
        mockMvc.perform(get("/api/assets/v1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Asset")));
    }

    @Test
    void testGetAssetByIdEndpoint() throws Exception {
        mockMvc.perform(get("/api/assets/v1/" + asset.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(asset.getId().toString())))
                .andExpect(jsonPath("$.name", is(asset.getName())));
    }

    @Test
    void testGetAssetByQrEndpoint() throws Exception {
        mockMvc.perform(get("/api/assets/v1/by-qr/" + asset.getQrCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode", is(asset.getQrCode())));
    }

    @Test
    void testCreateAssetEndpoint() throws Exception {
        CreateAssetRequest req = new CreateAssetRequest(
                null,
                "New Asset",
                "Model1",
                "SN1",
                "Warehouse",
                "file:///tmp/manualNew.txt",
                null
        );
        mockMvc.perform(post("/api/assets/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.qrImageUrl", is("http://localhost/qr-images/test.png")));
    }

    @Test
    void testGetManualPreviewEndpoint() throws Exception {
        // Create manual file
        java.nio.file.Path manualFile = java.nio.file.Files.createTempFile("manual-prev-int", ".txt");
        java.nio.file.Files.writeString(manualFile, "Hello Manual");
        asset.setManualPath("file://" + manualFile.toUri().getPath());
        assetRepository.save(asset);
        mockMvc.perform(get("/api/assets/v1/" + asset.getId() + "/manual/preview").param("maxChars", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preview", is("Hello")))
                .andExpect(jsonPath("$.isTruncated", is(true)));
    }
}
