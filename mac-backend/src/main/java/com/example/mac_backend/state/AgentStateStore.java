package com.example.mac_backend.state;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentStateStore {

    private final Map<String, AgentState> states =
            new ConcurrentHashMap<>();

    public AgentState getOrCreate(String conversationId) {
        return states.computeIfAbsent(
                conversationId,
                id -> new AgentState()
        );
    }

    public void remove(String conversationId) {
        states.remove(conversationId);
    }
}