package com.example.mac_backend.tool;

import com.example.mac_backend.service.NotionService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Component
public class NotionTools {

    private final NotionService notionService;

    public NotionTools(NotionService notionService) {
        this.notionService = notionService;
    }

    /**
     * Notion 일정 추가 Tool의 입력값
     */
    public record AddEventRequest(
            @Description("추가할 일정의 제목. 예: 프로젝트 회의, 병원 예약")
            String title,
            @Description("일정의 시작 날짜와 시간.ISO-8601 형식으로 입력합니다. 예: 2026-08-27T15:00:00")
            String startDateIso
    ) {}

    public ToolCallback getAddEventTool() {

        return FunctionToolCallback.builder("addNotionEvent", (AddEventRequest req) -> {
                            System.out.println("=== AI Tool 호출 ===");
                            System.out.println("title: " + req.title());
                            System.out.println("startDateIso: " + req.startDateIso());

                            return notionService.addCalendarEvent(req.title(), req.startDateIso());
                        }
                )
                .description("""
                        사용자가 일정, 약속, 회의, 예약 등을
                        Notion 캘린더에 추가해달라고 요청할 때 사용합니다.

                        title에는 일정의 제목을 입력합니다.
                        startDateIso에는 일정의 날짜와 시간을ISO-8601 형식으로 입력합니다.
                        
                        예시:
                        사용자:"내일 오후 3시에 프로젝트 회의 일정 추가해줘"
                        title:"프로젝트 회의"
                        """)
                .inputType(AddEventRequest.class)
                .build();
    }
}