package org.example.scrapingtest.repository;

import org.example.scrapingtest.model.Subjects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CourseRepository extends JpaRepository<Subjects, Long> {
}
