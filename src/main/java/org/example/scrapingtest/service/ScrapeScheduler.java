package org.example.scrapingtest.service;

import org.example.scrapingtest.model.Subjects;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ScrapeScheduler {

    private final ScrapeCourseService scrapeCourseService;
    private final CourseService courseService;

    public ScrapeScheduler(ScrapeCourseService scrapeCourseService, CourseService courseService) {
        this.scrapeCourseService = scrapeCourseService;
        this.courseService = courseService;
    }

    // run every day at 2:00 AM (cron configurable)
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyScrapeAndSave() {
        // these credentials should be set via environment variables for security
        String username = System.getenv().getOrDefault("SCRAPER_USER", "yourUser");
        String password = System.getenv().getOrDefault("SCRAPER_PASS", "yourPass");
        String faculty = System.getenv().getOrDefault("SCRAPER_FACULTY", "IT");

        Map<String, List<Map<String, String>>> courseData = scrapeCourseService.scrapeCourses(username, password, faculty);
        List<Subjects> saved = courseService.saveCourse(courseData);
        System.out.println("Scraped and saved " + saved.size() + " subjects");
    }
}

