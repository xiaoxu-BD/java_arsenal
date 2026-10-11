package org.xiaoxu.controller;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/prompt/engineer")
public class PromptEngineerController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;


    private ChatClient client;

    //主要有 修饰词填入 给用户更好的返回结果

    //shot 案例，让模型输出更加明确

    // 规定好特殊格式

    @GetMapping("call")
    public Flux<String> callStream(String message){

        return  client.prompt("请你以json格式输出内容").user(message).stream().content();

    }



    @Override
    public void afterPropertiesSet() throws Exception {



        client = ChatClient.builder(chatModel)
                .defaultOptions(   DashScopeChatOptions.builder()
                        .temperature(0.7)
                        .build())
                .build();
    }
}

