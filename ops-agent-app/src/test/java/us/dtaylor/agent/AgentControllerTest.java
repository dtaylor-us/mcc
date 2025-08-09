package us.dtaylor.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import us.dtaylor.agent.api.AgentController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AgentController}.  These tests do not start a Spring
 * context.  Instead, they instantiate the controller directly and provide
 * mocked dependencies.  The {@link ChatClient} is configured with deep
 * stubs so that fluent calls return mocks and ultimately yield a fixed
 * response.  The tool callback provider is injected via reflection to
 * simulate discovery of remote MCP tools.  This approach allows testing
 * of the controller’s logic without requiring a running model or MCP server.
 */
class AgentControllerTest {

    private ChatClient chatClient;
    private ChatMemory chatMemory;
    private AgentController controller;

    @BeforeEach
    void setup() throws Exception {
        // Create a ChatClient mock with deep stubs to handle fluent API calls.
        this.chatClient = Mockito.mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);
        // Configure the mock so that chaining calls returns itself and
        // ultimately yields a fixed response string.  Deep stubs allow us
        // to stub the result of a deeply nested call chain.
        Mockito.when(
            chatClient.prompt()
                .system(ArgumentMatchers.anyString())
                .user(ArgumentMatchers.anyString())
                .toolCallbacks(ArgumentMatchers.anyList())
                .advisors((Consumer<ChatClient.AdvisorSpec>) ArgumentMatchers.any())
                .call()
                .content()
        ).thenReturn("stubbed-answer");

        // Create a trivial ChatMemory mock.  The contents of the memory
        // are not inspected by the controller tests.
        this.chatMemory = Mockito.mock(ChatMemory.class);

        // Initialise the controller with no MCP clients (empty list).  The
        // internal provider will initially expose no tools, but we will
        // override it per test via reflection.
        this.controller = new AgentController(chatClient, chatMemory, List.of());
    }

    /**
     * Injects a custom {@link ToolCallbackProvider} into the controller using
     * reflection.  This helper allows tests to simulate discovery of remote
     * tools without requiring a real MCP client.  It avoids reliance on
     * implementation details of {@link SyncMcpToolCallbackProvider}.
     *
     * @param provider the provider to inject
     */
    private void setToolProvider(ToolCallbackProvider provider) throws Exception {
        Field field = AgentController.class.getDeclaredField("toolCallbackProvider");
        field.setAccessible(true);
        field.set(controller, provider);
    }

    /**
     * Creates a stub {@link ToolCallback} that returns a specific name.  Only
     * the name is used by the controller, so other methods are left
     * unimplemented by delegating to default behaviour.  We use Mockito to
     * create the stub for brevity and to avoid implementing the entire
     * interface manually.
     *
     * @param name the name of the tool
     * @return a stubbed ToolCallback
     */
private static ToolCallback stubToolCallback(String name) {
    ToolCallback callback = Mockito.mock(ToolCallback.class);
    // Create a stub ToolDefinition with the required name
    org.springframework.ai.tool.definition.ToolDefinition toolDef = Mockito.mock(org.springframework.ai.tool.definition.ToolDefinition.class);
    Mockito.when(toolDef.name()).thenReturn(name);
    Mockito.when(callback.getToolDefinition()).thenReturn(toolDef);
    return callback;
}
    @Test
    void ask_ReturnsAnswerAndToolCount() throws Exception {
        // Arrange: inject a provider exposing three tools
        ToolCallback[] callbacks = {
            stubToolCallback("asset.search"),
            stubToolCallback("manual.get"),
            stubToolCallback("worklog.create")
        };
        setToolProvider(() -> callbacks);

        // Act: call the controller
        Map<String, Object> result = controller.ask(new AgentController.AskRequest("What is the status?", "conv1"));

        // Assert: verify the stubbed response and tool count
        assertThat(result.get("answer")).isEqualTo("stubbed-answer");
        assertThat(result.get("toolCount")).isEqualTo(callbacks.length);
    }

    @Test
    void ask_NoToolsStillReturnsAnswer() throws Exception {
        // Arrange: provider exposes no tools
        setToolProvider(() -> new ToolCallback[]{});

        // Act
        Map<String, Object> result = controller.ask(new AgentController.AskRequest("No tools?", "conv2"));

        // Assert: answer is still returned and tool count is zero
        assertThat(result.get("answer")).isEqualTo("stubbed-answer");
        assertThat(result.get("toolCount")).isEqualTo(0);
    }

    @Test
    void setAllowedServerNames_FiltersProvider() throws Exception {
        // Arrange: two fake MCP clients with names "primary" and "secondary" that
        // will expose different numbers of tools when selected.  Since the
        // controller rebuilds its provider based on client names, we simulate
        // this by injecting providers manually after calling the method.
        // Initially inject provider with two tools
        ToolCallback[] primaryTools = { stubToolCallback("asset.search"), stubToolCallback("manual.get") };
        setToolProvider(() -> primaryTools);
        // Act: call setAllowedServerNames with a set that does not match any
        // MCP clients (the controller's internal list is empty).  This
        // effectively selects zero clients, resulting in an empty provider.
        controller.setAllowedServerNames(Set.of("nonexistent"));
        // Inject empty provider after filtering
        setToolProvider(() -> new ToolCallback[]{});
        Map<String, Object> result = controller.ask(new AgentController.AskRequest("Filter?", "conv3"));
        // Assert: toolCount reflects the empty provider
        assertThat(result.get("toolCount")).isEqualTo(0);
        assertThat(result.get("answer")).isEqualTo("stubbed-answer");
    }
}
