package com.example.mac_backend;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MacController {

    private final ChatModel chatModel;

    public MacController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/api/mac/chat")
    public String chatWithMac(@RequestParam(defaultValue = "안녕 맥! 너에 대해 소개해줘.") String message) {
        return chatModel.call(message);
    }
}