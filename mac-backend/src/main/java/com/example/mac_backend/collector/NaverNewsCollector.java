package com.example.mac_backend.collector;

import com.example.mac_backend.model.NewsArticle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class NaverNewsCollector implements NewsCollector {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public NaverNewsCollector(
            @Value("${naver.api.client-id:}") String clientId,
            @Value("${naver.api.client-secret:}") String clientSecret,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl("https://naverapihub.apigw.ntruss.com")
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId.trim())
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret.trim())
                .build();
    }

    @Override
    public List<NewsArticle> collectNews(String query) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/v1/news")
                        .queryParam("query", query)
                        .queryParam("display", 10)
                        .queryParam("sort", "date")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        List<NewsArticle> articles = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode items = root.get("items");

            if (items != null && items.isArray()) {

                for (JsonNode item : items) {

                    String title = cleanHtml(
                            item.get("title").asText()
                    );

                    String description = cleanHtml(
                            item.get("description").asText()
                    );

                    String url = item.get("originallink").asText();

                    String pubDate = item.get("pubDate").asText();

                    articles.add(new NewsArticle(
                            title,
                            description,
                            url,
                            "Naver News",
                            parseNaverDate(pubDate)
                    ));
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "NAVER 뉴스 응답 JSON 파싱 실패",
                    e
            );
        }

        return articles;
    }

    private String cleanHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceAll("<[^>]*>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private LocalDateTime parseNaverDate(String pubDate) {

        try {
            return ZonedDateTime
                    .parse(
                            pubDate,
                            DateTimeFormatter.RFC_1123_DATE_TIME
                    )
                    .toLocalDateTime();

        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}