# Concurrent Web Crawler

A multithreaded web crawler built in Java that efficiently discovers and processes web pages using concurrent programming techniques.

## Features

- **Concurrent Processing**: Uses ThreadPoolExecutor for parallel crawling
- **Depth Control**: Configurable crawling depth to limit exploration scope
- **Thread Safety**: Employs ConcurrentHashMap for safe URL tracking across threads
- **Robust Error Handling**: Gracefully handles network errors and timeouts
- **URL Validation**: Built-in filtering for invalid or problematic URLs
- **Progress Monitoring**: Real-time statistics on crawling progress

## Architecture

The crawler consists of three main components:

- **CrawlerApp**: Main application entry point and configuration
- **ConcurrentWebCrawler**: Orchestrates the crawling process and thread management
- **CrawlerTask**: Individual crawling tasks that process single URLs

## Requirements

- Java 8 or higher
- Maven (for dependency management)
- Internet connection

## Dependencies

```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.15.3</version>
</dependency>
```

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/concurrent-web-crawler.git
cd concurrent-web-crawler
```

2. Compile the project:
```bash
javac -cp "lib/*" src/com/spider/web/*.java
```

Or using Maven:
```bash
mvn compile
```

## Usage

### Basic Usage

```java
public class CrawlerApp {
    public static void main(String[] args) {
        String startUrl = "https://example.com";
        int maxThreads = 8;
        int maxDepth = 3;

        ConcurrentWebCrawler crawler = new ConcurrentWebCrawler(startUrl, maxThreads, maxDepth);
        crawler.startCrawling();
        crawler.awaitTermination();

        System.out.println("Total URLs discovered: " + crawler.getVisitedUrlCount());
    }
}
```

### Configuration Parameters

- **startUrl**: The initial URL to begin crawling from
- **maxThreads**: Number of concurrent threads (recommended: 4-16)
- **maxDepth**: Maximum depth to crawl (0 = start page only, 1 = start + direct links, etc.)

## How It Works

1. **Initialization**: Creates a thread pool and initializes data structures
2. **Seed URL**: Submits the starting URL as the first crawling task
3. **Concurrent Processing**: Each thread:
    - Fetches a web page using Jsoup
    - Extracts all links from the page
    - Submits new crawling tasks for undiscovered URLs
    - Respects depth limits and URL validation rules
4. **Completion Detection**: Monitors for completion when no active or queued tasks remain
5. **Cleanup**: Properly shuts down thread pool and reports statistics

## Performance Considerations

- **Thread Count**: More threads don't always mean better performance. Start with 4-8 threads.
- **Depth Limit**: Higher depths can lead to exponential URL growth. Use cautiously.
- **Timeout Settings**: Adjust connection timeouts based on target website responsiveness.
- **Rate Limiting**: Consider adding delays to be respectful to target servers.

## Thread Safety

The crawler uses several thread-safe mechanisms:

- `ConcurrentHashMap.newKeySet()` for tracking visited URLs
- Atomic `add()` operations for duplicate URL prevention
- `ThreadPoolExecutor` for managing concurrent tasks
- Proper synchronization for completion detection

## URL Filtering

The crawler automatically filters out:
- Non-HTTP/HTTPS URLs
- Image files (.jpg, .png, .gif)
- Document files (.pdf)
- JavaScript and mailto links
- Fragment identifiers (#anchors)

## Example Output

```
Starting crawler...
Start URL: https://example.com
Max Threads: 4
Max Depth: 2
=====================================
Starting crawl from: https://example.com
Crawling (depth 0): https://example.com
  -> Found 23 new links to crawl from depth 0
Active tasks: 4, Queued tasks: 19, URLs found: 24
Crawling (depth 1): https://example.com/about
  -> Found 12 new links to crawl from depth 1
Active tasks: 3, Queued tasks: 30, URLs found: 36
...
All crawling tasks completed. Shutting down...
=====================================
Crawling finished.
Total unique URLs visited: 156
Time taken: 12 seconds
=====================================
```

## Customization

### Adding Custom URL Filters

Modify the `isValidUrl()` method in `CrawlerTask.java`:

```java
private boolean isValidUrl(String url) {
    // Add your custom filtering logic
    if (url.contains("admin") || url.contains("login")) {
        return false;
    }
    return url.startsWith("https://yourdomain.com");
}
```

### Adjusting Timeouts and User Agent

Modify the Jsoup connection in `CrawlerTask.java`:

```java
Document document = Jsoup.connect(url)
    .userAgent("Your Custom User Agent")
    .timeout(15000)  // 15 second timeout
    .followRedirects(true)
    .get();
```

## Best Practices

1. **Respect robots.txt**: Consider implementing robots.txt parsing
2. **Rate Limiting**: Add delays between requests to avoid overwhelming servers
3. **Error Handling**: Monitor and handle different types of HTTP errors appropriately
4. **Memory Management**: For large crawls, consider implementing URL persistence
5. **Logging**: Add proper logging instead of System.out.println for production use

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

This crawler is for educational and research purposes. Always ensure you have permission to crawl websites and comply with their terms of service and robots.txt files. Be respectful of server resources and implement appropriate rate limiting.

## Troubleshooting

### Common Issues

**Low URL Count (0-1 URLs)**
- Ensure `awaitTermination()` doesn't call `shutdown()` too early
- Check that the starting URL is accessible
- Verify internet connectivity

**OutOfMemoryError**
- Reduce `maxDepth` or `maxThreads`
- Implement URL persistence for large crawls
- Increase JVM heap size with `-Xmx` flag

**Connection Timeouts**
- Increase timeout values in Jsoup configuration
- Check firewall/proxy settings
- Verify target website accessibility

**Thread Pool Exhaustion**
- Monitor active vs queued tasks
- Adjust thread pool size based on system resources
- Implement proper backpressure mechanisms