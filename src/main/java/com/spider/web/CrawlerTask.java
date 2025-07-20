package com.spider.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

public class CrawlerTask implements Runnable {

    private final String url;
    private final ThreadPoolExecutor executor;
    private final Set<String> visitedUrls;
    private final int maxDepth;
    private final int currentDepth;

    public CrawlerTask(String url, ThreadPoolExecutor executor, Set<String> visitedUrls, int maxDepth, int currentDepth) {
        this.url = url;
        this.executor = executor;
        this.visitedUrls = visitedUrls;
        this.maxDepth = maxDepth;
        this.currentDepth = currentDepth;
    }

    @Override
    public void run() {
        // Stop crawling if we've reached the maximum depth
        if (currentDepth >= maxDepth) {
            return;
        }

        // Check if this URL was already processed - the add() method is atomic
        // and returns true only if the URL was actually added (wasn't there before)
        if (!visitedUrls.add(url)) {
            return; // URL already processed by another thread
        }

        try {
            System.out.println("Crawling (depth " + currentDepth + "): " + url);

            // Use Jsoup to fetch the HTML content of the URL
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000) // Increased timeout for Wikipedia
                    .followRedirects(true)
                    .get();

            // Find all links <a> elements with an "href" attribute
            Elements linksOnPage = document.select("a[href]");

            int linksFound = 0;
            // For each link, submit new crawling tasks
            for (Element link : linksOnPage) {
                String newUrl = link.attr("abs:href");

                // Basic URL validation and filtering
                if (!newUrl.isEmpty() &&
                        newUrl.startsWith("http") &&
                        !visitedUrls.contains(newUrl) &&
                        isValidUrl(newUrl)) {

                    // Submit new task for the discovered URL
                    CrawlerTask newTask = new CrawlerTask(newUrl, executor, visitedUrls, maxDepth, currentDepth + 1);
                    executor.submit(newTask);
                    linksFound++;
                }
            }

            System.out.println("  -> Found " + linksFound + " new links to crawl from depth " + currentDepth);

        } catch (IOException e) {
            System.err.println("Error crawling '" + url + "': " + e.getMessage());
        }
    }

    /**
     * Basic URL filtering to avoid problematic URLs
     */
    private boolean isValidUrl(String url) {
        // Skip certain file types and special URLs
        if (url.contains("#") ||
                url.endsWith(".pdf") ||
                url.endsWith(".jpg") ||
                url.endsWith(".png") ||
                url.endsWith(".gif") ||
                url.contains("mailto:") ||
                url.contains("javascript:")) {
            return false;
        }


        return true;
    }
}