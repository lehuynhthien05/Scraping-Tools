package org.example.scrapingtest.service;

import org.example.scrapingtest.model.ClassCode;
import org.example.scrapingtest.model.SubjectLecturer;
import org.example.scrapingtest.model.Subjects;
import org.example.scrapingtest.repository.ClassCodeRepository;
import org.example.scrapingtest.repository.CourseRepository;
import org.example.scrapingtest.repository.SubjectLecturerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ClassCodeRepository classCodeRepository;
    @Mock
    private SubjectLecturerRepository subjectLecturerRepository;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        courseService = new CourseService(courseRepository, classCodeRepository, subjectLecturerRepository);
    }

    @Test
    void saveCourse_createsNewSubjectWithChildren() {
        Map<String, List<Map<String, String>>> courseData = new LinkedHashMap<>();
        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> r = new HashMap<>();
        r.put("Mã MH", "IT013IU");
        r.put("Tên môn học", "Algorithms & Data Structures");
        r.put("Mã lớp", "ITIT244WE31");
        r.put("Giảng viên", "Đ.T.Nhân");
        rows.add(r);
        courseData.put("IT", rows);

        when(classCodeRepository.findByClassCode("ITIT244WE31")).thenReturn(Collections.emptyList());
        when(subjectLecturerRepository.findByLecturer("Đ.T.Nhân")).thenReturn(Collections.emptyList());

        ArgumentCaptor<List<Subjects>> captor = ArgumentCaptor.forClass((Class) List.class);
        when(courseRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));


        verify(classCodeRepository, times(0)).save(any(ClassCode.class)); // saved via cascade
        verify(subjectLecturerRepository, times(0)).save(any(SubjectLecturer.class));
        verify(courseRepository).saveAll(captor.capture());

        List<Subjects> arg = captor.getValue();
        assertThat(arg).hasSize(1);
        Subjects s = arg.get(0);
        assertThat(s.getSubjectId()).isEqualTo("IT013IU");
        assertThat(s.getSubjectName()).isEqualTo("Algorithms & Data Structures");
        assertThat(s.getClassCodes()).hasSize(1);
        assertThat(s.getLecturers()).hasSize(1);
        assertThat(s.getClassCodes().get(0).getClassCode()).isEqualTo("ITIT244WE31");
        assertThat(s.getLecturers().get(0).getLecturer()).isEqualTo("Đ.T.Nhân");
    }

    @Test
    void saveCourse_mergesWithExistingSubject() {
        // existing subject with one class and one lecturer
        Subjects existing = new Subjects();
        existing.setSubjectId("IT013IU");
        existing.setSubjectName("Algorithms & Data Structures");
        ClassCode existingCc = new ClassCode();
        existingCc.setId(1L);
        existingCc.setClassCode("EXISTING_CODE");
        existingCc.setSubject(existing);
        existing.setClassCodes(new ArrayList<>(Collections.singletonList(existingCc)));
        SubjectLecturer existingSl = new SubjectLecturer();
        existingSl.setId(10L);
        existingSl.setLecturer("EXISTING_LEC");
        existingSl.setSubject(existing);
        existing.setLecturers(new ArrayList<>(Collections.singletonList(existingSl)));

        when(courseRepository.findAllById(anyCollection())).thenReturn(Collections.singletonList(existing));

        Map<String, List<Map<String, String>>> courseData = new LinkedHashMap<>();
        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> r = new HashMap<>();
        r.put("Mã MH", "IT013IU");
        r.put("Tên môn học", "Algorithms & Data Structures");
        r.put("Mã lớp", "NEW_CODE");
        r.put("Giảng viên", "NEW_LEC");
        rows.add(r);
        courseData.put("IT", rows);

        when(classCodeRepository.findByClassCode("NEW_CODE")).thenReturn(Collections.emptyList());
        when(subjectLecturerRepository.findByLecturer("NEW_LEC")).thenReturn(Collections.emptyList());

        when(courseRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Subjects> saved = courseService.saveCourse(courseData);

        assertThat(saved).hasSize(1);
        Subjects s = saved.get(0);
        // existing items should remain and new ones added
        assertThat(s.getClassCodes().stream().map(ClassCode::getClassCode)).contains("EXISTING_CODE", "NEW_CODE");
        assertThat(s.getLecturers().stream().map(SubjectLecturer::getLecturer)).contains("EXISTING_LEC", "NEW_LEC");
    }
}

