package org.example.scrapingtest.service;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class ScrapeCourseService {

    private final LoginService loginService;

    public ScrapeCourseService(LoginService loginService) {
        this.loginService = loginService;
    }

    public Map<String, List<Map<String, String>>> scrapeCourses(
            String username,
            String password,
            String faculty
    ) {

        WebDriver driver = loginService.login(username, password);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();

        try {
            driver.get("https://edusoftweb.hcmiu.edu.vn/Default.aspx?page=dkmonhoc");

            WebElement chkDieuKien = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("input[type='checkbox'][onclick*='chkHienThiDieuKien']")
                    )
            );

            if (!chkDieuKien.isSelected()) {
                chkDieuKien.click();
            }

            WebElement selectKhoaElement = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("selectKhoa"))
            );

            Select selectKhoa = new Select(selectKhoaElement);
            selectKhoa.selectByValue(faculty);


            wait.until(driver1 ->
                    driver1.findElements(
                            By.cssSelector("#pnlDSMonhocDK table.body-table")
                    ).size() > 0
            );


            List<String> headers = extractHeaders(driver);

            List<Map<String, String>> courses =
                    extractCourseData(driver, headers);

            result.put(faculty, courses);

            return result;

        } finally {
            driver.quit();
        }
    }

    // ============================================
    // HEADER
    // ============================================
    private List<String> extractHeaders(WebDriver driver) {
        List<String> headers = new ArrayList<>();

        WebElement headerTable = driver.findElement(
                By.cssSelector("#pnlDSMonhocDK table.title-table")
        );

        List<WebElement> tds = headerTable.findElements(By.tagName("td"));

        for (WebElement td : tds) {
            String text = td.getText().trim();
            if (!text.isEmpty()) {
                headers.add(text);
            }
        }

        return headers;
    }


    private List<Map<String, String>> extractCourseData(
            WebDriver driver,
            List<String> headers
    ) {

        List<Map<String, String>> courses = new ArrayList<>();

        List<WebElement> courseTables = driver.findElements(
                By.cssSelector("#pnlDSMonhocDK table.body-table")
        );

        for (WebElement table : courseTables) {

            WebElement row = table.findElement(By.tagName("tr"));
            List<WebElement> cells = row.findElements(By.tagName("td"));

            Map<String, String> course = new LinkedHashMap<>();
            int headerIndex = 0;

            for (WebElement cell : cells) {

                if (!cell.findElements(By.tagName("input")).isEmpty()) {
                    continue;
                }

                if (headerIndex < headers.size()) {
                    course.put(
                            headers.get(headerIndex),
                            cell.getText().trim()
                    );
                    headerIndex++;
                }
            }

            if (!course.isEmpty()) {
                courses.add(course);
            }
        }

        return courses;
    }
}
