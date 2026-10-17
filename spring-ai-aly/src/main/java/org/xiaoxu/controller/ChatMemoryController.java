package org.xiaoxu.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/memory")
public class ChatMemoryController {


    @Autowired
    private ChatModel chatModel;


    @Autowired
    private ChatClient chatClientBySelf;



    //会超时
    @RequestMapping("/call")
    public String memory(){

        List<Message> messages = new ArrayList<>();
        //第一轮对话
        messages.add(new SystemMessage("你是一个Java架构师"));
        messages.add(new UserMessage("SpringMVC的执行流程?"));

        messages.add(new AssistantMessage("你记得了用户说了url经过DispatcherServlet中的doDispatch()方法"));

        messages.add(new UserMessage("其中的适配器是什么模式为什么要这样做?"));


        messages.add(new AssistantMessage("你准备告诉他这是适配器模式，并用的是HandlerAdapter处理的"));

        messages.add(new UserMessage("用户问它是怎么具体怎么处理的"));

        Prompt prompt = new Prompt(messages);

        return  chatModel.call(prompt).getResult().getOutput().getText();
    }




    @GetMapping(value = "/stream")
    public Flux<String> memory2(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个Java架构师"));
        messages.add(new UserMessage("SpringMVC的执行流程?"));
        messages.add(new AssistantMessage("你记得了用户说了url经过DispatcherServlet中的doDispatch()方法"));   // 注意上次说的：这里应是模型说过的话
        messages.add(new UserMessage("其中的适配器是什么模式为什么要这样做?"));
        messages.add(new AssistantMessage("你准备告诉他这是适配器模式，并用的是HandlerAdapter处理的"));

        messages.add(new UserMessage("它是怎么具体怎么处理的"));

        Prompt prompt = new Prompt(messages);

        // ChatModel.stream 返回 Flux<ChatResponse>，逐块取出文本增量
        return chatModel.stream(prompt)
                .map(resp -> resp.getResult().getOutput().getText())
                .filter(java.util.Objects::nonNull);   // 防御：个别块的文本可能为 null
    }





    //记忆
    @RequestMapping("/client")
    public Flux<String> client(String message,String chatId, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        return chatClientBySelf.prompt().user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream().content();

    }



}
