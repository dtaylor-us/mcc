package us.dtaylor.mcpserver.service;

import org.junit.jupiter.api.Test;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetServiceTest {

    @Test
    void testFindByQrOrId_withUuid() {
        AssetRepository repo = mock(AssetRepository.class);
        AssetService service = new AssetService(repo);

        UUID uuid = UUID.randomUUID();
        Asset asset = new Asset();
        when(repo.findById(uuid)).thenReturn(Optional.of(asset));

        Optional<Asset> result = service.findByQrOrId(uuid.toString());
        assertTrue(result.isPresent());
        verify(repo).findById(uuid);
        verify(repo, never()).findByQrCode(anyString());
    }

    @Test
    void testFindByQrOrId_withQrCode() {
        AssetRepository repo = mock(AssetRepository.class);
        AssetService service = new AssetService(repo);

        String qrCode = "QR123";
        Asset asset = new Asset();
        when(repo.findByQrCode(qrCode)).thenReturn(Optional.of(asset));

        Optional<Asset> result = service.findByQrOrId(qrCode);
        assertTrue(result.isPresent());
        verify(repo, never()).findById(any());
        verify(repo).findByQrCode(qrCode);
    }
}
