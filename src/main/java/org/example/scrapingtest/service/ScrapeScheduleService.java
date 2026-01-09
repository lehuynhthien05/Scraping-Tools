package org.example.scrapingtest.service;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapeScheduleService {
    public Map<String, List<Map<String, String>>> scrapeCoursesByStudentId(String studentId) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        // options.addArguments("--headless=new");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // Navigate directly to the timetable page with the student ID
            driver.get("https://edusoftweb.hcmiu.edu.vn/default.aspx?page=thoikhoabieu&sta=0&id=" + studentId);
            WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ContentPlaceHolder1_ctl00_Table1")));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            // Process the table rows to extract course information
            return processCourseTable(rows);
        } finally {
            driver.quit();
        }
    }

    private Map<String, List<Map<String, String>>> processCourseTable(List<WebElement> rows) {
        Map<String, List<Map<String, String>>> coursesByDay = new LinkedHashMap<>();

        // Regex pattern to extract course details from onmouseover attribute
        //Ex: 'ITIT23IU01','Net-Centric Programming','IT096IU nhóm 01','Thứ Năm','4','A1.402','7','3','Instructor','15/01/2026','08/05/2026')'
        Pattern pattern = Pattern.compile(
                "ddrivetip\\('([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)'"
        );

        // Skip header row (index 0) and crawl data rows
        for (int r = 1; r < rows.size(); r++) {
            List<WebElement> cols = rows.get(r).findElements(By.tagName("td"));
            for (WebElement cell : cols) {

                String onmouseover = cell.getAttribute("onmouseover");
                if (onmouseover == null || onmouseover.isEmpty()) continue;

                Matcher matcher = pattern.matcher(onmouseover);
                if (!matcher.find() || matcher.group(2).trim().isEmpty()) continue;

                String subject = matcher.group(2).trim();
                String courseCode = matcher.group(1).trim();
                String dayOfWeek = matcher.group(4).trim();
                String room = matcher.group(6).trim();
                String startPeriod = matcher.group(7).trim();
                String periodCount = matcher.group(8).trim();
                String instructor = matcher.group(9).trim();
                String startDate = matcher.group(10).trim();
                String endDate = matcher.group(11).trim();
                String timeSlot = "Tiết " + startPeriod + " - " + (Integer.parseInt(startPeriod) + Integer.parseInt(periodCount) - 1);

                Map<String, String> courseInfo = new LinkedHashMap<>();
                courseInfo.put("subject", subject);
                courseInfo.put("courseCode", courseCode);
                courseInfo.put("room", room);
                courseInfo.put("timeSlot", timeSlot);
                courseInfo.put("instructor", instructor);
                courseInfo.put("startDate", startDate);
                courseInfo.put("endDate", endDate);

                coursesByDay.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(courseInfo);
            }
        }
        return coursesByDay;
    }
}
