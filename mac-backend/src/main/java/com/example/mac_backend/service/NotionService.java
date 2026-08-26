package com.example.mac_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotionService {
    //notion API와 HTTP 통신할 클라이언트
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${notion.database-id:}")
    private String databaseId;

    //WebClient를 생성
    public NotionService(WebClient.Builder webClientBuilder,
                         ObjectMapper objectMapper,
                         @Value("${notion.api-key:}") String apiKey,
                         @Value("${notion.version:2022-06-28}") String notionVersion) {
        System.out.println("=== Notion 환경변수 확인 ===");
        System.out.println("API Key 존재 여부: " + !apiKey.isBlank());

        this.objectMapper = objectMapper;

        this.webClient = webClientBuilder //WebClient 설정
                .baseUrl("https://api.notion.com/v1") //도든 API와 통신하기 위한 WebClient 설정
                .defaultHeader("Authorization", "Bearer " + apiKey) //헤더 설정
                .defaultHeader("Notion-Version", notionVersion) //버전 설정
                .defaultHeader("Content-Type", "application/json") //요청 데이터가 JSON 형식임을 지정
                .build(); //객체 생성
    }

    //Notion 데이터베이스에 새로운 일정 추가 함수
    public String addCalendarEvent(String title, String startDateIso) {
        String safeTitle = (title != null && !title.isBlank()) ? title : "제목 없음";
        String safeDate = (startDateIso != null && !startDateIso.isBlank()) ? startDateIso : "2026-08-26T00:00:00Z";

        //notion API에 전달할 전체 JSON 요청 데이터 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("parent", Map.of("database_id", databaseId));

        //Notion 데이터베이스의 속성을 저장할 Map
        Map<String, Object> properties = new HashMap<>();

        // 1. 카드 제목 속성 ('이름' 또는 'Name')
        properties.put("이름", Map.of("title", List.of(Map.of("text", Map.of("content", safeTitle)))));

        // 2. 날짜 속성 ('날짜' 대신 노션 DB의 실제 컬럼명인 '마감일' 또는 'Date' 적용)
        // ※ 만약 노션 DB 컬럼명이 'Date'라면 "Date"로 수정해 주세요.
        properties.put("마감일", Map.of("date", Map.of("start", safeDate)));

        //생성한 일정 정보를 전체 요청 Body에 추가
        requestBody.put("properties", properties);

        try {
            String response = webClient.post()
                    .uri("/pages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("=== Notion API 성공 응답 ===");
            System.out.println(response);

            return "노션 캘린더에 '" + safeTitle + "' (일시: " + safeDate + ") 일정을 성공적으로 등록했습니다.";
        } catch (WebClientResponseException e) {
            System.err.println("Notion API Error Body: " + e.getResponseBodyAsString());
            return "노션 일정 등록 실패 (400 Bad Request): " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "노션 일정 등록 실패: " + e.getMessage();
        }
    }

    //일정 제목을 기준으로 Notion 데이터베이스에서 해당 일정의 Page ID을 검색
    //@return 찾은 일정의 Page Id, 없으면 null
    public String findEventByTitle(String title){
        //Notion Database Query의 검색 조건을 생성
        Map<String, Object> filter = Map.of(
                "property", "이름",
                "title", Map.of(
                        "equals", title
                )
        );

        //Notion API에 전달할 요청 Body를 생성
        Map<String,Object> requestBody = Map.of(
                "filter", filter
        );

        try {
            //Notion Database Query API 호충
            //POST /v1/databases/{databaseId}/query
            String response = webClient.post()
                    .uri("/databases/{databaseId}/query", databaseId)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("=== Notion 일정 검색 결과 ===");
            System.out.println(response);

            //Notion API의 JSON 응답을 파싱합니다.
            JsonNode root = objectMapper.readTree(response);

            //검색 결과 배열을 가져옴
            JsonNode results = root.path("results");

            if(results.isEmpty()){
                return null;
            }

            return results.get(0)
                    .path("id")
                    .asText();
        } catch (Exception e) {
            System.err.println("Notion 일정 검색 실패 : " + e.getMessage());
            return null;
        }
    }
}