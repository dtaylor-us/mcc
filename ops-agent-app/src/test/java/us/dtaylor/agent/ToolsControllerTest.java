package us.dtaylor.agent;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import us.dtaylor.agent.api.ToolsController;
import us.dtaylor.agent.config.SecurityConfig;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ToolsController}. Verifies that the endpoint responds with
 * an empty list by default and is secured by HTTP basic authentication.
 */
@WebMvcTest(ToolsController.class)
@Import(SecurityConfig.class)
public class ToolsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private List<McpSyncClient> mcpSyncClients;

    @MockBean
    private org.springframework.ai.mcp.SyncMcpToolCallbackProvider toolCallbackProvider;

    @Test
    void testListToolsRequiresAuth() throws Exception {
        // Without credentials should result in 401
        mockMvc.perform(get("/agent/tools"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testListToolsReturnsEmptyListWithAuth() throws Exception {
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(new ToolCallback[0]);
        mockMvc.perform(get("/agent/tools").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("agent", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }
}
