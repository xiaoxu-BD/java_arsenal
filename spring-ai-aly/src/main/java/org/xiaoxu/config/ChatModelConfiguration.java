package org.xiaoxu.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfiguration {
    @Autowired
    private ChatModel chatModel;


    @Autowired
    private ChatMemory chatMemory;







    @Bean
    public ChatClient chatClientBySelf(){

//        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(3).build();
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build(), MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .temperature(0.7)
                                .build()
                )
                .build();
    }


    @Bean
    public ChatClient chatClient1(){

//        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(3).build();
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .temperature(0.7)
                                .build()
                )
                .build();
    }
}
