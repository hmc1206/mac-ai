package com.example.mac_backend.collector;

import com.example.mac_backend.model.NewsArticle;
import java.util.List;

public interface NewsCollector {
    List<NewsArticle> collectNews(String query);
}
