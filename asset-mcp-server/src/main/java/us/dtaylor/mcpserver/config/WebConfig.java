package us.dtaylor.mcpserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /qr-images/** to the local folder
        String location = "file:" + System.getProperty("user.home") + "/projects/maintenance-control-console/qr/";
        registry.addResourceHandler("/qr-images/**")
                .addResourceLocations(location);
    }
}
