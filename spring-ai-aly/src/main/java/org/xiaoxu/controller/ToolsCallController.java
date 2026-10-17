package org.xiaoxu.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.tools.TimeTools;

@RestController
@RequestMapping("/tools")
public class ToolsCallController {


    @Autowired
    private ChatClient chatClient1;

    @GetMapping("/chat")
    public String chat(@RequestParam("query") String query) {

//        return chatClient1.prompt().toolNames("getTimeFunction").user(query).call().content();
        return chatClient1.prompt().tools(new TimeTools()).user(query).call().content();
    }


}
