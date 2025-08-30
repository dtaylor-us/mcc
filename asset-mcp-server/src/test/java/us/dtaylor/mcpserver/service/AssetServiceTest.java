package us.dtaylor.mcpserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        asset1.setBrand("Brand A");
        asset1.setManualPath("file:///tmp/manual1.txt");
        asset1.setInstalledAt(Instant.now());
        assetRepository.save(asset1);

        asset2 = new Asset();
        asset2.setQrCode("QR456");
        asset2.setName("Pump Station");
        asset2.setModel("PS-100");
        asset2.setSerialNumber("SN-2");
        asset2.setBrand("Brand B");
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
