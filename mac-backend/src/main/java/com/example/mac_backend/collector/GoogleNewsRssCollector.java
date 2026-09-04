package com.example.mac_backend.collector;

import com.example.mac_backend.model.NewsArticle;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleNewsRssCollector implements NewsCollector {

    @Override
    public List<NewsArticle> collectNews(String query) {
        List<NewsArticle> articles = new ArrayList<>();
        try {
            // Google News RSS URL (경제/정치 키워드 타겟팅)
            String rssUrl = "https://news.google.com/rss/search?q=" + query + "&hl=en-US&gl=US&ceid=US:en";
            URL feedSource = new URL(rssUrl);

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));

            for (SyndEntry entry : feed.getEntries()) {
                articles.add(new NewsArticle(
                        entry.getTitle(),
                        entry.getDescription() != null ? entry.getDescription().getValue() : "",
                        entry.getLink(),
                        "Google News RSS",
                        entry.getPublishedDate() != null ? entry.getPublishedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }
}