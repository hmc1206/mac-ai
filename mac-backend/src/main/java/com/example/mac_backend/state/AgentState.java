package com.example.mac_backend.state;

import com.example.mac_backend.model.EventInfo;

import java.util.List;

public class AgentState {

    private List<EventInfo> searchResults = List.of();

    private EventInfo selectedEvent;

    public List<EventInfo> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<EventInfo> searchResults) {
        this.searchResults = searchResults;
    }

    public EventInfo getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(EventInfo selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public void clear() {
        this.searchResults = List.of();
        this.selectedEvent = null;
    }
}