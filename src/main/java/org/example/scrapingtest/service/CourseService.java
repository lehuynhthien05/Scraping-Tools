package org.example.scrapingtest.service;

import org.example.scrapingtest.model.ClassCode;
import org.example.scrapingtest.model.SubjectLecturer;
import org.example.scrapingtest.model.Subjects;
import org.example.scrapingtest.repository.ClassCodeRepository;
import org.example.scrapingtest.repository.CourseRepository;
import org.example.scrapingtest.repository.SubjectLecturerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final ClassCodeRepository classCodeRepository;
    private final SubjectLecturerRepository subjectLecturerRepository;

    public CourseService(CourseRepository courseRepository,
                         ClassCodeRepository classCodeRepository,
                         SubjectLecturerRepository subjectLecturerRepository) {
        this.courseRepository = courseRepository;
        this.classCodeRepository = classCodeRepository;
        this.subjectLecturerRepository = subjectLecturerRepository;
    }


    @Transactional(readOnly = true)
    public List<Subjects> getAllCourses() {
        return courseRepository.findAll();
    }

    // Group rows into Subjects and persist them to DB (merge with existing records)
    public List<Subjects> saveCourse(Map<String, List<Map<String, String>>> courseData) {
        class Temp {
            String subjectName;
            Set<String> codes = new LinkedHashSet<>();
            Set<String> lecturers = new LinkedHashSet<>();
        }

        Map<String, Temp> tempMap = new LinkedHashMap<>();

        for (List<Map<String, String>> courses : courseData.values()) {
            for (Map<String, String> row : courses) {
                String subjectId = row.getOrDefault("Mã MH", "").trim();
                if (subjectId.isEmpty()) continue;

                String subjectName = row.getOrDefault("Tên môn học", "").trim();
                String classCodeRaw = row.getOrDefault("Mã lớp", "").trim();
                String lecturerRaw = row.getOrDefault("Giảng viên", "").trim();

                Temp t = tempMap.computeIfAbsent(subjectId, k -> {
                    Temp x = new Temp();
                    x.subjectName = subjectName;
                    return x;
                });

                // collect codes
                List<String> codes = parseCodes(classCodeRaw);
                for (String c : codes) t.codes.add(c);

                // collect lecturers
                if (!lecturerRaw.isEmpty()) {
                    String[] parts = lecturerRaw.split("[\\n,;]");
                    for (String p : parts) {
                        String v = p.trim();
                        if (!v.isEmpty()) t.lecturers.add(v);
                    }
                }

                // update subjectName if empty
                if ((t.subjectName == null || t.subjectName.isEmpty()) && !subjectName.isEmpty()) {
                    t.subjectName = subjectName;
                }
            }
        }

        // Fetch existing subjects from DB
        Set<String> ids = new HashSet<>(tempMap.keySet());
        Map<String, Subjects> existingMap = new HashMap<>();
        if (!ids.isEmpty()) {
            List<Subjects> existing = courseRepository.findAllById(ids);
            for (Subjects e : existing) existingMap.put(e.getSubjectId(), e);
        }

        List<Subjects> toSave = new ArrayList<>();

        // Build Subjects entities with many-to-many associations
        for (Map.Entry<String, Temp> ent : tempMap.entrySet()) {
            String id = ent.getKey();
            Temp t = ent.getValue();

            Subjects subject = existingMap.getOrDefault(id, new Subjects());
            subject.setSubjectId(id);
            if (t.subjectName != null && !t.subjectName.isEmpty()) subject.setSubjectName(t.subjectName);

            if (subject.getClassCodes() == null) subject.setClassCodes(new ArrayList<>());
            if (subject.getLecturers() == null) subject.setLecturers(new ArrayList<>());

            // Ensure ClassCode and SubjectLecturer entities exist (or create) and add to subject lists
            for (String code : t.codes) {
                List<ClassCode> matches = classCodeRepository.findByClassCode(code);
                ClassCode cc;
                if (!matches.isEmpty()) {
                    // prefer one already linked to this subject
                    cc = matches.stream().filter(m -> subject.getClassCodes().stream().anyMatch(x -> Objects.equals(x.getId(), m.getId()))).findFirst().orElse(matches.get(0));
                } else {
                    ClassCode n = new ClassCode();
                    n.setClassCode(code);
                    cc = classCodeRepository.save(n);
                }
                boolean present = subject.getClassCodes().stream().anyMatch(x -> Objects.equals(x.getId(), cc.getId()) || Objects.equals(x.getClassCode(), cc.getClassCode()));
                if (!present) subject.getClassCodes().add(cc);
            }

            for (String lec : t.lecturers) {
                List<SubjectLecturer> matches = subjectLecturerRepository.findByLecturer(lec);
                SubjectLecturer sl;
                if (!matches.isEmpty()) {
                    sl = matches.stream().filter(m -> subject.getLecturers().stream().anyMatch(x -> Objects.equals(x.getId(), m.getId()))).findFirst().orElse(matches.get(0));
                } else {
                    SubjectLecturer n = new SubjectLecturer();
                    n.setLecturer(lec);
                    sl = subjectLecturerRepository.save(n);
                }
                boolean present = subject.getLecturers().stream().anyMatch(x -> Objects.equals(x.getId(), sl.getId()) || Objects.equals(x.getLecturer(), sl.getLecturer()));
                if (!present) subject.getLecturers().add(sl);
            }

            toSave.add(subject);
        }

        // persist to DB (insert new and update existing), relationships saved via join tables
        return courseRepository.saveAll(toSave);
    }

    // parse class codes or similar cell into list by splitting on newline, comma, or semicolon
    private List<String> parseCodes(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return out;
        String[] parts = raw.split("[\\n,;]");
        for (String p : parts) {
            String v = p.trim();
            if (!v.isEmpty() && !out.contains(v)) out.add(v);
        }
        return out;
    }
}
