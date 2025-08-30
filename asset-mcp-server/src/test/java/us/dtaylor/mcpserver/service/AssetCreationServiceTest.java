package us.dtaylor.mcpserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import us.dtaylor.mcpserver.domain.Asset;
import us.dtaylor.mcpserver.repository.AssetRepository;
import us.dtaylor.mcpserver.service.storage.QrStorage;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssetCreationServiceTest {
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private QrStorage qrStorage;

    private AssetCreationService service;

    @BeforeEach
    void setUp() {
        // Provide a dummy scanBaseUrl
        service = new AssetCreationService(assetRepository, qrCodeService, qrStorage, "http://scan.example.com");
    }

    @Test
    void createWithQrGeneratesAndStoresImage() throws Exception {
        Asset asset = new Asset();
        asset.setName("New Asset");
        asset.setQrCode("QR-CUSTOM");
        asset.setManualPath("file:/manuals/new.txt");

        // Mock repository to return the same asset with an ID when saved
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> {
            Asset arg = invocation.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(java.util.UUID.randomUUID());
            }
            return arg;
        });

        doAnswer(invocation -> {
            String data = invocation.getArgument(0);
            Path path = invocation.getArgument(1);
            java.nio.file.Files.writeString(path, "dummy");
            return null;
        }).when(qrCodeService).generatePng(anyString(), any(Path.class));

        when(qrStorage.storeAndGetPublicUrl(any(Path.class), anyString())).thenReturn("http://cdn.example.com/qr/QR-CUSTOM.png");

        Asset result = service.createWithQr(asset);
        assertNotNull(result.getId());
        assertEquals("QR-CUSTOM", result.getQrCode());
        // Use getQrImagePath() instead of getQrImageUrl()
        assertEquals("http://cdn.example.com/qr/QR-CUSTOM.png", result.getQrImagePath());
        verify(assetRepository, times(2)).save(any(Asset.class));
        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(qrCodeService).generatePng(dataCaptor.capture(), any(Path.class));
        assertTrue(dataCaptor.getValue().contains("QR-CUSTOM"), "Scan URL should contain QR code");
        verify(qrStorage).storeAndGetPublicUrl(any(Path.class), eq("QR-CUSTOM.png"));
    }
}
