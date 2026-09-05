package com.example.mac_backend.Tools;

import com.example.mac_backend.model.ProcessedNews;
import com.example.mac_backend.tool.NewsTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class NewsToolsTest {

    @Autowired
    private NewsTools newsTools;

    @Test
    void getTodayEconomyNews() {

        List<ProcessedNews> result =
                newsTools.getTodayEconomyNews();

        result.forEach(news -> {
            System.out.println("================================");
            System.out.println("제목: " + news.originalTitle());
            System.out.println("요약: " + news.summary());
            System.out.println("분류: " + news.category());
            System.out.println("영향: " + news.impact());
            System.out.println("URL: " + news.originalUrl());
        });
    }
}