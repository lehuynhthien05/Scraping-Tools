package org.example.scrapingtest.controller;

import org.example.scrapingtest.service.ScrapeCourseService;
import org.example.scrapingtest.service.ScrapeScheduleService;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scrape")
public class ScraperController {

    private final ScrapeScheduleService scrapeScheduleService;
    private final ScrapeCourseService scrapeCourseService;

    public ScraperController(ScrapeScheduleService scrapeScheduleService, ScrapeCourseService scrapeCourseService) {
        this.scrapeScheduleService = scrapeScheduleService;
        this.scrapeCourseService = scrapeCourseService;
    }

    /**
     * Call example:
     * http://localhost:8080/scrape?username={username}&password={password}
     */
    @GetMapping
    public String scrapeCourses(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String faculty
    ) {
        Map<String, List<Map<String, String>>> courseData = scrapeCourseService.scrapeCourses(username, password, faculty);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(courseData);
    }

    @GetMapping("/student")
    public String scrapeByStudentIdParam(
            @RequestParam(name = "id") String studentId
    ) {
        Map<String, List<Map<String, String>>> courseData = scrapeScheduleService.scrapeCoursesByStudentId(studentId);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(courseData);
    }
}
