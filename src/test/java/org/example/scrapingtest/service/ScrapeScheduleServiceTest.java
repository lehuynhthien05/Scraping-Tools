package org.example.scrapingtest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScrapeScheduleServiceTest {

    @Test
    void processCourseTable_parsesOnMouseOverCorrectly() throws Exception {
        ScrapeScheduleService service = new ScrapeScheduleService();

        WebElement headerRow = mock(WebElement.class);
        WebElement dataRow = mock(WebElement.class);

        when(headerRow.findElements(any())).thenReturn(Collections.emptyList());

        WebElement dataCell = mock(WebElement.class);
        String onmouseover = "ddrivetip('ITIT23IU01','Net-Centric Programming','IT096IU nhóm 01','Thứ Năm','4','A1.402','7','3','Instructor','15/01/2026','08/05/2026')";
        when(dataCell.getAttribute("onmouseover")).thenReturn(onmouseover);
        // No nested inputs
        when(dataCell.findElements(any())).thenReturn(Collections.emptyList());

        // dataRow.findElements(By.tagName("td")) -> list with the dataCell
        when(dataRow.findElements(any())).thenReturn(Collections.singletonList(dataCell));

        List<WebElement> rows = Arrays.asList(headerRow, dataRow);

        // Invoke private method processCourseTable via reflection
        Method m = ScrapeScheduleService.class.getDeclaredMethod("processCourseTable", List.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, String>>> result = (Map<String, List<Map<String, String>>>) m.invoke(service, rows);

        assertNotNull(result);
        assertTrue(result.containsKey("Thứ Năm"));
        List<Map<String, String>> list = result.get("Thứ Năm");
        assertEquals(1, list.size());
        Map<String, String> course = list.get(0);

        assertEquals("Net-Centric Programming", course.get("subject"));
        assertEquals("ITIT23IU01", course.get("courseCode"));
        assertEquals("A1.402", course.get("room"));
        assertEquals("Tiết 7 - 9", course.get("timeSlot")); // startPeriod 7, periodCount 7 => 7..13
        assertEquals("Instructor", course.get("instructor"));
        assertEquals("15/01/2026", course.get("startDate"));
        assertEquals("08/05/2026", course.get("endDate"));
    }
}