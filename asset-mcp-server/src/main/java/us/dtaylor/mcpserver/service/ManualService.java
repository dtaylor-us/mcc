package us.dtaylor.mcpserver.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ManualService {
    public String readManual(String uriLike) {
        // Demo: only "file:" supported here; swap to S3 SDK in production.
        if (!uriLike.startsWith("file:")) throw new IllegalArgumentException("Only file: supported in demo");
        try { return Files.readString(Path.of(uriLike.substring("file:".length()))); }
        catch (IOException e) { throw new RuntimeException("manual not found: " + uriLike, e); }
    }
}
