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

    //Notion 일정 상태 수정 Tool의 입력값
    public  record UpdateEventstatusRequest(
            @Description("수정할 Notion 페이지의 Page ID")
            String pageId,

            @Description("""
                    변경할 일정 상태.
                    예 : 시작 전, 진행 중, 완료
                    """)
            String status
    ) {}

    //Notion 일정 삭제 Tool의 입력값
    public record ArchiveEventRequest(
            @Description("삭제할 Notion 페이지의 Page ID")
            String pageId
    ) {}

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

    //Notion 일정 상태를 수정하는 Tool
    public ToolCallback getUpdateEventStatusTool() {
        return FunctionToolCallback.builder("updateNotionEventStatus",(UpdateEventstatusRequest req) -> {
                    System.out.println("=== Notion 일정 상태 수정 Tool 호출 ===");

                    System.out.println("pageId: " + req.pageId());
                    System.out.println("status: " + req.status());

                    return notionService.updateEventStatus(
                            req.pageId(),
                            req.status
                    );
                }
            )
            .description("""
                    Notion에 저장된 일정의 상태를 변경할 때 사용합니다.
                    
                    사용자가 다음과 같이 일정 상태 변경을 요청하면 사용합니다.
                    pageId에는 예시 값이나 "<page-id>" 같은 문자열을 넣으면 안 됩니다.
                    반드시 Notion에서 조회한 실제 Page ID를 사용해야 합니다.
    
                    예시:
                    "정보통신공학과 회의를 진행중으로 바꿔줘"
                    "프로젝트 회의를 완료로 변경해줘"
    
                    pageId에는 수정할 Notion 페이지 ID를 입력합니다.
                    status에는 변경할 상태를 입력합니다.
    
                    가능한 상태:
                    시작 전
                    진행중
                    완료
                """)
            .inputType(UpdateEventstatusRequest.class)
            .build();
    }

    //Notion 일정을 휴지통으로 이동하는 Tool
    public ToolCallback getArchiveEventTool() {

        return FunctionToolCallback.builder("archiveNotionEvent",
                        (ArchiveEventRequest req) -> {
                            System.out.println("=== Notion 일정 삭제 Tool 호출 ===");
                            System.out.println("pageId: " + req.pageId());

                            return notionService.archiveEvent(req.pageId());
                        }
                )
                .description("""
                    특정 Notion 일정을 삭제할 때 사용합니다.

                    pageId를 사용하여 해당 Notion 페이지를
                    휴지통으로 이동합니다.
                    """)
                .inputType(ArchiveEventRequest.class)
                .build();
    }
}