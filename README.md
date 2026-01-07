# Web Scraping with Java and JSoup

This project demonstrates how to scrape data from websites using Java and the JSoup library in a Spring Boot application.

## Features

- Extract links from webpages
- Extract text content using CSS selectors
- Extract data from tables
- Extract image URLs
- Get raw HTML content
- Authentication support for scraping protected pages

## Project Structure

- `WebScraperService.java`: Service class with methods for different scraping operations
- `WebScraperController.java`: REST controller exposing endpoints for scraping operations
- `WebScrapingExample.java`: Standalone examples of how to use JSoup directly

## How to Use

### Using the REST API

Once the application is running, you can use the following endpoints:

1. **Extract Links**:
   ```
   GET /api/scraper/links?url=https://example.com
   ```

2. **Extract Text by CSS Selector**:
   ```
   GET /api/scraper/text?url=https://example.com&selector=p
   ```

3. **Extract Table Data**:
   ```
   GET /api/scraper/table?url=https://example.com&tableSelector=table
   ```

4. **Extract Image URLs**:
   ```
   GET /api/scraper/images?url=https://example.com
   ```

5. **Get Raw HTML**:
   ```
   GET /api/scraper/raw?url=https://example.com
   ```

### Using the Service Directly

You can inject the `WebScraperService` into your own components:

```java
@Service
public class YourService {
    
    private final WebScraperService webScraperService;
    
    @Autowired
    public YourService(WebScraperService webScraperService) {
        this.webScraperService = webScraperService;
    }
    
    public void yourMethod() {
        // Extract links from a webpage
        List<String> links = webScraperService.extractLinks("https://example.com");
        
        // Extract text content using CSS selectors
        List<String> paragraphs = webScraperService.extractTextBySelector("https://example.com", "p");
        
        // Extract data from tables
        List<Map<String, String>> tableData = webScraperService.extractTableData("https://example.com", "table");
        
        // Extract image URLs
        List<String> imageUrls = webScraperService.extractImageUrls("https://example.com");
    }
}
```

### Standalone Usage

If you want to use JSoup directly without the service or controller, check the `WebScrapingExample.java` file for examples:

```java
// Basic scraping - get the title of a webpage
Document doc = Jsoup.connect("https://example.com")
        .userAgent("Mozilla/5.0")
        .timeout(5000)
        .get();

String title = doc.title();
System.out.println("Title: " + title);

// Extract all links from a webpage
Elements links = doc.select("a[href]");
for (Element link : links) {
    System.out.println("Link: " + link.attr("abs:href") + " - Text: " + link.text());
}
```

## Best Practices for Web Scraping

1. **Respect robots.txt**: Always check the website's robots.txt file to ensure you're allowed to scrape it.
2. **Add delays**: Don't overload the server with too many requests in a short time.
3. **Use a proper User-Agent**: Identify your scraper with a proper User-Agent header.
4. **Handle errors gracefully**: Websites can change their structure, so make your scraper resilient.
5. **Cache results**: Avoid re-scraping the same content repeatedly.
6. **Be ethical**: Only scrape publicly available data and respect the website's terms of service.

## Dependencies

- Spring Boot
- JSoup: Java HTML Parser
- Lombok (for reducing boilerplate code)

## Legal Considerations

Web scraping may be subject to legal restrictions. Always:
- Check the website's Terms of Service
- Respect copyright and intellectual property rights
- Consider using official APIs if available
- Obtain permission when necessary