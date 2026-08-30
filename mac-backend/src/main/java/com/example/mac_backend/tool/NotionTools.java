package com.example.mac_backend.tool;

import com.example.mac_backend.service.NotionService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

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
                System.out.println("=== Notion 일정 검색 Tool 호출 ===");
                System.out.println("title: " + req.title());

                NotionService.EventSearchResult result = notionService.findEventByTitle(req.title());

                System.out.println(
                        "검색 결과 타입: " + result.resultType()
                );

                System.out.println(
                        "검색 결과 개수: " + result.events().size()
                );

                return result;
            }
        ).description("""
           Notion 데이터베이스에서 일정 제목을 기준으로 일정을 검색합니다.
            
           중요:
           사용자가 일정의 수정 또는 삭제를 요청했는데
           실제 Notion Page ID를 제공하지 않은 경우
           반드시 이 Tool을 먼저 사용하여 일정을 검색해야 합니다.
        
           검색 결과:
        
           NOT_FOUND:
           해당 제목의 일정이 없습니다.
           수정 또는 삭제를 진행하지 않습니다.
        
           FOUND:
           해당 제목의 일정이 정확히 하나 존재합니다.
           검색 결과의 실제 Page ID를 사용하여
           수정 또는 삭제를 진행할 수 있습니다.
        
           AMBIGUOUS:
           동일한 제목의 일정이 여러 개 존재합니다.
           어떤 일정을 수정하거나 삭제해야 하는지
           명확하지 않으므로 임의로 하나를 선택하면 안 됩니다.
           날짜, 상태 등의 추가 정보를 사용자에게 요청해야 합니다.
        
           사용자가 직접 실제 Notion Page ID를 제공한 경우에는
           해당 Page ID를 사용할 수 있습니다.
        
           예시:
        
           사용자:
           "정보통신공학과 회의를 삭제해줘"
        
           처리:
           1. findNotionEvent("정보통신공학과 회의")
           2. 검색 결과 확인
           3. 하나라면 실제 Page ID로 삭제
           4. 여러 개라면 사용자에게 어떤 일정인지 질문
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
                    - 시작 전
                    - 진행 중
                    - 완료
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
                    특정 Notion 일정을 삭제할 때 사용하는 Tool입니다.
                        
                    중요:
                    사용자가 일정 제목으로 삭제를 요청했고
                    실제 Notion Page ID를 제공하지 않은 경우에는
                    이 Tool을 바로 호출하면 안 됩니다.
                
                    먼저 findNotionEvent Tool을 사용하여
                    해당 제목의 일정을 검색해야 합니다.
                
                    검색 결과가 하나인 경우:
                    - 검색 결과의 실제 Page ID를 사용하여
                      archiveNotionEvent를 호출합니다.
                
                    검색 결과가 없는 경우:
                    - 일정이 존재하지 않는다고 사용자에게 안내합니다.
                
                    검색 결과가 여러 개인 경우:
                    - 어떤 일정을 삭제해야 하는지 알 수 없으므로
                      임의로 하나를 선택하면 안 됩니다.
                    - 사용자에게 날짜 등의 추가 정보를 요청해야 합니다.
                
                    pageId에는 반드시 Notion에서 조회한 실제 Page ID를 사용합니다.
                    사용자가 제공하지 않은 Page ID를 추측하거나
                    "<page-id>" 같은 임의의 값을 사용하면 안 됩니다.
                    """)
                .inputType(ArchiveEventRequest.class)
                .build();
    }
}