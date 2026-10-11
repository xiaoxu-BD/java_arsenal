package org.xiaoxu.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.entity.Book;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/struct")
public class StructOutPutController {



    @Resource
    private ChatClient chatClientBySelf;


//  beanOutputConvert不支持Flux Stirng

    @RequestMapping("stream")
    public String structStream(HttpServletResponse response){

        response.setCharacterEncoding("UTF-8");


        PromptTemplate promptTemplate = new PromptTemplate(" 请帮我推荐1本java相关的书  {format} ");

        Prompt prompt = promptTemplate.create(Map.of("format", new BeanOutputConverter<>(Book.class).getFormat()));

        return chatClientBySelf.prompt(prompt).system("你是一个专业的图书推荐人员").call().content();
    }


    @RequestMapping("/note")
    public String message(){

        Book book = chatClientBySelf.prompt("给我推荐1本java的书籍").call().entity(Book.class);
        return book.toString();
    }

    @RequestMapping("/test")
    public String test(@RequestParam String test){
        return  "test" + " " + test;
    }




}
