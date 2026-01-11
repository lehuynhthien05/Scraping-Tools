// java
package org.example.scrapingtest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScrapeCourseServiceTest {

    @Test
    void extractHeaders_returnsTrimmedHeaders() throws Exception {
        // Prepare mocks
        WebDriver driver = mock(WebDriver.class);
        WebElement headerTable = mock(WebElement.class);

        WebElement td1 = mock(WebElement.class);
        WebElement td2 = mock(WebElement.class);
        when(td1.getText()).thenReturn("  Header1  ");
        when(td2.getText()).thenReturn("Header2");

        when(headerTable.findElements(any())).thenReturn(Arrays.asList(td1, td2));
        when(driver.findElement(By.cssSelector("#pnlDSMonhocDK table.title-table"))).thenReturn(headerTable);

        // Create service (loginService not used here)
        ScrapeCourseService service = new ScrapeCourseService(null);

        Method m = ScrapeCourseService.class.getDeclaredMethod("extractHeaders", WebDriver.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) m.invoke(service, driver);

        assertEquals(2, headers.size());
        assertEquals("Header1", headers.get(0));
        assertEquals("Header2", headers.get(1));
    }

    @Test
    void extractCourseData_parsesTablesIntoMaps() throws Exception {
        WebDriver driver = mock(WebDriver.class);

        // Prepare headers
        List<String> headers = Arrays.asList("ColA", "ColB", "ColC");

        // Mock a body table
        WebElement table = mock(WebElement.class);
        WebElement row = mock(WebElement.class);
        WebElement cellA = mock(WebElement.class);
        WebElement cellB = mock(WebElement.class);
        WebElement cellC = mock(WebElement.class);

        when(cellA.findElements(By.tagName("input"))).thenReturn(Collections.emptyList());
        when(cellB.findElements(By.tagName("input"))).thenReturn(Collections.emptyList());
        when(cellC.findElements(By.tagName("input"))).thenReturn(Collections.emptyList());

        when(cellA.getText()).thenReturn("A1");
        when(cellB.getText()).thenReturn("B1");
        when(cellC.getText()).thenReturn("C1");

        when(row.findElements(By.tagName("td"))).thenReturn(Arrays.asList(cellA, cellB, cellC));
        when(table.findElement(By.tagName("tr"))).thenReturn(row);

        when(driver.findElements(By.cssSelector("#pnlDSMonhocDK table.body-table"))).thenReturn(Collections.singletonList(table));

        ScrapeCourseService service = new ScrapeCourseService(null);

        Method m = ScrapeCourseService.class.getDeclaredMethod("extractCourseData", WebDriver.class, List.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> courses = (List<Map<String, String>>) m.invoke(service, driver, headers);

        assertEquals(1, courses.size());
        Map<String, String> course = courses.get(0);
        assertEquals("A1", course.get("ColA"));
        assertEquals("B1", course.get("ColB"));
        assertEquals("C1", course.get("ColC"));
    }
}