package org.example.scrapingtest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectImportDto {
    private String subjectName;
    private List<String> classCodes;
    private List<String> lecturers;
    private String subjectId;
}

