package com.example.mac_backend.model;

public record ProcessedNews(
        String originalTitle,
        String content,         // 뉴스 내용
        String category,        // [경제], [정부정책], [금융] 등
        String impact,          // 시장/정책에 미치는 영향 분석
        String originalUrl
) {}