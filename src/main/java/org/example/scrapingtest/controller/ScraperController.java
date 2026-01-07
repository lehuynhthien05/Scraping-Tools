package org.example.scrapingtest.controller;

import org.example.scrapingtest.service.EduSoftScrapeService;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scrape")
public class ScraperController {

    private final EduSoftScrapeService scrapeService;

    public ScraperController(EduSoftScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    /**
     * Call example:
     * http://localhost:8080/scrape?username={username}&password={password}
     */
    @GetMapping
    public String scrape(
            @RequestParam String username,
            @RequestParam String password
    ) {
        Map<String, List<Map<String, String>>> courseData = scrapeService.scrapeCourses(username, password);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(courseData);
    }
}
