package org.example.scrapingtest.service;

import org.example.scrapingtest.model.Subjects;
import org.example.scrapingtest.model.Schedule;
import org.example.scrapingtest.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public int convertAndSaveCourseData(Map<String, List<Map<String, String>>> courseData) {

        Map<String, Subjects> subjectMap = new HashMap<>();

        for (List<Map<String, String>> courses : courseData.values()) {

            for (Map<String, String> row : courses) {

                String subjectId = row.getOrDefault("Mã MH", "").trim();
                if (subjectId.isEmpty()) continue;

                // ===== create Subject only once =====
                Subjects subject = subjectMap.computeIfAbsent(subjectId, id -> {
                    Subjects s = new Subjects();
                    s.setSubjectId(id);
                    s.setClassCode(row.getOrDefault("Mã lớp", ""));
                    s.setCredits(parseInt(row.get("STC")));
                    s.setCapacity(parseInt(row.get("Sĩ số")));
                    s.setSubjectName(row.getOrDefault("Tên môn học", ""));
                    return s;
                });

                // ===== parse schedule =====
                String[] days = split(row.get("Thứ"));
                String[] starts = split(row.get("Tiết BD"));
                String[] durs = split(row.get("ST"));
                String[] rooms = split(row.get("Phòng"));
                String[] lecturers = split(row.get("Giảng viên"));

                int max = maxLen(days, starts, durs, rooms, lecturers);

                for (int i = 0; i < max; i++) {
                    if (get(days, i).isEmpty() || get(rooms, i).isEmpty()) continue;

                    Schedule sc = new Schedule();
                    sc.setDayOfWeek(get(days, i));
                    sc.setStartPeriod(parseInt(get(starts, i)));
                    sc.setDuration(parseInt(get(durs, i)));
                    sc.setRoom(get(rooms, i));
                    sc.setLecturer(get(lecturers, i));

                    subject.addSchedule(sc);
                }
            }
        }

        courseRepository.saveAll(subjectMap.values());
        return subjectMap.size();
    }

    // ===== helper methods =====
    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private String[] split(String s) {
        return (s == null) ? new String[]{""} : s.split("\\n");
    }

    private String get(String[] arr, int i) {
        return i < arr.length ? arr[i].trim() : arr[0].trim();
    }

    private int maxLen(String[]... arrs) {
        int m = 0;
        for (String[] a : arrs) m = Math.max(m, a.length);
        return m;
    }

    @Transactional(readOnly = true)
    public List<Subjects> getAllCourses() {
        return courseRepository.findAll();
    }
}
