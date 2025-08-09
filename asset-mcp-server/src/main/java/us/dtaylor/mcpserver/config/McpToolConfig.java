package us.dtaylor.mcpserver.config;

import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import us.dtaylor.mcpserver.tools.AssetTools;

@Configuration
public class McpToolConfig {
    @Bean
    ToolCallbackProvider mcpTools(AssetTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }
}
