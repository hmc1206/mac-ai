package com.example.mac_backend.tool;

import com.example.mac_backend.model.ProcessedNews;
import com.example.mac_backend.service.NewsService;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsTools {

    private final NewsService newsService;

    public NewsTools(NewsService newsService) {
        this.newsService = newsService;
    }

    public List<ProcessedNews> getTodayEconomyNews() {
        return newsService.fetchAndProcessEconomyNews();
    }

    public FunctionToolCallback<Void, List<ProcessedNews>>
    getTodayEconomyNewsTool() {

        return FunctionToolCallback
                .builder(
                        "get_today_economy_news",
                        (Void request) -> getTodayEconomyNews()
                )
                .description(
                        "한국 정부 경제 정책 및 글로벌 금리/정책 관련 최신 경제 뉴스를 " +
                                "수집하고 AI로 요약 및 영향을 분석하여 반환합니다."
                )
                .inputType(Void.class)
                .build();
    }
}