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

    //Notion 일정 추가 Tool의 입력값
    public record AddEventRequest(
            @Description("추가할 일정의 제목. 예: 프로젝트 회의, 병원 예약")
            String title,
            @Description("일정의 시작 날짜와 시간.ISO-8601 형식으로 입력합니다. 예: 2026-08-27T15:00:00")
            String startDateIso
    ) {}

    //Notion에서 검색할 일정의 제목
    public record FindEventRequest(
            @Description("검색할 일정의 제목")
            String title
    ){}

    //일정을 추가하는 Tool
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

    //제목을 기준으로 Notion 일정을 검색하는 Tool
    public ToolCallback getFindEventTool(){
        return FunctionToolCallback.builder("findNotionEvent",
            (FindEventRequest req) -> {
                String pageId = notionService.findEventByTitle(req.title());

                //해당 제목의 일정이 없는 경우
                if(pageId == null){
                    return "해당 제목의 일정을 찾을 수 없습니다.";
                }

                return "찾은 일정의 Page ID : " + pageId;
            }
        ).description("""
            사용자가 특정 Notion 일정을 찾거나,
            일정의 수정 또는 삭제를 요청할 때
            해당 일정의 Page ID를 찾기 위해 사용합니다.

            title에는 사용자가 언급한 일정의 제목을 입력합니다.

            예시:
            사용자: "정보통신공학과 회의를 찾아줘"

            title: "정보통신공학과 회의"
            """)
            .inputType(FindEventRequest.class)
            .build();
    }
}