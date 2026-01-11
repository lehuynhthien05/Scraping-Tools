package org.example.scrapingtest.controller;

import org.example.scrapingtest.model.ClassCode;
import org.example.scrapingtest.model.SubjectLecturer;
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

    /**
     * Scrape courses and return as JSON
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

    /**
     * Get all courses from the database
     */
    @GetMapping("/courses")
    public ResponseEntity<List<Subjects>> getAllCourses() {
        return ResponseEntity.ok(subjectService.getAllCourses());
    }

    /**
     * Scrape courses and persist to DB
     */
    @GetMapping("/save")
    public ResponseEntity<List<Subjects>> scrapeCoursesAndSave(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String faculty
    ) {
        Map<String, List<Map<String, String>>> courseData = scrapeCourseService.scrapeCourses(username, password, faculty);
        List<Subjects> saved = subjectService.saveCourse(courseData);
        return ResponseEntity.ok(saved);
    }

    /**
     * Return DB mappings as plain text lines: subjectId class_id lecturer_id subjectName
     */
    @GetMapping("/db-lines")
    public ResponseEntity<String> getDbLines() {
        List<Subjects> subjects = subjectService.getAllCourses();
        StringBuilder sb = new StringBuilder();

        for (Subjects s : subjects) {
            List<ClassCode> codes = s.getClassCodes();
            List<SubjectLecturer> lects = s.getLecturers();

            if ((codes == null || codes.isEmpty()) && (lects == null || lects.isEmpty())) {
                sb.append(s.getSubjectId()).append(" 0 0 ").append(s.getSubjectName() == null ? "" : s.getSubjectName()).append('\n');
                continue;
            }

            if (codes == null || codes.isEmpty()) {
                for (SubjectLecturer l : lects) {
                    sb.append(s.getSubjectId()).append(' ').append(0).append(' ').append(l.getId() == null ? 0 : l.getId()).append(' ').append(s.getSubjectName() == null ? "" : s.getSubjectName()).append('\n');
                }
                continue;
            }

            if (lects == null || lects.isEmpty()) {
                for (ClassCode c : codes) {
                    sb.append(s.getSubjectId()).append(' ').append(c.getId() == null ? 0 : c.getId()).append(' ').append(0).append(' ').append(s.getSubjectName() == null ? "" : s.getSubjectName()).append('\n');
                }
                continue;
            }

            for (ClassCode c : codes) {
                for (SubjectLecturer l : lects) {
                    sb.append(s.getSubjectId()).append(' ').append(c.getId() == null ? 0 : c.getId()).append(' ').append(l.getId() == null ? 0 : l.getId()).append(' ').append(s.getSubjectName() == null ? "" : s.getSubjectName()).append('\n');
                }
            }
        }

        return ResponseEntity.ok(sb.toString());
    }
}
