package com.spider.web;

public class CrawlerApp {

    public static void main(String[] args) {
        // --- Configuration ---
        String startUrl = "https://en.wikipedia.org/wiki/Web_crawler";
        int maxThreads = 4; // Reduced for better control with Wikipedia
        int maxDepth = 2;   // Reduced depth for testing

        System.out.println("Starting crawler...");
        System.out.println("Start URL: " + startUrl);
        System.out.println("Max Threads: " + maxThreads);
        System.out.println("Max Depth: " + maxDepth);
        System.out.println("=====================================");

        long startTime = System.currentTimeMillis();

        // 1. Create an instance of our crawler
        ConcurrentWebCrawler crawler = new ConcurrentWebCrawler(startUrl, maxThreads, maxDepth);

        // 2. Start the crawling process
        crawler.startCrawling();

        // 3. Wait for the crawler to finish its work
        crawler.awaitTermination();

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        System.out.println("=====================================");
        System.out.println("Crawling finished.");
        System.out.println("Total unique URLs visited: " + crawler.getVisitedUrlCount());
        System.out.println("Time taken: " + duration + " seconds");
        System.out.println("=====================================");

        // Uncomment this if want to see all discovered URLs
        // crawler.printAllUrls();
    }
}