package com.example.mac_backend.model;

import java.util.List;

public record EventSearchResult(
        SearchStatus status,
        List<EventInfo> events
) {
}