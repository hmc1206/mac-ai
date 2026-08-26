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
    //Josn 문자열을 JAVA에서 읽고 처리
    private final ObjectMapper objectMapper;

    //생성자 생성
    public MacController(ChatClient.Builder chatClientBuilder, NotionTools notionTools, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.notionTools = notionTools;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/chat")
    public String chatWithMac(@RequestParam String message) {

        String response = chatClient.prompt()
                .user(message)
                .tools(notionTools.getAddEventTool())
                .call()
                .content();

        System.out.println("=== AI 응답 ===");
        System.out.println(response);

        try {
            JsonNode root = objectMapper.readTree(response);
            // AI가 addNotionEvent 호출을 요청한 경우
            if ("addNotionEvent".equals(root.path("name").asText())) {
                // arguments만 추출
                JsonNode arguments = root.path("arguments");
                //System.out.println("=== Tool Arguments ===");
                //System.out.println(arguments);

                // Tool이 기대하는 형식으로 전달
                return notionTools.getAddEventTool().call(arguments.toString());
            }

        } catch (Exception e) {
            // 일반적인 대화 응답은 JSON이 아닐 수 있음
            System.out.println("Tool JSON 파싱 실패 - 일반 AI 응답으로 처리");
        }
        return response;
    }
}