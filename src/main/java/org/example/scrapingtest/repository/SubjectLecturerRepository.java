package org.example.scrapingtest.repository;

import org.example.scrapingtest.model.SubjectLecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectLecturerRepository extends JpaRepository<SubjectLecturer, Long> {
    List<SubjectLecturer> findByLecturer(String lecturer);
}
