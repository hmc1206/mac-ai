package com.example.mac_backend.controller;

import com.example.mac_backend.tool.NotionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/mac")
public class MacController {

    //spring AI
    private final ChatClient chatClient;
    //notion 관련 AI Tool 관리 함수
    private final NotionTools notionTools;

    //생성자 생성
    public MacController(ChatClient.Builder chatClientBuilder, NotionTools notionTools) {
        this.chatClient = chatClientBuilder.build();
        this.notionTools = notionTools;
    }

    @GetMapping("/chat")
    public String chatWithMac(@RequestParam String message) {

        String response = chatClient.prompt()
                .user(message)
                .tools(
                        notionTools.getAddEventTool(),
                        notionTools.getFindEventTool(),
                        notionTools.getUpdateEventStatusTool(),
                        notionTools.getArchiveEventTool()
                )
                .call()
                .content();

        System.out.println("=== AI 응답 ===");
        System.out.println(response);

        return response;
    }
}