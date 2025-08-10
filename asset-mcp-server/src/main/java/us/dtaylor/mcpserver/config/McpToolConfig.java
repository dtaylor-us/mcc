package us.dtaylor.mcpserver.config;

import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import us.dtaylor.mcpserver.tools.AssetTools;

/**
 * Configuration class responsible for exposing {@link AssetTools} as
 * Model Context Protocol (MCP) tools.  The Spring AI MCP server
 * starter will automatically discover beans of type {@link ToolCallbackProvider}
 * and publish their tool methods over the MCP transport.
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider assetToolCallbackProvider(AssetTools assetTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(assetTools)
                .build();
    }
}
