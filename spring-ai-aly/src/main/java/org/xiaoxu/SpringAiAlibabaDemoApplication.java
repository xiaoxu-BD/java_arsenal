package org.xiaoxu;

/**
 * Hello world!
 *
 */
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAiAlibabaDemoApplication {

    // 1. 创建 ChatClient Bean
    // Spring AI Alibaba 会自动配置它，我们直接注入即可
    private final ChatClient chatClient;

    public SpringAiAlibabaDemoApplication(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAlibabaDemoApplication.class, args);
    }

    // 2. 使用 CommandLineRunner 在应用启动后执行代码
    @Bean
    public CommandLineRunner runner() {
        return args -> {
            System.out.println("准备向通义千问提问...");

            // 3. 发送请求并获取响应
            String userMessage = "请用一句话介绍一下Spring框架。";
            String response = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            System.out.println("通义千问的回答：");
            System.out.println(response);
        };
    }
}