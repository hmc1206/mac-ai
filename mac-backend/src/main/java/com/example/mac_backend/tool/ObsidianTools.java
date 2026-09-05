package com.example.mac_backend.tool;

import com.example.mac_backend.service.ObsidianService;
import org.springframework.stereotype.Component;

@Component
public class ObsidianTools {

    private final ObsidianService obsidianService;

    public ObsidianTools(ObsidianService obsidianService) {
        this.obsidianService = obsidianService;
    }

    public String createDailyNote(String content) {

        obsidianService.createDailyNote(
                "2026-09-05",
                content
        );

        return "Obsidian Daily Note가 생성되었습니다.";
    }
}