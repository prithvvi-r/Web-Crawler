package com.spider.web;

import java.util.Set;
import java.util.concurrent.*;

public class ConcurrentWebCrawler {

    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final ThreadPoolExecutor executorService;
    private final int maxDepth;
    private final String startUrl;

    public ConcurrentWebCrawler(String startUrl, int numThreads, int maxDepth) {
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
        this.executorService = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * Starts the crawling process by submitting the first task.
     */
    public void startCrawling() {
        System.out.println("Starting crawl from: " + startUrl);
        // Submit the very first task to kick things off
        executorService.submit(new CrawlerTask(startUrl, executorService, visitedUrls, maxDepth, 0));
    }

    /**
     * Waits for all crawling tasks to complete, then shuts down the executor.
     * This is the key fix - we wait for completion BEFORE shutting down.
     */
    public void awaitTermination() {
        try {
            // Keep checking if crawling is complete
            while (true) {
                Thread.sleep(1000); // Check every second

                // Print current stats
                System.out.println("Active tasks: " + executorService.getActiveCount() +
                        ", Queued tasks: " + executorService.getQueue().size() +
                        ", URLs found: " + visitedUrls.size());

                // If no tasks are running AND no tasks are queued, we're done
                if (executorService.getActiveCount() == 0 && executorService.getQueue().isEmpty()) {
                    // Wait a bit more to be absolutely sure
                    Thread.sleep(2000);

                    // Double-check
                    if (executorService.getActiveCount() == 0 && executorService.getQueue().isEmpty()) {
                        System.out.println("All crawling tasks completed. Shutting down...");
                        break;
                    }
                }
            }

        } catch (InterruptedException e) {
            System.err.println("Crawling interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            // NOW we can safely shut down
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate gracefully, forcing shutdown.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getVisitedUrlCount() {
        return visitedUrls.size();
    }

    public void printAllUrls() {
        System.out.println("\n=== All Discovered URLs ===");
        visitedUrls.forEach(System.out::println);
        System.out.println("============================");
    }
}