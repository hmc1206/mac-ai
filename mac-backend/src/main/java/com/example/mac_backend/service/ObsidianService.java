package com.example.mac_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Service
public class ObsidianService {

    private final Path vaultPath;

    public ObsidianService(
            @Value("${obsidian.vault-path}") String vaultPath
    ) {
        this.vaultPath = Path.of(vaultPath);
    }


    //Daily Note 기본 템플릿을 생성한다.
    private String createDailyNoteTemplate(String date) {
        return """
                ---
                type: daily
                date: %s
                tags:
                  - daily
                ---

                # %s

                ## 📰 오늘의 뉴스

                ## 📝 오늘의 메모

                ## 🎯 오늘 할 일

                """.formatted(date, date);
    }

    //오늘 날짜의 Daily Note가 존재하는지 확인 후 없으면 기본 템플릿으로 생성
    private Path ensureTodayDailyNote() {
        String today = LocalDate.now().toString();

        Path dailyDirectory = vaultPath.resolve("03_Daily");
        Path dailyNote = dailyDirectory.resolve(today + ".md");

        try {
            // 03_Daily 디렉터리가 없으면 생성
            Files.createDirectories(dailyDirectory);

            // 오늘의 Daily Note가 없으면 생성
            if (!Files.exists(dailyNote)) {
                Files.writeString(
                        dailyNote,
                        createDailyNoteTemplate(today),
                        StandardCharsets.UTF_8
                );

                System.out.println(
                        "Obsidian Daily Note 생성 완료: " + dailyNote
                );
            }

            return dailyNote;

        } catch (IOException e) {
            throw new RuntimeException(
                    "오늘의 Obsidian Daily Note 생성 실패",
                    e
            );
        }
    }


    // 오늘 날짜의 Daily Note를 생성한다.
    // 현재는 테스트 및 명시적인 Daily Note 생성에 사용한다.
    public void createDailyNote(String content) {
        String today = LocalDate.now().toString();

        createDailyNote(today, content);
    }

    // 지정한 날짜의 Daily Note를 생성한다.
    private void createDailyNote(String date, String content) {
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

    // 오늘 날짜의 Daily Note에 내용을 추가한다.
    // 오늘의 Daily Note가 존재하지 않으면 먼저 기본 템플릿으로 생성한 후 내용을 추가한다.
    public void appendToTodayDailyNote(String content) {

        Path dailyNote = ensureTodayDailyNote();

        String today = LocalDate.now().toString();

        appendToDailyNote(today, content);
    }

    // 지정한 날짜의 Daily Note에 내용을 추가한다.
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

            // 뉴스 섹션이 존재하는지 확인
            if (newsIndex == -1) {
                throw new IllegalArgumentException("Daily Note에 뉴스 섹션이 없습니다.");
            }

            // 메모 섹션이 뉴스 섹션보다 뒤에 있는지 확인
            if (nextIndex == -1 || nextIndex <= newsIndex) {
                throw new IllegalArgumentException("Daily Note의 섹션 구조가 올바르지 않습니다.");
            }

            // 뉴스 섹션과 메모 섹션 사이에 내용을 삽입한다.
            String updatedContent =
                    existingContent.substring(0, nextIndex)
                            + content.trim()
                            + "\n\n"
                            + existingContent.substring(nextIndex);

            Files.writeString(dailyNote, updatedContent, StandardCharsets.UTF_8);

            System.out.println("Obsidian Daily Note 업데이트 완료: " + dailyNote);

        } catch (IllegalArgumentException e) {
            // 우리가 의도적으로 발생시킨 검증 오류
            System.err.println("Obsidian Daily Note 업데이트 실패: " + e.getMessage());

            throw e;
        } catch (IOException e) {
            // 파일 I/O 오류
            System.err.println("Obsidian Daily Note 파일 처리 실패");

            throw new RuntimeException("Obsidian Daily Note 업데이트 실패", e);
        }
    }
}