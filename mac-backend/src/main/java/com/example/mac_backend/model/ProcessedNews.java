package com.example.mac_backend.model;

public record ProcessedNews(
        String originalTitle,
        String summary,         // AI가 요약한 2-3문장
        String category,        // [경제], [정부정책], [금융] 등
        String impact,          // 시장/정책에 미치는 영향 분석
        String originalUrl
) {}