package us.dtaylor.mcpserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures static resource handling so that generated QR images can be
 * served via HTTP from the local file system.  The base directory and
 * the corresponding URL prefix are derived from application properties.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String qrDir;

    public WebConfig(@Value("${app.qr.storage.local.dir}") String qrDir) {
        this.qrDir = qrDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /qr-images/** to the configured directory.  Always prefix
        // with file: for proper resolution and ensure a trailing slash.
        String location = qrDir;
        if (!location.startsWith("file:")) {
            location = "file:" + location;
        }
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/qr-images/**")
                .addResourceLocations(location);
    }
}
