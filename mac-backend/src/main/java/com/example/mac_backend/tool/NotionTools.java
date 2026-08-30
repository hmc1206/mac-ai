package com.example.mac_backend.tool;

import com.example.mac_backend.model.EventInfo;
import com.example.mac_backend.model.EventSearchResult;
import com.example.mac_backend.service.NotionService;
import com.example.mac_backend.state.AgentState;
import com.example.mac_backend.state.AgentStateStore;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotionTools {

    private final NotionService notionService;
    private final AgentStateStore agentStateStore;

    public NotionTools(NotionService notionService, AgentStateStore agentStateStore) {
        this.notionService = notionService;
        this.agentStateStore = agentStateStore;
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

    //AgentState에 저장된 검색 결과에서 사용자가 선택한 이벤트를 찾아주는 Tool
    public record SelectEventRequest(
            @Description("""
                    검색 결과에서 선택할 일정의 번호입니다.
                    첫 번째 일정은 1, 두번째 일정은 2입니다.
                    """)
            int selection
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
            (FindEventRequest req, ToolContext toolContext) -> {
                System.out.println("=== Notion 일정 검색 Tool 호출 ===");
                System.out.println("title: " + req.title());

                // 현재 대화의 conversationId 가져오기
                String conversationId = (String) toolContext.getContext().get("conversationId");

                // Notion에서 일정 검색
                EventSearchResult result = notionService.findEventByTitle(req.title());

                // 현재 대화에 해당하는 AgentState 가져오기
                AgentState state = agentStateStore.getOrCreate(conversationId);

                // 검색 결과를 AgentState에 저장
                state.setSearchResults(result.events());

                System.out.println(
                        "검색 결과 타입: " + result.status()
                );

                System.out.println(
                        "검색 결과 개수: " + result.events().size()
                );

                System.out.println(
                        "AgentState 저장 완료 - conversationId: "
                                + conversationId
                );

                System.out.println(
                        "AgentState 검색 결과 개수: "
                                + state.getSearchResults().size()
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
                        (ArchiveEventRequest req, ToolContext toolContext) -> {
                            System.out.println(
                                    "=== Notion 일정 삭제 Tool 호출 ==="
                            );

                            String conversationId =
                                    (String) toolContext.getContext()
                                            .get("conversationId");

                            AgentState state =
                                    agentStateStore.getOrCreate(conversationId);

                            EventInfo selectedEvent =
                                    state.getSelectedEvent();

                            if (selectedEvent == null) {
                                return "삭제할 일정이 선택되지 않았습니다.";
                            }

                            String pageId =
                                    selectedEvent.pageId();

                            System.out.println(
                                    "삭제할 Page ID: " + pageId
                            );

                            return notionService.archiveEvent(pageId);
                        }
                )
                .description("""
                    특정 Notion 일정을 삭제할 때 사용하는 Tool입니다.
                        
                     사용자가 일정 제목으로 삭제를 요청했고
                     실제 Page ID를 제공하지 않은 경우에는
                     이 Tool을 바로 호출하면 안 됩니다.
    
                     반드시 먼저 findNotionEvent를 사용하여
                     일정을 검색해야 합니다.
    
                     검색 결과가 여러 개인 경우:
                     1. 사용자에게 선택을 요청합니다.
                     2. 사용자의 선택에 따라 selectNotionEvent를 호출합니다.
                     3. selectNotionEvent가 반환한 EventInfo의
                        실제 pageId를 사용합니다.
                     4. 그 pageId를 사용하여 이 Tool을 호출합니다.
    
                     pageId에는 반드시 실제 Notion Page ID를 사용합니다.
    
                     "<page-id>" 같은 placeholder를 사용하면 안 됩니다.
                     Page ID를 추측해서도 안 됩니다.
    
                     예:
    
                     사용자:
                     "정보통신공학과 회의를 삭제해줘"
    
                     검색 결과가 여러 개인 경우:
    
                     findNotionEvent
                     ->
                     사용자에게 선택 요청
                     ->
                     사용자가 "두 번째"라고 응답
                     ->
                     selectNotionEvent(selection=2)
                     ->
                     선택된 EventInfo의 실제 pageId 사용
                     ->
                     archiveNotionEvent(pageId)
    
                     실제 Page ID를 확보하지 못한 경우
                     이 Tool을 호출하지 않습니다.
                    """)
                .inputType(ArchiveEventRequest.class)
                .build();
    }

    public ToolCallback getSelectEventTool() {
        return FunctionToolCallback.builder(
                        "selectNotionEvent",
                        (SelectEventRequest req, ToolContext toolContext) -> {

                            System.out.println(
                                    "=== Notion 일정 선택 Tool 호출 ==="
                            );

                            System.out.println(
                                    "선택 번호: " + req.selection()
                            );

                            String conversationId =
                                    (String) toolContext.getContext()
                                            .get("conversationId");

                            AgentState state =
                                    agentStateStore.getOrCreate(conversationId);

                            List<EventInfo> events =
                                    state.getSearchResults();

                            if (events == null || events.isEmpty()) {
                                return "현재 선택할 수 있는 검색 결과가 없습니다.";
                            }

                            int index = req.selection() - 1;

                            if (index < 0 || index >= events.size()) {
                                return "잘못된 선택입니다. 1부터 "
                                        + events.size()
                                        + "까지 선택할 수 있습니다.";
                            }

                            EventInfo selectedEvent =
                                    events.get(index);

                            state.setSelectedEvent(selectedEvent);

                            System.out.println(
                                    "선택된 일정 저장: " + selectedEvent
                            );

                            return selectedEvent;
                        }
                )
                .description("""
                    이전에 findNotionEvent Tool로 검색한 일정 목록에서
                    사용자가 선택한 일정을 찾습니다.

                    중요:
                    이 Tool은 새로운 Notion 검색을 수행하지 않습니다.

                    반드시 AgentState에 저장된 검색 결과를 사용합니다.

                    사용자가:
                    - 첫 번째
                    - 1번
                    - 두 번째
                    - 2번

                    등으로 특정 일정을 선택했을 때 사용합니다.

                    selection은 1부터 시작합니다.

                    예:
                    검색 결과가 2개이고 사용자가 "두 번째"라고 하면
                    selection = 2

                    선택된 일정의 실제 Notion Page ID를 반환합니다.
                    """)
                .inputType(SelectEventRequest.class)
                .build();
    }
}