package us.dtaylor.agent.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.modelcontextprotocol.client.McpSyncClient;

/**
 * Agent controller that integrates with an MCP server to enable tool calling.
 *
 * <p>This implementation uses Spring AI's {@link SyncMcpToolCallbackProvider}
 * to discover all available tools from one or more {@link McpSyncClient}s and
 * exposes them to the underlying chat model. It demonstrates how to build
 * providers programmatically and how to filter tools based on server names
 * when working with multiple MCP servers. See Spring AI’s MCP utilities
 * documentation for more details:contentReference[oaicite:0]{index=0}.</p>
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final List<McpSyncClient> mcpSyncClients;
    private volatile ToolCallbackProvider toolCallbackProvider;

    /**
     * Creates a new {@code AgentController} with the required collaborators.
     *
     * @param chatClient     the chat client used to interact with the language model
     * @param chatMemory     persistent chat memory for conversation context
     * @param mcpSyncClients one or more MCP clients used to discover tools
     */
    @Autowired
    public AgentController(ChatClient chatClient,
                           ChatMemory chatMemory,
                           List<McpSyncClient> mcpSyncClients) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.mcpSyncClients = mcpSyncClients;
        // Build a default provider using all available clients. See
        // SyncMcpToolCallbackProvider documentation for details:contentReference[oaicite:1]{index=1}.
        this.toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
    }

    /**
     * Payload for chat requests. Contains the user’s message and an identifier
     * for the conversation so that chat memory can associate state with a
     * specific session.
     */
    public record AskRequest(String userMessage, String conversationId) {
    }

    /**
     * Handles chat requests from the client. Delegates to the language model and
     * enables tool calling against the MCP server by exposing all discovered
     * tools to the model. The system prompt instructs the model when to
     * invoke each tool. Refer to the Spring AI documentation on tool
     * callback providers for more information:contentReference[oaicite:2]{index=2}.
     */
    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody AskRequest req) {
        // 1) Get MCP tool callbacks
        ToolCallback[] callbacks = this.toolCallbackProvider.getToolCallbacks();

        String systemPrompt = """
                You are a Field Maintenance Agent. When the user mentions a QR code or asset ID:
                1) Call 'asset.search' to look up the asset.
                2) If the user asks for a manual, call 'manual.get'.
                3) If the user describes work completed, call 'worklog.create' with action, technician, duration, and notes.
                Be concise and ask clarifying questions if the QR code or asset ID is missing.
                """;

        // 2) Memory advisor (unchanged)
        MessageChatMemoryAdvisor memoryAdvisor =
                MessageChatMemoryAdvisor.builder(this.chatMemory).build();

        // 3) Use .toolCallbacks(...) instead of .tools(...)
        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(req.userMessage())
                .toolCallbacks(Arrays.asList(callbacks))  // <-- fix is here
                .advisors(spec -> spec
                        .advisors(memoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, req.conversationId()))
                .call()
                .content();

        return Map.of("answer", answer, "toolCount", callbacks.length);
    }


    /**
     * Dynamically restricts the set of MCP servers whose tools are made available
     * to the chat model. This method rebuilds the {@link ToolCallbackProvider}
     * using only the clients whose {@code serverInfo().name()} matches the
     * supplied set of allowed names. This mirrors the example from the Spring AI
     * documentation demonstrating dynamic provider selection:contentReference[oaicite:3]{index=3}.
     *
     * @param allowedServerNames the set of server names to include
     */
    public void setAllowedServerNames(Set<String> allowedServerNames) {
        List<McpSyncClient> selected = this.mcpSyncClients.stream()
                .filter(client -> allowedServerNames.contains(client.getServerInfo().name()))
                .toList();
        // Rebuild the provider with the filtered clients
        this.toolCallbackProvider = new SyncMcpToolCallbackProvider(selected);
    }
}
