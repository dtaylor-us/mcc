package us.dtaylor.mcpserver.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ManualPathNormalizer}. Ensures that various forms of
 * user input are converted into canonical URI strings.
 */
public class ManualPathNormalizerTest {

    @Test
    void testNormalizeNullOrBlank() {
        assertNull(ManualPathNormalizer.normalize(null));
        assertNull(ManualPathNormalizer.normalize(" ")); 
    }

    @Test
    void testNormalizeHttpAndHttpsUnchanged() {
        String http = "http://example.com/manual.txt";
        String https = "https://example.com/another.pdf";
        assertEquals(http, ManualPathNormalizer.normalize(http));
        assertEquals(https, ManualPathNormalizer.normalize(https));
    }

    @Test
    void testNormalizeFileUriUnchanged() {
        String file = "file:///path/to/manual.pdf";
        assertEquals(file, ManualPathNormalizer.normalize(file));
    }

    @Test
    void testNormalizeWindowsPath() {
        String input = "C:\\manuals\\file.txt";
        String expected = "file:///C:/manuals/file.txt";
        assertEquals(expected, ManualPathNormalizer.normalize(input));
    }

    @Test
    void testNormalizeUnixAbsolutePath() {
        String input = "/var/docs/manual.md";
        String expected = "file:///var/docs/manual.md";
        assertEquals(expected, ManualPathNormalizer.normalize(input));
    }

    @Test
    void testNormalizeRelativePath() {
        // Use a simple relative name; the exact absolute path depends on the
        // working directory so we simply assert the prefix
        String input = "manual.txt";
        String result = ManualPathNormalizer.normalize(input);
        assertTrue(result.startsWith("file://"));
        assertTrue(result.endsWith("manual.txt"));
    }
}