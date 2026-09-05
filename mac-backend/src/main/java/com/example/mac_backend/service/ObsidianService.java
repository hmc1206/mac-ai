package com.example.mac_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ObsidianService {

    private final Path vaultPath;

    public ObsidianService(
            @Value("${obsidian.vault-path}") String vaultPath
    ) {
        this.vaultPath = Path.of(vaultPath);
    }

    public void createDailyNote(String date, String content) {
        try {
            Path dailyDirectory = vaultPath.resolve("03_Daily");

            Files.createDirectories(dailyDirectory);

            Path dailyNote = dailyDirectory.resolve(date + ".md");

            Files.writeString(dailyNote, content, StandardCharsets.UTF_8);

            System.out.println("Obsidian Daily Note 생성 완료: " + dailyNote);

        } catch (IOException e) {
            throw new RuntimeException("Obsidian Daily Note 생성 실패", e);
        }
    }

    public void appendToDailyNote(String date, String content) {
        try {
            Path dailyNote = vaultPath.resolve("03_Daily").resolve(date + ".md");

            if (!Files.exists(dailyNote)) {
                throw new IllegalArgumentException("Daily Note가 존재하지 않습니다: " + dailyNote);
            }

            String existingContent = Files.readString(dailyNote, StandardCharsets.UTF_8);

            String newsHeading = "## 📰 오늘의 뉴스";
            String nextHeading = "## 📝 오늘의 메모";

            int newsIndex = existingContent.indexOf(newsHeading);
            int nextIndex = existingContent.indexOf(nextHeading);

            if (newsIndex == -1) {throw new IllegalArgumentException("Daily Note에 뉴스 섹션이 없습니다.");}

            if (nextIndex == -1 || nextIndex <= newsIndex) {
                throw new IllegalArgumentException("Daily Note의 섹션 구조가 올바르지 않습니다.");
            }

            String updatedContent = existingContent.substring(0, nextIndex) + content.trim() + "\n\n" + existingContent.substring(nextIndex);

            Files.writeString(dailyNote, updatedContent, StandardCharsets.UTF_8);

            System.out.println("Obsidian Daily Note 업데이트 완료: " + dailyNote);

        } catch (Exception e) {
            System.err.println("Obsidian Daily Note 업데이트 실패");
            e.printStackTrace();

            throw new RuntimeException("Obsidian Daily Note 업데이트 실패", e);
        }
    }
}