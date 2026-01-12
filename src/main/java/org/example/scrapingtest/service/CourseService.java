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
                t.codes.addAll(codes);

                // collect lecturers
                if (!lecturerRaw.isEmpty()) {
                    String[] parts = lecturerRaw.split("[\\n,;]");
                    for (String p : parts) {
                        String v = p.trim();
                        if (!v.isEmpty()) t.lecturers.add(v);
                    }
                }

                if ((t.subjectName == null || t.subjectName.isEmpty()) && !subjectName.isEmpty()) {
                    t.subjectName = subjectName;
                }
            }
        }

        Set<String> ids = new HashSet<>(tempMap.keySet());
        Map<String, Subjects> existingMap = new HashMap<>();
        if (!ids.isEmpty()) {
            List<Subjects> existing = courseRepository.findAllById(ids);
            for (Subjects e : existing) existingMap.put(e.getSubjectId(), e);
        }

        List<Subjects> toSave = new ArrayList<>();

        for (Map.Entry<String, Temp> ent : tempMap.entrySet()) {
            String id = ent.getKey();
            Temp t = ent.getValue();

            Subjects subject = existingMap.getOrDefault(id, new Subjects());
            subject.setSubjectId(id);
            if (t.subjectName != null && !t.subjectName.isEmpty()) subject.setSubjectName(t.subjectName);

            if (subject.getClassCodes() == null) subject.setClassCodes(new ArrayList<>());
            if (subject.getLecturers() == null) subject.setLecturers(new ArrayList<>());

            // merge/add class codes as child entities
            for (String code : t.codes) {
                List<ClassCode> matches = classCodeRepository.findByClassCode(code);
                ClassCode cc;
                if (!matches.isEmpty()) {
                    cc = matches.get(0);
                    // reassign subject if needed
                    if (cc.getSubject() == null || !Objects.equals(cc.getSubject().getSubjectId(), subject.getSubjectId())) {
                        cc.setSubject(subject);
                    }
                } else {
                    cc = new ClassCode();
                    cc.setClassCode(code);
                    cc.setSubject(subject);
                    // do not save now; will be cascaded when saving subject
                }

                boolean present = false;
                for (ClassCode x : subject.getClassCodes()) {
                    if (Objects.equals(x.getId(), cc.getId()) || Objects.equals(x.getClassCode(), cc.getClassCode())) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    subject.getClassCodes().add(cc);
                }
            }

            // merge/add lecturers as child entities
            for (String lec : t.lecturers) {
                List<SubjectLecturer> matches = subjectLecturerRepository.findByLecturer(lec);
                SubjectLecturer sl;
                if (!matches.isEmpty()) {
                    sl = matches.get(0);
                    if (sl.getSubject() == null || !Objects.equals(sl.getSubject().getSubjectId(), subject.getSubjectId())) {
                        sl.setSubject(subject);
                    }
                } else {
                    sl = new SubjectLecturer();
                    sl.setLecturer(lec);
                    sl.setSubject(subject);
                    // do not save now; will be cascaded
                }

                boolean present = false;
                for (SubjectLecturer x : subject.getLecturers()) {
                    if (Objects.equals(x.getId(), sl.getId()) || Objects.equals(x.getLecturer(), sl.getLecturer())) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    subject.getLecturers().add(sl);
                }
            }

            toSave.add(subject);
        }

        return courseRepository.saveAll(toSave);
    }

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
