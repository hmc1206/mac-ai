package com.example.mac_backend.model;

import java.time.LocalDateTime;

public record NewsArticle(
        String title,               //뉴스 제목
        String description,         //뉴스 요약/본문 일부
        String url,                 //원문 뉴스 URL
        String source,              //뉴스 출처
        LocalDateTime publishedAt   //뉴스 발행 시간
) {
}