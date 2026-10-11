package org.xiaoxu.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.text.CharacterIterator;
import java.util.Map;

@RestController
@RequestMapping("/template")
public class PromptTemplateController implements InitializingBean {


    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;


    @GetMapping("/network")
    public String callNetwork(String topic){

        String template = """
                请给我推荐几个关于{topic}的开源项目
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        promptTemplate.add("topic", topic);

        return chatClient.prompt(promptTemplate.create()).call().content();
    }


    @GetMapping("network2")
    public Flux<String> callNetwork2(@RequestParam("topic") String topic, HttpServletResponse response){
        String template = """
                请给我推荐几个关于{topic}的开源项目
                """;
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(new PromptTemplate(template).create(Map.of("topic",topic))).stream().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {


        chatClient = ChatClient.builder(chatModel)
                .defaultOptions( DashScopeChatOptions.builder()
                        .temperature(0.7)
                        .build())
                .build();
    }
}
