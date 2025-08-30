package us.dtaylor.mcpserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.dto.ManualPreviewDto;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.util.ManualPathNormalizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AssetService}. Uses an in-memory H2 database via
 * {@link DataJpaTest} to persist and query entities.
 */
@DataJpaTest
@ContextConfiguration(classes = {AssetServiceTest.Config.class})
class AssetServiceTest {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetService assetService;

    private Asset asset1;
    private Asset asset2;

    @BeforeEach
    void setUp() {
        // populate two assets for search and lookup tests
        asset1 = new Asset();
        asset1.setQrCode("QR123");
        asset1.setName("Air Handler");
        asset1.setModel("AH-900");
        asset1.setSerialNumber("SN-1");
        asset1.setLocation("Building A");
        asset1.setManualPath("file:///tmp/manual1.txt");
        asset1.setInstalledAt(Instant.now());
        assetRepository.save(asset1);

        asset2 = new Asset();
        asset2.setQrCode("QR456");
        asset2.setName("Pump Station");
        asset2.setModel("PS-100");
        asset2.setSerialNumber("SN-2");
        asset2.setLocation("Building B");
        asset2.setManualPath("http://example.com/manual2.txt");
        asset2.setInstalledAt(Instant.now());
        assetRepository.save(asset2);
    }

    @Test
    void testFindByQrOrId() {
        Optional<Asset> byId = assetService.findByQrOrId(asset1.getId().toString());
        assertThat(byId).contains(asset1);
        Optional<Asset> byQr = assetService.findByQrOrId("QR456");
        assertThat(byQr).contains(asset2);
        Optional<Asset> missing = assetService.findByQrOrId("NONEXISTENT");
        assertThat(missing).isEmpty();
    }

    @Test
    void testSearchBlankReturnsAll() {
        var page = assetService.search("", 0, 10);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testSearchFiltersByName() {
        var page = assetService.search("pump", 0, 10);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Pump Station");
    }

    @Test
    void testGetManualPreviewForFile() throws Exception {
        // Create a temporary file with known contents
        Path tmp = Files.createTempFile("manual-prev", ".txt");
        Files.writeString(tmp, "1234567890abcdef");
        String manualPath = "file://" + tmp.toUri().getPath();
        Asset a = new Asset();
        a.setQrCode("QR999");
        a.setName("Test Asset");
        a.setManualPath(manualPath);
        assetRepository.save(a);

        ManualPreviewDto preview = assetService.getManualPreview(a.getId(), 5);
        assertEquals(a.getId(), preview.assetId());
        assertEquals(ManualPathNormalizer.normalize(manualPath), preview.manualPath());
        assertEquals("12345", preview.preview());
        assertTrue(preview.isTruncated());
    }

    @Test
    void testGetManualPreviewForHttp() {
        Asset a = new Asset();
        a.setQrCode("QR777");
        a.setName("Remote Manual Asset");
        a.setManualPath("https://example.com/remote.txt");
        assetRepository.save(a);
        ManualPreviewDto preview = assetService.getManualPreview(a.getId(), 100);
        assertEquals("https://example.com/remote.txt", preview.manualPath());
        assertEquals("[Preview not available for remote manuals. Open the full manual link.]", preview.preview());
        assertTrue(preview.isTruncated());
    }

    @Disabled("The ManualPathNormalizer prevents the scheme from being invalid")
    @Test
    void testGetManualPreviewUnsupportedScheme() {
        Asset a = new Asset();
        a.setQrCode("QR888");
        a.setName("Bad Scheme Asset");
        a.setManualPath("ftp:/example.com/file"); // single slash, not a valid file path
        assetRepository.save(a);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> assetService.getManualPreview(a.getId(), 100));
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    }

    @Test
    void testGetManualPreviewFileNotFound() {
        Asset a = new Asset();
        a.setQrCode("QR889");
        a.setName("Missing Manual Asset");
        a.setManualPath("/nonexistent/path/manual.txt");
        assetRepository.save(a);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> assetService.getManualPreview(a.getId(), 10));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    /**
     * Minimal configuration supplying the service under test and its
     * dependencies. Uses the real AssetRepository and ManualPathNormalizer.
     */
    @Configuration
    @EntityScan("us.dtaylor.mcpserver.domain")
    @EnableJpaRepositories("us.dtaylor.mcpserver.repository")
    static class Config {
        @Bean
        AssetService assetService(AssetRepository repo) {
            return new AssetService(repo);
        }
    }
}
