package com.example.mac_backend.service;

import com.example.mac_backend.model.NewsArticle;
import com.example.mac_backend.model.ProcessedNews;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class NewsAiService {

    private final ChatClient chatClient;

    public NewsAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ProcessedNews analyzeNews(NewsArticle article) {
        String promptText = """
                다음 뉴스를 분석하여 주요 핵심을 정제해주세요.
                
                제목: %s
                내용: %s
                
                [요청사항]
                1. 내용을 2~3문장으로 핵심만 요약하세요.
                2. 카테고리를 [국내경제], [해외경제], [정부정책], [금융/금리] 중 하나로 분류하세요.
                3. 이 뉴스가 경제나 시장에 미칠 영향을 한 문장으로 정리하세요.
                """.formatted(article.title(), article.description());

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(ProcessedNews.class); // 객체로 자동 매핑
    }
}