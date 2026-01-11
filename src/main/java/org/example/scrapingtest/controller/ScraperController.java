package org.example.scrapingtest.controller;

import org.example.scrapingtest.model.Subjects;
import org.example.scrapingtest.service.CourseService;
import org.example.scrapingtest.service.ScrapeCourseService;
import org.example.scrapingtest.service.ScrapeScheduleService;
import org.springframework.http.ResponseEntity;
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
    private final CourseService subjectService;

    public ScraperController(ScrapeScheduleService scrapeScheduleService, 
                            ScrapeCourseService scrapeCourseService,
                            CourseService subjectService) {
        this.scrapeScheduleService = scrapeScheduleService;
        this.scrapeCourseService = scrapeCourseService;
        this.subjectService = subjectService;
    }

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

    /**
     * Scrape courses and save to database
     * @param username Username for login
     * @param password Password for login
     * @param faculty Faculty code
     * @return Number of courses saved
     */
    @GetMapping("/save")
    public ResponseEntity<String> scrapeCoursesSave(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String faculty
    ) {
        Map<String, List<Map<String, String>>> courseData = scrapeCourseService.scrapeCourses(username, password, faculty);
        int savedCount = subjectService.convertAndSaveCourseData(courseData);

        return ResponseEntity.ok("Saved " + savedCount + " courses to database");
    }

    @GetMapping("/student")
    public String scrapeByStudentIdParam(
            @RequestParam(name = "id") String studentId
    ) {
        Map<String, List<Map<String, String>>> courseData = scrapeScheduleService.scrapeCoursesByStudentId(studentId);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(courseData);
    }

    /**
     * Get all courses from the database
     * @return List of all courses
     */
    @GetMapping("/courses")
    public ResponseEntity<List<Subjects>> getAllCourses() {
        return ResponseEntity.ok(subjectService.getAllCourses());
    }

    /**
     * Save student schedule to database
     * @param studentId Student ID
     * @return Number of courses saved
     */
    @GetMapping("/student/save")
    public ResponseEntity<String> saveStudentSchedule(@RequestParam(name = "id") String studentId) {
        Map<String, List<Map<String, String>>> courseData = scrapeScheduleService.scrapeCoursesByStudentId(studentId);
        int savedCount = subjectService.convertAndSaveCourseData(courseData);

        return ResponseEntity.ok("Saved " + savedCount + " courses to database");
    }
}
