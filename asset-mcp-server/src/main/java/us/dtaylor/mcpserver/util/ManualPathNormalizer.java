package us.dtaylor.mcpserver.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public final class ManualPathNormalizer {

    private static final Pattern WINDOWS_DRIVE = Pattern.compile("^[a-zA-Z]:\\\\.*");

    private ManualPathNormalizer() {}

    /** Accepts raw input and returns a normalized URI string:
     * - http(s)://... → unchanged
     * - file://...    → unchanged
     * - /abs/path     → file:///abs/path
     * - C:\path       → file:///C:/path (Windows)
     * - empty/null    → null
     */
    public static String normalize(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        String lower = s.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file://")) {
            return s;
        }

        // Windows: C:\folder\file.txt → file:///C:/folder/file.txt
        if (WINDOWS_DRIVE.matcher(s).matches()) {
            String forward = s.replace('\\', '/');
            // ensure file:///C:/...
            if (!forward.startsWith("/")) forward = "/" + forward;
            return "file://" + forward;
        }

        // Absolute *nix path: /mnt/...
        if (s.startsWith("/")) {
            return "file://" + s; // results in file:///mnt/...
        }

        // Fallback: treat as relative file path (resolve to absolute)
        Path p = Paths.get(s).toAbsolutePath().normalize();
        return "file://" + p.toString().replace('\\', '/');
    }
}
