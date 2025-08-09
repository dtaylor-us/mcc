package us.dtaylor.mcpserver.service.storage;

import java.nio.file.Path;

public interface QrStorage {
    /** Persist a just-generated PNG and return a public URL. */
    String storeAndGetPublicUrl(Path localFile, String fileName) throws Exception;
}
