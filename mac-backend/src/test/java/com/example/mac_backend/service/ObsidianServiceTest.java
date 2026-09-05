package com.example.mac_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ObsidianServiceTest {

    @Autowired
    private ObsidianService obsidianService;

    @Test
    void createTodayDailyNote() {

        String date = "2026-09-05";

        String content = """
                ---
                type: daily
                date: 2026-09-05
                tags:
                  - daily
                ---

                # 2026-09-05

                ## 📰 오늘의 뉴스

                ## 📝 오늘의 메모

                ## 🎯 오늘 할 일

                """;

        obsidianService.createDailyNote(date, content);
    }

    @Test
    public void appendToTodayDailyNote() {
        String date = "2026-09-05";

        String content = """
        ### ### 개발 뉴스
       
        - 새로운 개발 기술이 공개됐다.
        """;

        obsidianService.appendToDailyNote(date, content);
    }
}