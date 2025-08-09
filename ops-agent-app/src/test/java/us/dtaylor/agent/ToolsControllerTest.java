package us.dtaylor.agent;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import us.dtaylor.agent.api.ToolsController;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link ToolsController}.  These tests verify that the
 * controller correctly lists the names of the tools reported by the
 * {@link SyncMcpToolCallbackProvider}.  They use a simple stub provider
 * configured with a few fake {@link ToolCallback} instances to avoid
 * connecting to a real MCP server.  Because the controller has no other
 * dependencies, there is no need to start the full Spring application
 * context; the controller is instantiated directly.
 */
class ToolsControllerTest {

    /**
     * A simple stub {@link ToolCallback} used for testing.  Only the name
     * matters for the controller under test, so the remaining methods are
     * implemented as no‑ops or return null as appropriate.
     */
    private static class StubToolCallback implements ToolCallback {
        private final String name;

        StubToolCallback(String name) {
            this.name = name;
        }

        public ToolDefinition getToolDefinition() {
            return new ToolDefinition() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String description() {
                    return "";
                }

                @Override
                public String inputSchema() {
                    return "";
                }
                // Implement other methods as needed or leave as default
            };
        }

        @Override
        public ToolMetadata getToolMetadata() {
            return ToolCallback.super.getToolMetadata();
        }

        @Override
        public String call(String toolInput) {
            return "";
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            return ToolCallback.super.call(toolInput, toolContext);
        }
    }

    @Test
    void listTools_ReturnsNamesFromProvider() throws Exception {
        // Arrange: set up a stub provider that returns a few tool callbacks.
        ToolCallback[] callbacks = {
                new StubToolCallback("asset.search"),
                new StubToolCallback("manual.get"),
                new StubToolCallback("worklog.create")
        };
        List<McpSyncClient> clients = List.of(); // empty, not used in test
        ToolsController controller = new ToolsController(clients);

        // Use reflection to inject the stub provider
        var field = ToolsController.class.getDeclaredField("toolCallbackProvider");
        field.setAccessible(true);
        field.set(controller, new SyncMcpToolCallbackProvider(clients) {
            @Override
            public ToolCallback[] getToolCallbacks() {
                return callbacks;
            }
        });
        // Act
        List<String> names = controller.list();

        // Assert
        assertThat(names).containsExactly("asset.search", "manual.get", "worklog.create");
    }

    @Test
    void listTools_EmptyProviderReturnsEmptyList() throws Exception {
        List<McpSyncClient> clients = List.of();
        ToolsController controller = new ToolsController(clients);

        var field = ToolsController.class.getDeclaredField("toolCallbackProvider");
        field.setAccessible(true);
        field.set(controller,  new SyncMcpToolCallbackProvider(clients) {
            @Override
            public ToolCallback[] getToolCallbacks() {
                return new ToolCallback[0]; // no tools
            }
        });

        List<String> names = controller.list();

        assertThat(names).isEmpty();
    }
}
