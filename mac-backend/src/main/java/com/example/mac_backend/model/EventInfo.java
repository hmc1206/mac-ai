package com.example.mac_backend.model;

public record EventInfo(
        String pageId,
        String title,
        String date,
        String status
) {
}