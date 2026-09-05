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
            String date,
            String content
    ) {}

    public FunctionToolCallback<AppendDailyNoteRequest, String>
    getAppendToDailyNoteTool() {

        return FunctionToolCallback
                .builder(
                        "append_to_daily_note",
                        (AppendDailyNoteRequest request) -> {

                            obsidianService.appendToDailyNote(
                                    request.date(),
                                    request.content()
                            );

                            return "Daily Note에 내용을 저장했습니다.";
                        }
                )
                .description(
                        "Obsidian의 Daily Note에 Markdown 내용을 추가합니다. " +
                                "date는 YYYY-MM-DD 형식입니다."
                )
                .inputType(AppendDailyNoteRequest.class)
                .build();
    }
}