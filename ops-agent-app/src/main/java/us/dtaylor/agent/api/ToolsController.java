package us.dtaylor.agent.api;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/agent/tools")
public class ToolsController {

    @Autowired
    private List<McpSyncClient> mcpSyncClients;  // For sync client

    @Autowired
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    /**
     * Creates a new {@code ToolsController} with the required collaborators.
     *
     * @param mcpSyncClients one or more MCP clients used to discover tools
     */
    public ToolsController(List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients;
        // Build a default provider using all available clients.
        this.toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
    }

    /**
     * Lists all available tool callbacks.
     *
     * @return a list of tool callback names
     */
    @GetMapping
    public List<String> list() {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(toolCallback -> toolCallback.getToolDefinition().name())
                .toList();
    }
}
