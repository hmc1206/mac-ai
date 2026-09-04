package com.example.mac_backend.service;

import com.example.mac_backend.collector.GoogleNewsRssCollector;
import com.example.mac_backend.collector.NaverNewsCollector;
import com.example.mac_backend.model.NewsArticle;
import com.example.mac_backend.model.ProcessedNews;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {

    private final NaverNewsCollector naverCollector;
    private final GoogleNewsRssCollector googleCollector;
    private final NewsAiService newsAiService;

    public NewsService(NaverNewsCollector naverCollector,
                       GoogleNewsRssCollector googleCollector,
                       NewsAiService newsAiService) {
        this.naverCollector = naverCollector;
        this.googleCollector = googleCollector;
        this.newsAiService = newsAiService;
    }

    public List<ProcessedNews> fetchAndProcessEconomyNews() {
        List<NewsArticle> rawArticles = new ArrayList<>();

        // 1. 네이버에서 한국 정부 경제 정책 뉴스 수집
        rawArticles.addAll(naverCollector.collectNews("정부 경제 정책"));

        // 2. 구글 RSS에서 글로벌 금리/정책 뉴스 수집
        rawArticles.addAll(googleCollector.collectNews("FED+policy"));

        // 3. 중복 제거 (URL 기준) 및 AI 분석 처리
        return rawArticles.stream()
                .filter(distinctByKey(NewsArticle::url))
                .limit(5) // AI API 비용 및 속도를 고려해 상위 5개만 정제
                .map(newsAiService::analyzeNews)
                .toList();
    }

    private <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        java.util.Set<Object> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}