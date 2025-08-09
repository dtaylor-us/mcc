package us.dtaylor.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                // register memory as a default advisor so you don't have to add it on every call
                .defaultAdvisors(spec -> spec.advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                ))
                .build();
    }
}
