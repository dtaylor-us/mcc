package us.dtaylor.mcpserver.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class LocalQrStorage implements QrStorage {

    private final Path dir;
    private final String publicBaseUrl;

    public LocalQrStorage(
            @Value("${app.qr.storage.local.dir}") String dir,
            @Value("${app.qr.storage.local.publicBaseUrl}") String publicBaseUrl) {
        this.dir = Path.of(dir);
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length()-1) : publicBaseUrl;
    }

    @Override
    public String storeAndGetPublicUrl(Path localFile, String fileName) throws Exception {
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);
        // Move/overwrite
        Files.deleteIfExists(target);
        Files.move(localFile, target);
        return publicBaseUrl + "/" + fileName;
    }
}
