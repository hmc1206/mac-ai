package com.example.mac_backend.tool;

import com.example.mac_backend.service.ObsidianService;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

@Component
public class ObsidianTools {

    private final ObsidianService obsidianService;

    public ObsidianTools(ObsidianService obsidianService) {
        this.obsidianService = obsidianService;
    }

    public record AppendDailyNoteRequest(
            String content
    ) {}

    public FunctionToolCallback<AppendDailyNoteRequest, String>
    getAppendToTodayDailyNoteTool() {

        return FunctionToolCallback
                .builder("append_to_today_daily_note",
                        (AppendDailyNoteRequest request) -> {
                            obsidianService.appendToTodayDailyNote(request.content());

                            return "오늘의 Daily Note에 내용을 저장했습니다.";
                        }
                )
                .description(
                        "오늘 날짜의 Obsidian Daily Note에서 " +
                                "'## 📰 오늘의 뉴스' 섹션에 뉴스 내용을 추가합니다. " +
                                "content에는 뉴스 항목만 포함해야 합니다. " +
                                "'##' 수준의 섹션 제목이나 저장 완료 메시지는 포함하지 마세요. " +
                                "각 뉴스는 '###' 제목과 요약 및 영향 정보로 구성하세요."
                )
                .inputType(AppendDailyNoteRequest.class)
                .build();
    }
}