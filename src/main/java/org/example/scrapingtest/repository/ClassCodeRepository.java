package org.example.scrapingtest.repository;

import org.example.scrapingtest.model.ClassCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassCodeRepository extends JpaRepository<ClassCode, Long> {
    List<ClassCode> findByClassCode(String classCode);
}
